package com.egrand.web.ui

import androidx.lifecycle.ViewModel
import com.egrand.web.dao.UserDao
import com.egrand.web.entity.User
import io.reactivex.Completable
import io.reactivex.Flowable


/**
 * View Model for the [UserActivity]
 */
class UserViewModel(private val dataSource: UserDao) : ViewModel() {

    /**
     * Get the user name of the user.
     * @return a [Flowable] that will emit every time the user name has been updated.
     */
    // for every emission of the user, get the user name
    fun userName(): Flowable<String> {
        return dataSource.getUserById(USER_ID)
            .map { user -> user.name }
    }

    /**
     * Update the user name.
     * @param userName the new user name
     * *
     * @return a [Completable] that completes when the user name is updated
     */
    fun updateUserName(userName: String,userPwd:String): Completable {
        val user = User(USER_ID, userName,userPwd)
        return dataSource.insertUser(user)
    }

    companion object {
        // using a hardcoded value for simplicity
        const val USER_ID =1
    }
}
