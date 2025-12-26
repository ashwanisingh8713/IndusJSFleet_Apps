package com.indusjs.fleet.locationtracker.mqtt

import android.content.Context
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
 * MQTT Client for publishing vehicle location updates.
 *
 * Uses HiveMQ MQTT client to connect to the broker and publish
 * location messages to the appropriate topics.
 */
@Suppress("UNUSED_PARAMETER")
class MqttLocationClient(context: Context) {

    private val log = Logger.withTag("MqttLocationClient")
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

    // Buffer for offline messages
    private val messageBuffer = Channel<Pair<String, LocationMessage>>(Channel.BUFFERED)

    /**
     * Connect to the MQTT broker with the given configuration.
     */
    fun connect(config: TrackerConfig) {
        if (_isConnected.value && currentConfig?.mqttBrokerUrl == config.mqttBrokerUrl) {
            log.d { "Already connected to ${config.mqttBrokerUrl}" }
            return
        }

        currentConfig = config

        try {
            // Parse broker URL (format: tcp://host:port or ssl://host:port)
            val uri = URI(config.mqttBrokerUrl.replace("tcp://", "http://").replace("ssl://", "https://"))
            val host = uri.host ?: "localhost"
            val port = if (uri.port > 0) uri.port else 1883
            val useSsl = config.mqttBrokerUrl.startsWith("ssl://")

            // Generate client ID if not provided
            val clientId = config.mqttClientId.ifBlank {
                "fleet-vehicle-${config.registrationNumber}-${System.currentTimeMillis()}"
            }

            log.i { "Connecting to MQTT broker: $host:$port (SSL: $useSsl). Note: MQTT uses port 1883, not 18083" }

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

            // Handle connection result in a coroutine to avoid API level issues
            scope.launch {
                try {
                    future.get() // Block until connected
                    log.i { "Connected to MQTT broker" }
                    _isConnected.value = true
                    _lastError.value = null

                    // Publish any buffered messages
                    publishBufferedMessages()
                } catch (e: Exception) {
                    log.e { "Failed to connect: ${e.message}. Verify MQTT port is 1883 (not 18083)" }
                    _isConnected.value = false
                    _lastError.value = "Connection failed: ${e.message}. Check if port is 1883"

                    // Try to reconnect after delay
                    kotlinx.coroutines.delay(5000)
                    currentConfig?.let { connect(it) }
                }
            }

        } catch (e: Exception) {
            log.e { "Error creating MQTT client: ${e.message}" }
            _isConnected.value = false
            _lastError.value = "Error: ${e.message}"
        }
    }

    /**
     * Disconnect from the MQTT broker.
     */
    fun disconnect() {
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
}

