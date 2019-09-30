package com.egrand.web

import android.content.Context
import com.egrand.web.dao.AppDatabase
import com.egrand.web.dao.UserDao
import com.egrand.web.ui.ViewModelFactory

/**
 * Enables injection of data sources.
 */
object Injection {
    fun provideUserDataSource(context: Context): UserDao {
        val database = AppDatabase.getInstance(context)
        return database.userDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return ViewModelFactory(dataSource)
    }
}
