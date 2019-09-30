package com.egrand.web.db

import android.content.Context
import androidx.room.Room
import com.egrand.web.dao.AppDatabase

class AppDbHelper private constructor() {
    companion object {
        private lateinit var db: AppDatabase
        private var _initFlag = false
        private var instance: AppDbHelper? = null
            get() {
                if (field == null) {
                    field = AppDbHelper()
                }
                return field
            }


        fun get(cxt: Context): AppDbHelper {
            this.initDb(cxt)
            return instance!!
        }


        fun initDb(cxt: Context) {
            if (!_initFlag) {
                this.db = Room.databaseBuilder(cxt, AppDatabase::class.java, "hybird-db").build()
                this._initFlag = true
            }
        }
    }

    fun getDb(): AppDatabase {
        return db;
    }

}
