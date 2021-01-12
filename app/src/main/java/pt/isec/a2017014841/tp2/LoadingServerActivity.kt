package pt.isec.a2017014841.tp2

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.Spanned
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_loading_server.*
import java.lang.Exception
import android.Manifest
import android.os.PersistableBundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider



class LoadingServerActivity : AppCompatActivity() {
    //vai buscar o wifi manager
    val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    //obter ip
    val ip = wifiManager.connectionInfo.ipAddress
//    mete o ip no strIpAddress
    val strIPAddress = String.format("%d.%d.%d.%d",
        ip and 0xff,
        (ip shr 8) and 0xff,
        (ip shr 16) and 0xff,
        (ip shr 24) and 0xff
    )

    //modelo que é criado
    private val model = ViewModelProvider(this).get(LoadingServerViewModel::class.java)
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading_server)

        tvserver_ip.text=String.format("Server IP: %s",strIPAddress)

        btcreate_team.setOnClickListener{
            CreateTeam()
        }

        btsend_sms.setOnClickListener{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                    SendSMS()
                }
                else{
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.SEND_SMS),1)
                }
            }
        }
    }

    fun CreateTeam(){
        //nome do documento: coordenadas iniciais (j1), num jogadores, data/hora


        //cria documento no firebase
    }

    fun SendSMS(){
        val phoneNo = EditText(this).apply {
            maxLines = 1
            inputType = TYPE_CLASS_NUMBER
        }
        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.client_mode))
            setMessage(getString(R.string.ask_ip))
            setPositiveButton(getString(R.string.send_sms)) { _: DialogInterface, _: Int ->
                val numtele = phoneNo.text.toString().trim()
                val ipaddr = strIPAddress.toString().trim()
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(numtele, null, ipaddr, null, null)
                    Toast.makeText(this@LoadingServerActivity, "Message sent", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception){
                    e.printStackTrace()
                    Toast.makeText(this@LoadingServerActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
                finish()
            }
            setCancelable(false)
            setView(phoneNo)
            create()
        }
        dlg.show()
    }
}