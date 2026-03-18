package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cost Type Category DTO.
 * Top-level container for cost types, grouping them by category
 * (e.g., "Trip Costs", "Maintenance Costs", "Driver Costs").
 */
@Serializable
data class CostTypeCategoryDto(
    @SerialName("category_id")
    val categoryId: String = "",
    @SerialName("category_name")
    val categoryName: String = "",
    @SerialName("groups")
    val groups: List<CostTypeGroupDto> = emptyList()
) : Dto

/**
 * Cost Type Group DTO.
 * Represents a hierarchical group of cost types (e.g., "Fuel & Energy", "Salary & Wages").
 * Shared across Trip, Maintenance, and Driver cost type structures.
 */
@Serializable
data class CostTypeGroupDto(
    @SerialName("group_id")
    val groupId: String,
    @SerialName("group_name")
    val groupName: String,
    @SerialName("items")
    val items: List<CostTypeItemDto> = emptyList()
) : Dto

/**
 * Cost Type Item DTO.
 * Represents a single cost type within a group (e.g., "Diesel", "Monthly Salary").
 */
@Serializable
data class CostTypeItemDto(
    @SerialName("id")
    val id: String,
    @SerialName("value")
    val value: String,
    @SerialName("label")
    val label: String
) : Dto
