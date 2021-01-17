package pt.isec.a2017014841.tp2.Loading

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
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
import android.widget.Toast.makeText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE
import pt.isec.a2017014841.tp2.Dados.isServer
import pt.isec.a2017014841.tp2.Dados.server_client_mode_text

import android.Manifest.permission.SEND_SMS
import android.Manifest.permission.READ_SMS
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.LocationManager
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.type.DateTime
import kotlinx.android.synthetic.main.activity_loading_server.*
import pt.isec.a2017014841.tp2.Dados
import pt.isec.a2017014841.tp2.R
import pt.isec.a2017014841.tp2.Dados.actualLocation
import pt.isec.a2017014841.tp2.Dados.errorDialog
import pt.isec.a2017014841.tp2.Dados.nomeDaEquipa
import pt.isec.a2017014841.tp2.Dados.onCreateDados
import pt.isec.a2017014841.tp2.Game.GameActivity
import java.util.*

class LoadingActivity : AppCompatActivity() {
    val SERVER_PORT = 9999
    lateinit var strIPAddress: String
    lateinit var model: LoadingViewModel
    private var dlg: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProvider(this).get(LoadingViewModel::class.java)
        model.setContext(this)
        model.errorText.observe(this) {

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
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isServer)
            model.stopServer()
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
        Dados.nomeDaEquipa =
            actualLocation.latitude.toString() + actualLocation.longitude.toString() + model.nClients.value.toString() + DateTime.getDefaultInstance()
                .toString()
        Toast.makeText(this, nomeDaEquipa, Toast.LENGTH_LONG).show()

        val mapusers = model.getListOfUsers()
        mapusers[1.toString()]= Dados.locationToString(actualLocation)
        onCreateDados(nomeDaEquipa, mapusers)
        Toast.makeText(this, "TEAM CREATED", Toast.LENGTH_SHORT).show()
       startGame()
    }


    fun onLocationChanged() {

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

    fun waitForServer(){
        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = "Waiting For Server Response"
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this).run {
            setTitle("CLIENT MODE")
            setView(ll)
            create()
        }

        dlg?.show()
    }

    fun onErrorShow(msg: String, context: Context = this@LoadingActivity) {
        errorDialog(server_client_mode_text, msg, context)
    }

}