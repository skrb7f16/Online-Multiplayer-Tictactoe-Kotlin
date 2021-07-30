package com.skrb7f16.onlinetictactoe

import android.app.ActionBar
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.skrb7f16.onlinetictactoe.databinding.ActivityHomePageBinding
import com.skrb7f16.onlinetictactoe.models.GameRoom
import com.skrb7f16.onlinetictactoe.models.Rooms
import com.skrb7f16.onlinetictactoe.models.Users


class HomePageActivity : AppCompatActivity() {
    var binding: ActivityHomePageBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var create = false
    var join = false
    var progressBar: ProgressDialog? = null
    var u: FirebaseUser? = null
    var users: Users? = null
    private lateinit var gameRoom: GameRoom;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        binding=ActivityHomePageBinding.inflate(layoutInflater);
        setContentView(binding!!.getRoot());
        progressBar= ProgressDialog(this);
        progressBar!!.setMessage("Checking ");
        progressBar!!.setTitle("Please wait.....");
        database= FirebaseDatabase.getInstance("https://online-tictactoe-46264-default-rtdb.firebaseio.com/");
        auth=FirebaseAuth.getInstance();
        users= auth!!.currentUser?.displayName?.let { auth!!.uid?.let { it1 -> Users(it, it1) } }
        binding!!.newRoom.setOnClickListener {
            if (!create) {
                binding!!.makeRoomLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            } else {
                binding!!.makeRoomLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            }
            create = !create
        }
        binding!!.joinRoom.setOnClickListener {
            if (!join) {
                binding!!.joinRoomLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
            } else {
                binding!!.joinRoomLinearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            }
            join = !join
        }
        binding!!.create.setOnClickListener(View.OnClickListener {
            progressBar!!.show();
            if(binding!!.roomName.text.length<4 || binding!!.roomKey!!.text.length<4){
                Toast.makeText(this,"Sorry the size of the room name or room key is insuffient",Toast.LENGTH_SHORT).show()
                progressBar!!.hide()
            }
            else{
                val rooms= users?.let { it1 -> Rooms(binding!!.roomName.text.toString(),binding!!.roomKey.text.toString(), it1) }
                if (rooms != null) {
                    database?.reference?.child("Rooms")?.child(rooms.roomname)?.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val room=snapshot.getValue(Rooms::class.java)
                            if(room!=null){
                                progressBar!!.hide()
                                Toast.makeText(this@HomePageActivity,"This room already exists",Toast.LENGTH_SHORT).show()
                                binding!!.roomName.setText("")
                                binding!!.roomKey.setText("")

                            }
                            else{
                                database?.reference?.child("Rooms")?.child(rooms.roomname)?.setValue(rooms)?.addOnSuccessListener {
                                    gameRoom=GameRoom(rooms.roomname,rooms.roomKey,rooms.creator)
                                    database?.reference?.child("GameRooms")?.child(rooms.roomname)?.setValue(gameRoom)?.addOnSuccessListener {
                                        progressBar!!.hide()
                                    }
                                    val intent= Intent(this@HomePageActivity,MainActivity::class.java).apply {
                                        putExtra("roomname",rooms.roomname)
                                        putExtra("action","create")
                                    }
                                    startActivity(intent)

                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                }
            }
        })
        binding!!.join.setOnClickListener {
            progressBar!!.show()
            if(binding!!.roomNameJoin.text.length<4 || binding!!.roomKeyJoin.text.length<4){
                Toast.makeText(this,"Sorry the size of the room name or room key is insuffient",Toast.LENGTH_SHORT).show()
                progressBar!!.hide()
                return@setOnClickListener
            }


                database?.reference?.child("Rooms")?.child(binding!!.roomNameJoin.text.toString())?.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val room=snapshot.getValue(Rooms::class.java)
                        if(room==null){
                            progressBar!!.hide()
                            Toast.makeText(this@HomePageActivity,"This room doesn't  exists",Toast.LENGTH_SHORT).show()
                            binding!!.roomName.setText("")
                            binding!!.roomKey.setText("")

                        }
                        else{
                            if(room.roomKey != binding!!.roomKeyJoin.text.toString()){
                                progressBar!!.hide()
                                Toast.makeText(this@HomePageActivity,"Wrong  room key",Toast.LENGTH_SHORT).show()
                                binding!!.roomName.setText("")
                                binding!!.roomKey.setText("")
                                return
                            }
                            val res= users?.let { it1 -> room.addMember(it1) }
                            if(res==0){
                                progressBar!!.hide()
                                Toast.makeText(this@HomePageActivity,"This room is already full please try different one",Toast.LENGTH_SHORT).show()
                                binding!!.roomName.setText("")
                                binding!!.roomKey.setText("")
                            }
                            else if(res==2){
                                Toast.makeText(this@HomePageActivity,"You are already in the room :)",Toast.LENGTH_SHORT).show()
                                progressBar!!.hide()
                                val intent= Intent(this@HomePageActivity,MainActivity::class.java).apply {
                                    putExtra("roomname",room.roomname)
                                    putExtra("action","again")
                                }
                                startActivity(intent)
                            }
                            else if (res==1){
                                database?.reference?.child("Rooms")!!.child(room.roomname).setValue(room).addOnSuccessListener {

                                    database?.reference?.child("GameRooms")?.child(room.roomname)?.addListenerForSingleValueEvent(object :ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            gameRoom=snapshot.getValue(GameRoom::class.java)!!
                                            val res= users?.let { it1 -> gameRoom.addMember(it1) }
                                            if(res==1||res==2) {
                                                gameRoom.player2 = gameRoom.members[1]
                                                gameRoom.gameActive=true
                                                gameRoom.message= gameRoom.player1?.username.toString()+" turn"
                                                gameRoom.currentUser=0

                                            }
                                            database?.reference?.child("GameRooms")?.child(room.roomname)?.setValue(gameRoom)?.addOnSuccessListener {
                                                progressBar!!.hide()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })

                                    val intent= Intent(this@HomePageActivity,MainActivity::class.java).apply {
                                        putExtra("roomname",room.roomname)
                                        putExtra("action","join")
                                    }
                                    startActivity(intent)
                                }
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }


        binding!!.logout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    }
