package pt.isec.a2017014841.tp2.Loading

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.InputType
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_loading_server.*
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE
import pt.isec.a2017014841.tp2.R
import pt.isec.a2017014841.tp2.helpers.NetUtils.getPublicIp

class LoadingActivity : AppCompatActivity() {


    val SERVER_PORT = 9999
    lateinit var strIPAddress: String
    lateinit var model: LoadingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        model = ViewModelProvider(this).get(LoadingViewModel::class.java)

        if (model.connectionState.value != LoadingViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
            when (intent.getIntExtra("mode", SERVER_MODE)) {
                SERVER_MODE -> startAsServer()
                CLIENT_MODE -> startAsClient()
            }
        }


    }

    private fun startAsServer() {
        setContentView(R.layout.activity_loading_server)

        model.nClients.observe(this@LoadingActivity) {
            tvclients_connected.text =
                String.format(
                    "%s %s",
                    getString(R.string.clients_connected),
                    model.nClients.value.toString()
            ***REMOVED***
            if (model.nClients.value!! >= 2) {
                btcreate_team.isEnabled = true
                btcreate_team.isClickable = true
                btcreate_team.setOnClickListener {
                    CreateTeam()
                }
            } else {
                btcreate_team.isEnabled = false
                btcreate_team.isClickable = false
            }

        }

        model.startServer()

        getString(R.string.sasdf)
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        strIPAddress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
    ***REMOVED***

        tvserver_ip.text = String.format("Server IP: %s", getPublicIp())

        btsend_sms.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ***REMOVED*** == PackageManager.PERMISSION_GRANTED
            ***REMOVED*** {
                    SendSMS()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.SEND_SMS),
                        1
                ***REMOVED***
                }
            }
        }
    }

    private fun CreateTeam() {
        //CRIA DOC NA FIREBASE
        //nome do documento: coordenadas iniciais (j1), num jogadores, data/hora
        //MANDA PARA A GAMEACTIVITY
        /*
            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra("mode", CLIENT_MODE)
            }
            startActivity(intent)
            */
    }

    private fun SendSMS() {
        val phoneNo = EditText(this).apply {
            maxLines = 1
            inputType = TYPE_CLASS_NUMBER
        }
        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.msgsend_sms))
            setMessage(getString(R.string.phone_no))
            setPositiveButton(getString(R.string.send_sms)) { _: DialogInterface, _: Int ->
                val numtele = phoneNo.text.toString().trim()
                val ipaddr = strIPAddress.toString().trim()
                try {
                    val smsManager = SmsManager.getDefault()
                    Log.i("Mandar SMS", "numero: $numtele; ipaddr: $ipaddr")
                    //TODO: ask for permission before sending
                    smsManager.sendTextMessage(numtele, null, ipaddr, null, null)
                    Toast.makeText(this@LoadingActivity, "Message sent", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@LoadingActivity,
                        "Failed to send message",
                        Toast.LENGTH_SHORT
                ***REMOVED***.show()
                }
            }
            setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
            }
            setCancelable(true)
            setView(phoneNo)
            create()
        }
        dlg.show()
    }


    private fun startAsClient() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
            ***REMOVED***: CharSequence? {
                    if (source?.none { it.isDigit() || it == '.' } == true)
                        return ""
                    return null
                }

            })
        }
        AlertDialog.Builder(this).run {
            setTitle(getString(R.string.client_mode))
            setMessage(getString(R.string.ask_ip))

            setPositiveButton(getString(R.string.button_connect)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(
                        this@LoadingActivity,
                        getString(R.string.error_address),
                        Toast.LENGTH_LONG
                ***REMOVED***.show()
                    finish()
                } else {
                    try {
                        model.startClient(edtBox.text.toString())
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@LoadingActivity,
                            "Server IP not found",
                            Toast.LENGTH_LONG
                    ***REMOVED***.show()
                        finish()
                    }
                }
            }
//            setNeutralButton(getString(R.string.btn_emulator)) { _: DialogInterface, _: Int ->
//                model.startClient("10.0.2.2", SERVER_PORT-1)
//                // Add port redirect on the Server Emulator:
//                // telnet localhost <5554|5556|5558|...>
//                // auth <key>
//                // redir add tcp:9998:9999
//            }
            setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
                finish()
            }
            setCancelable(false)
            setView(edtBox)
            create()
        }.show()
    }

}