package com.egrand.web.dao

import androidx.room.*
import com.egrand.web.entity.User
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface UserDao {
    @Query("SELECT * FROM SYS_USER")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM SYS_USER WHERE uid IN (:userIds)")
    suspend fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM SYS_USER WHERE name LIKE :name LIMIT 1")
    suspend fun findByName(name: String): User

    @Insert
    suspend fun insertAll(vararg users: User)

    @Update
    suspend fun update(vararg users: User)

    @Delete
    suspend fun delete(vararg users: User)
    /**
     * Get a user by id.
     * @return the user from the table with a specific id.
     */
    @Query("SELECT * FROM SYS_USER WHERE uid = :id")
    fun getUserById(id: Int): Flowable<User>

    /**
     * Insert a user in the database. If the user already exists, replace it.
     * @param user the user to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Completable

    /**
     * Delete all users.
     */
    @Query("DELETE FROM SYS_USER")
    fun deleteAllUsers()
}
