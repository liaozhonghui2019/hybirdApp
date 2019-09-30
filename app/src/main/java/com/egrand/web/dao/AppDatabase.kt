package com.egrand.web.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.egrand.web.entity.App
import com.egrand.web.entity.User

@Database(entities = [User::class, App::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appDao(): APPDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private var dbName="hybirdApp.db"
        fun getInstance(cxt: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(cxt).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): AppDatabase {

            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, dbName
            )
                .build()
        }
    }
}
