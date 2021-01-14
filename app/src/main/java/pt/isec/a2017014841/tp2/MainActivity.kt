package pt.isec.a2017014841.tp2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.RC_SIGN_IN
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE
import pt.isec.a2017014841.tp2.Dados.mAuth
import pt.isec.a2017014841.tp2.Loading.LoadingActivity
import pt.isec.a2017014841.tp2.helpers.NetUtils


class MainActivity : AppCompatActivity() {


    private val TEST_DB = -1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance()
        //verifica se tem rede
        if (!NetUtils.verifyNetworkStateV2(this)) {
            Toast.makeText(this, getString(R.string.noNetwork), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.btnew_game).setOnClickListener {
            startGame(SERVER_MODE)
        }

        findViewById<Button>(R.id.btjoin_game).setOnClickListener {
            startGame(CLIENT_MODE)
        }

        testdb.setOnClickListener {
            startActivityForResult(Intent(this, TestDB::class.java), TEST_DB)
        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        if (currentUser == null) {
            val TAG = "Authenticação Anonima"
            mAuth?.signInAnonymously()
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = mAuth?.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            if (mAuth?.currentUser == null)
                Toast.makeText(baseContext, "There is no User.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(baseContext, "User logged.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                mAuth = FirebaseAuth.getInstance()
            }
        }
    }

    fun startGame(mode: Int) {
        val intent = Intent(this, LoadingActivity::class.java).apply {
            putExtra("mode", mode)
        }
        startActivity(intent)
    }

}


/*
val btnew_game: Button = findViewById(R.id.btnew_game)
btnew_game.setOnClickListener{
    val intent = Intent(this, NewGameActivity::class.java)
    startActivity(intent)
}

val btjoin_game: Button = findViewById(R.id.btjoin_game)
btjoin_game.setOnClickListener{
    val mDialogView = LayoutInflater.from(this).inflate(R.layout.insert_ip, null);
    val mBuilder = AlertDialog.Builder(this)
        .setView(mDialogView)
        .setTitle("Indique o IP:")

    val mAlertDialog = mBuilder.show()
    mDialogView.ok.setOnClickListener{
        mAlertDialog.dismiss()
        val ip = mDialogView.ip.text.toString()
        val intent = Intent(this, JoinGameActivity::class.java)
        intent.putExtra("Ip", ip)
        startActivity(intent)

    }
    mDialogView.cancel_button.setOnClickListener{
        mAlertDialog.dismiss()
    }
    */