package com.egrand.web.dao

import androidx.room.*
import com.egrand.web.entity.App

@Dao
interface APPDao {
    @Query("SELECT * FROM SYS_APP")
    suspend fun getAll(): List<App>

    @Query("SELECT * FROM SYS_APP WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<App>

    @Insert
    suspend fun insertAll(apps: List<App>)

    @Update
    suspend fun update(vararg apps: App)

    @Delete
    suspend fun delete(vararg apps: App)
}
