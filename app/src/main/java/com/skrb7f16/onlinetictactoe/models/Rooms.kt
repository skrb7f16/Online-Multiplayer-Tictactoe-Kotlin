package com.skrb7f16.onlinetictactoe.models

open class Rooms{
    lateinit var roomname:String
    lateinit var roomKey:String
    lateinit var creator:Users
    var isFull:Boolean=false
    lateinit var members:ArrayList<Users>
    var totalMembers:Int=0

    constructor(roomname: String, roomKey: String, creator: Users) {
        this.roomname = roomname
        this.roomKey = roomKey
        this.creator = creator
        this.isFull = false
        this.members = ArrayList<Users>(2)
        this.members.add(creator)
        this.totalMembers = this.members.size
    }

    constructor(){
        this.members = ArrayList<Users>(2)
    }


    fun addMember(users: Users):Int {
        for (u in members){
            if(u.userId.equals(users.userId))return 2
        }
        if(this.isFull)return 0
        else{
            this.members.add(users)
            this.totalMembers++
            this.isFull=true
            return 1
        }
    }
}
