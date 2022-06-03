package com.example.composesample.sub

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itemTable")
data class ItemDTO (
    @PrimaryKey
    var id: Long?,

    @ColumnInfo(name = "uuid")
    var uniqueId: String,
)