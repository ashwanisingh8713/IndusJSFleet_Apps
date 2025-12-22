package com.indusjs.fleet.data.mapper

/**
 * Base interface for mapping between data layer models and domain entities.
 *
 * @param D Data layer model (DTO, Entity from Room, etc.)
 * @param E Domain layer entity
 */
interface Mapper<D, E> {
    /**
     * Maps from data model to domain entity.
     */
    fun mapToDomain(data: D): E

    /**
     * Maps from domain entity to data model.
     */
    fun mapToData(entity: E): D
}

/**
 * Extension to map a list of data models to domain entities.
 */
fun <D, E> Mapper<D, E>.mapToDomainList(dataList: List<D>): List<E> = dataList.map { mapToDomain(it) }

/**
 * Extension to map a list of domain entities to data models.
 */
fun <D, E> Mapper<D, E>.mapToDataList(entities: List<E>): List<D> = entities.map { mapToData(it) }

