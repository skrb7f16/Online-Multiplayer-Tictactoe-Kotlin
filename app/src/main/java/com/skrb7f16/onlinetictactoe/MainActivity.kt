package com.skrb7f16.onlinetictactoe



import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skrb7f16.onlinetictactoe.databinding.ActivityMainBinding
import com.skrb7f16.onlinetictactoe.models.GameRoom
import com.skrb7f16.onlinetictactoe.models.Rooms
import com.skrb7f16.onlinetictactoe.models.Users
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    lateinit var win:MediaPlayer
    lateinit var loose:MediaPlayer
    lateinit var click:MediaPlayer
    private lateinit var gameRoom: GameRoom;
    private lateinit var rooms:Rooms
    var binding: ActivityMainBinding? = null
    lateinit var auth: FirebaseAuth
    var database: FirebaseDatabase? = null
    var create = false
    var join = false
    var progressBar: ProgressDialog? = null
    var u: FirebaseUser? = null
    lateinit var users: Users
    lateinit var images:ArrayList<ImageView>
    var  myTurn by Delegates.notNull<Int>();
    var winPositions = arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), intArrayOf(0, 4, 8), intArrayOf(2, 4, 6))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root);
        initialization()
        progressBar= ProgressDialog(this);
        progressBar!!.setMessage("Reseting ");
        progressBar!!.setTitle("Please wait.....");
        //progressBar!!.show()
        database= FirebaseDatabase.getInstance("https://online-tictactoe-46264-default-rtdb.firebaseio.com/");
        auth=FirebaseAuth.getInstance();

        users= auth.currentUser?.displayName?.let { auth.uid?.let { it1 -> Users(it, it1) } }!!
        val action=intent.getStringExtra("action")
        val roomName=intent.getStringExtra("roomname")
        database?.reference?.child("GameRooms")?.child(roomName!!)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gameRoom = snapshot.getValue(GameRoom::class.java)!!
                if (auth.uid == gameRoom.creator.userId) myTurn = 0
                else myTurn = 1
                rearrange()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        


    }



    fun  playerTap(view: View){
        click.start()
        val img = view as ImageView
        val tappedImage = img.tag.toString().toInt()
        if (gameRoom.gameState.get(tappedImage) === 2 && gameRoom.gameActive && gameRoom.currentUser==myTurn) {
            gameRoom.counter++
            if(gameRoom.currentUser==0 && gameRoom.counter<=9){
                gameRoom.gameState[tappedImage]=0
                gameRoom.currentUser=1
                gameRoom.message= gameRoom.members.get(gameRoom.currentUser).username+" turn"

            }
            else if(gameRoom.currentUser==1 && gameRoom.counter<=9){
                gameRoom.gameState[tappedImage]=1
                gameRoom.currentUser=0
                gameRoom.message= gameRoom.members.get(gameRoom.currentUser).username+" turn"

            }
            for (winPosition in winPositions) {
                if (gameRoom.gameState.get(winPosition[0]) === gameRoom.gameState.get(winPosition[1]) &&
                        gameRoom.gameState.get(winPosition[1]) === gameRoom.gameState.get(winPosition[2]) &&
                        gameRoom.gameState.get(winPosition[0]) !== 2) {
                    var winner: String
                    if (gameRoom.gameState.get(winPosition[0]) === 0) {
                        winner=gameRoom.members[0].username+" won"

                    } else {
                        winner = gameRoom.members[1].username+" won"


                    }
                    if(gameRoom.gameState[winPosition[0]] ==myTurn){
                        win.start()
                    }
                    else{
                        loose.start()
                    }
                    gameRoom.message=winner
                    gameRoom.gameActive = false

                }
            }
            if(gameRoom.counter==9)gameRoom.gameActive=false
            database?.reference?.child("GameRooms")?.child(gameRoom.roomname)?.setValue(gameRoom)
        }

        else if(!gameRoom.gameActive && gameRoom.counter!=0){
            reset()
        }
    }

    private fun reset() {

        gameRoom.gameActive=true
        gameRoom.counter=0
        gameRoom.message= gameRoom.player1?.username.toString()+" turn"
        gameRoom.currentUser=0
        for (i in 0..8){
            gameRoom.gameState[i]=2
        }
        database?.reference?.child("GameRooms")?.child(gameRoom.roomname)?.setValue(gameRoom)
    }

    fun rearrange(){
            click.start()
            binding?.status?.setText(gameRoom.message)
            for (i in 0..8){
                if(gameRoom.gameState.get(i)==2)
                images.get(i).setImageResource(0)
                else if(gameRoom.gameState.get(i)==1)
                    images.get(i).setImageResource(R.drawable.o)
                else
                    images.get(i).setImageResource(R.drawable.x)
            }

    }

    private fun initialization(){
        images=ArrayList(9)
        images.add(findViewById(R.id.imageView0))
        images.add(findViewById(R.id.imageView1))
        images.add(findViewById(R.id.imageView2))
        images.add(findViewById(R.id.imageView3))
        images.add(findViewById(R.id.imageView4))
        images.add(findViewById(R.id.imageView5))
        images.add(findViewById(R.id.imageView6))
        images.add(findViewById(R.id.imageView7))
        images.add(findViewById(R.id.imageView8))
        win=MediaPlayer.create(this,R.raw.win)
        loose=MediaPlayer.create(this,R.raw.lose)
        click=MediaPlayer.create(this,R.raw.click)
    }
}