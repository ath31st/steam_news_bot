package sidim.doma.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class Page<T>(
    val items: List<T>,
    val totalItems: Long,
    val totalPages: Int,
    val currentPage: Int,
)
