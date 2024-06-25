package com.example.composesample.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemTable")
data class ItemDTO(
    @PrimaryKey
    var id: Long?,

    @ColumnInfo(name = "uuid")
    var uniqueId: String,
)

// DataCache Example Data Table
@Entity(tableName = "exampleTable")
data class UserData(
    @PrimaryKey
    val id: Long?,

    @ColumnInfo(name = "user_name")
    val userName: String
)