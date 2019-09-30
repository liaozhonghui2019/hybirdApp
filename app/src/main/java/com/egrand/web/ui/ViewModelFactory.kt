package com.egrand.web.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.egrand.web.dao.UserDao


/**
 * Factory for ViewModels
 */
@Singleton
class ViewModelFactory @Inject constructor(private val dataSource: UserDao) : ViewModelProvider.Factory,
    Parcelable {

    constructor(parcel: Parcel) : this(TODO("dataSource")) {
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ViewModelFactory> {
        override fun createFromParcel(parcel: Parcel): ViewModelFactory {
            return ViewModelFactory(parcel)
        }

        override fun newArray(size: Int): Array<ViewModelFactory?> {
            return arrayOfNulls(size)
        }
    }
}
