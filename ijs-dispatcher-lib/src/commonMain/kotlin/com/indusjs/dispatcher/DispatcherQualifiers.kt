package com.indusjs.dispatcher

/**
 * Qualifier annotations for dependency injection.
 * Use these to distinguish between different dispatcher types when injecting.
 *
 * Usage with Metro DI:
 * ```kotlin
 * @SingleIn(AppScope::class)
 * @DependencyGraph
 * abstract class AppGraph {
 *     @Provides
 *     @MainDispatcher
 *     fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
 *
 *     @Provides
 *     @IoDispatcher
 *     fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
 * }
 *
 * @Inject
 * class MyRepository(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 */

/**
 * Qualifier for Main/UI dispatcher.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MainDispatcher

/**
 * Qualifier for IO dispatcher.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class IoDispatcher

/**
 * Qualifier for Default/CPU dispatcher.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DefaultDispatcher

/**
 * Qualifier for Unconfined dispatcher.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class UnconfinedDispatcher

/**
 * Qualifier for Application-level CoroutineScope.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ApplicationScope

/**
 * Qualifier for ViewModel-level CoroutineScope.
 */
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ViewModelScope

