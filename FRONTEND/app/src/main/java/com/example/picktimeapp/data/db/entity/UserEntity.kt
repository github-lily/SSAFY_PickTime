package com.example.picktimeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null,
    val lastLoginTime: Long = System.currentTimeMillis()
)