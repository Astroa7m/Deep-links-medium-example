package com.astroscoding.deeplinkdemo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val description: String,
    val joinedYear: Int,
    val isElite: Boolean
)
