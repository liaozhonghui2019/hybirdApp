package com.egrand.web.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ico on 2019/7/30.
 */
@Entity(tableName = "SYS_APP")
open class App {
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1
    var name: String = ""
    var url: String = ""
    var icon: Int = 0

    constructor(id: Long, name: String, url: String, icon: Int) {
        this.id = id
        this.name = name
        this.url = url
        this.icon = icon
    }
}
