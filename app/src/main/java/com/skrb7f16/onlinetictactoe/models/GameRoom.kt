package com.skrb7f16.onlinetictactoe.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.IgnoreExtraProperties
import kotlin.properties.Delegates


class GameRoom: Rooms {

    lateinit var gameState:ArrayList<Int>

    var gameActive by Delegates.notNull<Boolean>()
    var counter = 0
    lateinit var message:String
     var currentUser:Int=-1;
     var player1:Users?=null;
     var player2: Users? =null
    constructor(roomname: String, roomKey: String, creator: Users) : super(roomname, roomKey, creator){
        gameState=ArrayList<Int>(9)
        for (i in 0..8){
            gameState.add(2)
        }
        player1= members[0]
        if(totalMembers==2){
            player2=members.get(1)
            gameActive=true
            message="Tap to play"
        }
        else{
            gameActive=false
            message="Waiting for other player to enter"
            player2=null
        }
    }
    constructor() : super(){

    }

}