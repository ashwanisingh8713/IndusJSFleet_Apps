package com.indusjs.fleet.di

/**
 * Module providing network-related dependencies.
 *
 * Note: In Metro, network dependencies (HttpClient, Json) are provided
 * through the main RootGraph. This module serves as documentation for
 * available network dependencies.
 */
object NetworkModule {
    // Network dependencies are provided through RootGraph:
    // - HttpClient: configured with ContentNegotiation and Logging
    // - Json: configured for API serialization
    //
    // Access via AppDependencies.httpClient or AppDependencies.json
}

