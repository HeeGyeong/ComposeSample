package com.example.composesample.sub

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM itemTable WHERE uuid" +
            " LIKE:startsWith || '%' ORDER BY id DESC")
    fun searchData(startsWith: String): Flow<List<ItemDTO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemDTO)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: ItemDTO)

    @Delete
    suspend fun delete(item: ItemDTO)

    @Query("DELETE FROM itemTable")
    suspend fun clear()
}