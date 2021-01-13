package pt.isec.a2017014841.tp2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import android.R.*
import android.util.Log
import android.widget.Toast
import com.github.onikenx.networkandroid.NetUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.insert_ip.view.*
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //verifica se tem rede
        if(!NetUtils.verifyNetworkStateV2(this)){
            Toast.makeText(this,getString(R.string.noNetwork),Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.btnew_game).setOnClickListener{
            val intent = Intent(this, LoadingServerActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btjoin_game).setOnClickListener{
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
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