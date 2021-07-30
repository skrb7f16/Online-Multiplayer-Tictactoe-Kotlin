package com.skrb7f16.onlinetictactoe.models

class Users {
    lateinit var username:String
    lateinit var userId:String

    constructor(username: String, userId: String) {
        this.username = username
        this.userId = userId
    }

    constructor()


}