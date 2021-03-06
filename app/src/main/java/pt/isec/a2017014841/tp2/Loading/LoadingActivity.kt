package pt.isec.a2017014841.tp2.Loading

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.text.InputFilter
import android.text.InputType
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.Spanned
import android.text.format.Time
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_loading_server.*
import pt.isec.a2017014841.tp2.Dados
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE
import pt.isec.a2017014841.tp2.Dados.actualLocation
import pt.isec.a2017014841.tp2.Dados.errorDialog
import pt.isec.a2017014841.tp2.Dados.isServer
import pt.isec.a2017014841.tp2.Dados.nomeDaEquipa
import pt.isec.a2017014841.tp2.Dados.server_client_mode_text
import pt.isec.a2017014841.tp2.Game.GameActivity
import pt.isec.a2017014841.tp2.R
import java.util.*

class LoadingActivity : AppCompatActivity(), LocationListener {
    val SERVER_PORT = 9999
    lateinit var strIPAddress: String
    lateinit var model: LoadingViewModel
    private var dlg: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProvider(this).get(LoadingViewModel::class.java)
        model.setContext(this)
        model.errorText.observe(this) {
            while (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.INTERNET
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_WIFI_STATE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    Dados.PERMISSION_REQUEST_CODE
                )
            }
        }
        if (model.connectionState.value != LoadingViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
            when (intent.getIntExtra("mode", SERVER_MODE)) {
                SERVER_MODE -> {
                    isServer = true
                    server_client_mode_text = getString(R.string.server_mode)
                    startAsServer()
                }
                CLIENT_MODE -> {
                    isServer = false
                    server_client_mode_text = getString(R.string.client_mode)
                    startAsClient()
                }
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
                )
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

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        strIPAddress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        tvserver_ip.text = String.format("Server IP: %s", strIPAddress)

        btsend_sms.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    SendSMS()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.SEND_SMS),
                        1
                    )

                }
            }
        }

        model.startServer()


    }


//    override fun onDestroy() {
//        super.onDestroy()
//        if (isServer) {
//            model.serverThread?.interrupt()
//            model.serverThread!! = null
//            model.stopServer()
//        }
//    }

    private fun CreateTeam() {
        val now = Time()
        now.setToNow()
        nomeDaEquipa =
            actualLocation.latitude.toString() + " " +
                    actualLocation.longitude.toString() + " " +
                    model.nClients.value.toString() + " " +
                    now.year + "/" + now.month + "/" + now.monthDay + " " +
                    now.hour + ":" + now.minute + ":" + now.second


        Toast.makeText(this, nomeDaEquipa, Toast.LENGTH_LONG).show()
        model.getListOfUsers()

        // mapusers[1.toString()]= Dados.locationToString(actualLocation)

        Toast.makeText(this, "TEAM CREATED", Toast.LENGTH_SHORT).show()
        startGame()
    }

    override fun onLocationChanged(location: Location) {
        actualLocation = location
        Toast.makeText(this, Dados.locationToString(actualLocation), Toast.LENGTH_LONG).show()
    }

    private fun startGame() {
        //TODO: verificar as locs

        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)

    }


    private fun SendSMS() {
        val phoneNo = EditText(this).apply {
            maxLines = 1
            inputType = TYPE_CLASS_NUMBER
        }
        dlg = AlertDialog.Builder(this).run {
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
                    ).show()
                }
            }
            setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
            }
            setCancelable(true)
            setView(phoneNo)
            create()
        }
        dlg?.show()
    }


    private fun startAsClient() {
        model.clientSucess.observe(this) {
         if(model.clientSucess.value == true){
             model.clientSucess.value = false
             startGame()
         }
        }

        model.clientError.observe(this) {
            if(model.clientError.value == true) {
                model.clientError.value = false
                AlertDialog.Builder(this).run {  //cliente vai fechar because connectionzz
                    setTitle("CONNECTION ERROR")
                    setMessage("CLIENT WILL CLOSE")
                    setPositiveButton("OK") { _: DialogInterface, _: Int ->
                        finish()
                    }
                    setCancelable(false)
                    create()
                }.show()
            }
        }
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
                ): CharSequence? {
                    if (source?.none { it.isDigit() || it == '.' } == true)
                        return ""
                    return null
                }

            })
        }
        AlertDialog.Builder(this).run {
            setTitle(server_client_mode_text)
            setMessage(getString(R.string.ask_ip))
            setPositiveButton(getString(R.string.button_connect)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(
                        this@LoadingActivity,
                        getString(R.string.error_address),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    try {
                        model.startClient(edtBox.text.toString())
                    } catch (_: Exception) {
                        onErrorShow(getString(R.string.serverIpNotFound))
                        finish()
                    }
                    waitForServer()
                }
            }
            setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
                finish()
            }
            setCancelable(false)
            setView(edtBox)
            create()
        }.show()
    }

    fun waitForServer() {
        setContentView(R.layout.wait_for_server)
    }

    fun onErrorShow(msg: String, context: Context = this@LoadingActivity) {
        errorDialog(server_client_mode_text, msg, context)
    }

}