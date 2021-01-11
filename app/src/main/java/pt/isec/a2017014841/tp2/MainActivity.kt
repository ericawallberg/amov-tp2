package pt.isec.a2017014841.tp2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.R.*
import android.widget.Toast
import com.github.onikenx.networkandroid.NetUtils
import kotlinx.android.synthetic.main.insert_ip.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!NetUtils.verifyNetworkStateV2(this)){
            Toast.makeText(this,"No network available",Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.btnew_game).setOnClickListener{
            startGame(SERVER_MODE)
        }

        findViewById<Button>(R.id.btjoin_game).setOnClickListener{
            startGame(CLIENT_MODE)
        }

    }

    fun startGame(mode: Int){
        if(mode== CLIENT_MODE) {
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("mode", mode)
            }
            startActivity(intent)
        }
        else{
            val intent = Intent(this, LoadingServerActivity::class.java)
        }
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