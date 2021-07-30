package com.skrb7f16.onlinetictactoe

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skrb7f16.onlinetictactoe.databinding.ActivityLoginBinding
import com.skrb7f16.onlinetictactoe.models.Users
import java.lang.NullPointerException

class LoginActivity : AppCompatActivity() {
    private lateinit var database:FirebaseDatabase;
    private lateinit var auth: FirebaseAuth;
    private lateinit var googleSignInClient: GoogleSignInClient;
    private var RC_SIGN_IN=60;
    private lateinit var binding:ActivityLoginBinding
    private lateinit var firebaseUser:FirebaseUser
    private lateinit var progressDialog:ProgressDialog;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database= FirebaseDatabase.getInstance("https://online-tictactoe-46264-default-rtdb.firebaseio.com/");
        auth= FirebaseAuth.getInstance()
        progressDialog= ProgressDialog(this)
        if (auth.uid!=null){
            val intent=Intent(this,HomePageActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.login.setOnClickListener(View.OnClickListener {
            progressDialog.show()
            signIn()
        })
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("meow", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("meow", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("meow", "signInWithCredential:success")

                    val users= auth.currentUser?.displayName?.let { auth.uid?.let { it1 -> Users(it, it1) } }

                    if (users != null) {
                        checkWithDatabase(users)
                    };

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("meow", "signInWithCredential:failure", task.exception)

                }
            }
    }

    private fun checkWithDatabase(users:Users){
        val intent=Intent(this,HomePageActivity::class.java)
        var u:Users?=null;
        database.reference.child("Users").child(users.userId).addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    u = dataSnapshot.getValue(Users::class.java)!!
                    progressDialog.hide()
                    startActivity(intent)
                    finish()
                }catch (e:NullPointerException){
                    database.reference.child("Users").child(users.userId).setValue(users).addOnCompleteListener(OnCompleteListener {
                        progressDialog.hide()
                    })

                    startActivity(intent)
                    finish()
                }



            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("meow", "loadPost:onCancelled", databaseError.toException())
            }
        })


    }
}