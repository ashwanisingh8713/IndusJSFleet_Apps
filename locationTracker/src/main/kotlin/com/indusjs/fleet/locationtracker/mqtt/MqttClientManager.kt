package com.indusjs.fleet.locationtracker.mqtt

import co.touchlab.kermit.Logger
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.indusjs.fleet.locationtracker.data.LocationMessage
import com.indusjs.fleet.locationtracker.data.TrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URI

/**
 * Singleton MQTT Client Manager for sharing connection state across app components.
 *
 * This ensures the UI can observe the same connection state that the service uses.
 */
object MqttClientManager {

    private val log = Logger.withTag("MqttClientManager")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var mqttClient: Mqtt3AsyncClient? = null
    private var currentConfig: TrackerConfig? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messagesPublished = MutableStateFlow(0L)
    val messagesPublished: StateFlow<Long> = _messagesPublished.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    // Track if connection is in progress to prevent duplicate connections
    @Volatile
    private var isConnecting = false

    // Buffer for offline messages
    private val messageBuffer = Channel<Pair<String, LocationMessage>>(Channel.BUFFERED)

    /**
     * Connect to the MQTT broker with the given configuration.
     */
    @Synchronized
    fun connect(config: TrackerConfig) {
        // Already connected to the same broker
        if (_isConnected.value && currentConfig?.mqttBrokerUrl == config.mqttBrokerUrl) {
            log.d { "Already connected to ${config.mqttBrokerUrl}" }
            return
        }

        // Connection already in progress
        if (isConnecting) {
            log.d { "Connection already in progress" }
            return
        }

        // Disconnect existing client if connecting to a different broker
        if (mqttClient != null && currentConfig?.mqttBrokerUrl != config.mqttBrokerUrl) {
            log.i { "Disconnecting from previous broker to connect to new one" }
            disconnect()
        }

        currentConfig = config
        isConnecting = true
        _connectionState.value = "Connecting..."

        try {
            // Parse broker URL (format: tcp://host:port or ssl://host:port)
            val uri = URI(config.mqttBrokerUrl.replace("tcp://", "http://").replace("ssl://", "https://"))
            val host = uri.host ?: "localhost"
            val port = if (uri.port > 0) uri.port else 1883
            val useSsl = config.mqttBrokerUrl.startsWith("ssl://")

            // Generate stable client ID (no timestamp to avoid multiple clients)
            val clientId = config.mqttClientId.ifBlank {
                "fleet-vehicle-${config.registrationNumber}"
            }

            log.i { "Connecting to MQTT broker: $host:$port (SSL: $useSsl) with clientId: $clientId" }
            _connectionState.value = "Connecting to $host:$port..."

            val clientBuilder = MqttClient.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .useMqttVersion3()

            mqttClient = if (useSsl) {
                clientBuilder.sslWithDefaultConfig().buildAsync()
            } else {
                clientBuilder.buildAsync()
            }

            val connectBuilder = mqttClient!!.connectWith()
                .cleanSession(true)
                .keepAlive(30)

            if (config.mqttUsername.isNotBlank()) {
                connectBuilder.simpleAuth()
                    .username(config.mqttUsername)
                    .password(config.mqttPassword.toByteArray())
                    .applySimpleAuth()
            }

            val future = connectBuilder.send()

            // Handle connection result in a coroutine
            scope.launch {
                try {
                    future.get() // Block until connected
                    log.i { "Connected to MQTT broker" }
                    isConnecting = false
                    _isConnected.value = true
                    _connectionState.value = "Connected"
                    _lastError.value = null

                    // Publish any buffered messages
                    publishBufferedMessages()
                } catch (e: Exception) {
                    isConnecting = false
                    val errorMsg = e.message ?: "Unknown error"
                    val cause = e.cause?.message ?: ""
                    log.e { "Failed to connect: $errorMsg. Cause: $cause" }
                    _isConnected.value = false
                    _connectionState.value = "Connection Failed"

                    // Provide more helpful error messages
                    _lastError.value = when {
                        errorMsg.contains("Connection refused") || cause.contains("Connection refused") ->
                            "Connection refused. Check if MQTT broker is running on port 1883"
                        errorMsg.contains("UnknownHostException") || cause.contains("UnknownHostException") ||
                        errorMsg.contains("Unable to resolve host") || cause.contains("Unable to resolve host") ->
                            "Cannot reach host. Check the IP address"
                        errorMsg.contains("SocketTimeoutException") || cause.contains("timed out") ||
                        errorMsg.contains("timed out") ->
                            "Connection timed out. Check network and broker address"
                        errorMsg.contains("18083") || cause.contains("18083") ->
                            "Port 18083 is the dashboard port. Use port 1883 for MQTT"
                        errorMsg.contains("CLEARTEXT") || cause.contains("CLEARTEXT") ->
                            "Cleartext traffic not allowed. Check network_security_config.xml"
                        errorMsg.contains("Network is unreachable") || cause.contains("Network is unreachable") ->
                            "Network unreachable. Check WiFi/mobile data connection"
                        else -> "Connection failed: $errorMsg ${if (cause.isNotEmpty()) "($cause)" else ""}"
                    }

                    // Reconnection is handled by the watchdog in the service, not here
                    // This prevents infinite reconnection loops
                }
            }

        } catch (e: Exception) {
            isConnecting = false
            log.e { "Error creating MQTT client: ${e.message}" }
            _isConnected.value = false
            _connectionState.value = "Error"
            _lastError.value = "Error: ${e.message}"
        }
    }

    /**
     * Disconnect from the MQTT broker.
     */
    fun disconnect() {
        isConnecting = false
        _connectionState.value = "Disconnecting..."
        try {
            scope.launch {
                try {
                    mqttClient?.disconnect()?.get()
                    log.i { "Disconnected from MQTT broker" }
                } catch (e: Exception) {
                    log.e { "Error disconnecting: ${e.message}" }
                }
            }
        } catch (e: Exception) {
            log.e { "Error disconnecting: ${e.message}" }
        } finally {
            mqttClient = null
            _isConnected.value = false
            _connectionState.value = "Disconnected"
        }
    }

    /**
     * Publish a location message to the appropriate MQTT topic.
     */
    fun publishLocation(location: LocationMessage) {
        val config = currentConfig ?: run {
            log.e { "No configuration available" }
            return
        }

        val topic = config.getMqttTopic()

        scope.launch {
            try {
                val payload = json.encodeToString(LocationMessage.serializer(), location)

                if (_isConnected.value && mqttClient != null) {
                    try {
                        mqttClient!!.publishWith()
                            .topic(topic)
                            .payload(payload.toByteArray(Charsets.UTF_8))
                            .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                            .retain(false)
                            .send()
                            .get() // Block until published

                        _messagesPublished.value++
                        log.d { "Published location to $topic" }
                    } catch (e: Exception) {
                        log.e { "Failed to publish: ${e.message}" }
                        messageBuffer.send(topic to location)

                        // Mark as disconnected and try to reconnect
                        _isConnected.value = false
                        _connectionState.value = "Reconnecting..."
                        currentConfig?.let { connect(it) }
                    }

                    // Also publish to trip topic if there's an active trip
                    config.getTripMqttTopic()?.let { tripTopic ->
                        try {
                            mqttClient!!.publishWith()
                                .topic(tripTopic)
                                .payload(payload.toByteArray(Charsets.UTF_8))
                                .qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                                .retain(false)
                                .send()
                                .get()
                        } catch (e: Exception) {
                            log.e { "Failed to publish to trip topic: ${e.message}" }
                        }
                    }

                } else {
                    log.w { "Not connected, buffering message" }
                    messageBuffer.send(topic to location)
                }

            } catch (e: Exception) {
                log.e { "Error publishing location: ${e.message}" }
                _lastError.value = "Publish error: ${e.message}"
            }
        }
    }

    /**
     * Publish buffered messages after reconnection.
     */
    private fun publishBufferedMessages() {
        while (true) {
            val result = messageBuffer.tryReceive()
            if (result.isSuccess) {
                val (topic, location) = result.getOrThrow()
                try {
                    val payload = json.encodeToString(LocationMessage.serializer(), location)
                    mqttClient?.publishWith()
                        ?.topic(topic)
                        ?.payload(payload.toByteArray(Charsets.UTF_8))
                        ?.qos(com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE)
                        ?.retain(false)
                        ?.send()
                        ?.get()
                    _messagesPublished.value++
                } catch (e: Exception) {
                    log.e { "Error publishing buffered message: ${e.message}" }
                }
            } else {
                break
            }
        }
    }

    /**
     * Check if the client is currently connected.
     */
    fun isClientConnected(): Boolean {
        return _isConnected.value
    }

    /**
     * Reset the messages published counter.
     */
    fun resetMessagesCounter() {
        _messagesPublished.value = 0
    }
}

