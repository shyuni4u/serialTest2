package com.loenzo.serialtest2.room

import androidx.room.*
import androidx.room.OnConflictStrategy.ABORT
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface LastPictureDao {
    @Query("SELECT * FROM category")
    fun getAll(): List<LastPicture>

    @Insert(onConflict = REPLACE)
    fun insert(item: LastPicture): Long

    @Delete
    fun delete(item: LastPicture)

    @Update(onConflict = ABORT)
    fun update(item: LastPicture)

    @Query("DELETE FROM category")
    fun deleteAll()
}