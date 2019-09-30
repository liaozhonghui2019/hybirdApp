package com.egrand.web.entity

/**
 * ico on 2019/09/21.
 */
class DialogMenuItem {
    var id: Long = -1
    var name: String = ""
    var url: String = ""
    var icon: Int = 0

    constructor()

    constructor(id: Long, name: String, url: String, icon: Int) {
        this.id = id
        this.name = name
        this.url = url
        this.icon = icon
    }
}
