package me.talofa.app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a group for categorizing shared content
 * If id is null, it indicates a new group to be created on the server
 */
@Parcelize
data class Group(
    val id: String?,
    val name: String,
    val icon: String? = null,
    val description: String? = null
) : Parcelable
