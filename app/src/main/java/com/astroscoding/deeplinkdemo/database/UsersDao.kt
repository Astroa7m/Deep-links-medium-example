package com.astroscoding.deeplinkdemo.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users ORDER BY id")
    fun getUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE :id = id")
    suspend fun getUser(id: Int?): User?

}