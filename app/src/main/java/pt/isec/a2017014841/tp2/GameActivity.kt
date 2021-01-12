package pt.isec.a2017014841.tp2

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.text.Spanned
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider


const val SERVER_MODE = 0
const val CLIENT_MODE = 1
class GameActivity : AppCompatActivity() {
    private var dlg: AlertDialog? = null
    private val model = ViewModelProvider(this).get(GameViewModel::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)


       val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.title = getString(R.string.GameName)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        if (model..value != LoadingServerViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
            when (intent.getIntExtra("mode", SERVER_MODE)) {
                SERVER_MODE -> startAsServer()
                CLIENT_MODE -> startAsClient()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun startAsServer(){
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.VERTICAL
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
                text = String.format(getString(R.string.msg_ip_address),strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
            addView(Button(context).apply {
                setText("SEND IP ADDRESS THROUGH SMS")
                setTextSize(10.0f)
                setEnabled(false)
            })
            addView(Button(context).apply {
                setText("CREATE TEAM")
                setTextSize(10.0f)
                setEnabled(false)
            })
        }
        dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.server_mode))
            setView(ll)
            setOnCancelListener {
                model.stopServer()
                finish()
            }
            create()
        }
        model.startServer()

        dlg?.show()

    }

    private fun startAsClient(){
        val edtBox = EditText(this).apply {
            maxLines = 1
            inputType = TYPE_NUMBER_FLAG_DECIMAL
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
        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.client_mode))
            setMessage(getString(R.string.ask_ip))

            setPositiveButton(getString(R.string.button_connect)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this@GameActivity, getString(R.string.error_address), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    model.startClient(edtBox.text.toString())
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
        }
        dlg.show()
    }

//    const val SERVER_MODE = 0
//    const val CLIENT_MODE = 1
//    const val TAG = "GameActivity"
//
//    class GameActivity : AppCompatActivity() {
//        private lateinit var model: GameViewModel
//        private lateinit var imgRock: ImageView
//        private lateinit var imgPaper: ImageView
//        private lateinit var imgScissors: ImageView
//        private lateinit var tvInfo: TextView
//        private var dlg: AlertDialog? = null
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            setContentView(R.layout.activity_game)
//
//            model = ViewModelProvider(this).get(GameViewModel::class.java)
//            model.state.observe(this) {
//                updateUI()
//            }
//            tvInfo = findViewById(R.id.tvInfo)
//            imgRock = findViewById<ImageView>(R.id.imageRock).apply {
//                setOnClickListener { model.changeMyMove(MOVE_ROCK) }
//            }
//            imgPaper = findViewById<ImageView>(R.id.imagePaper).apply {
//                setOnClickListener { model.changeMyMove(MOVE_PAPER) }
//            }
//            imgScissors = findViewById<ImageView>(R.id.imageScissors).apply {
//                setOnClickListener { model.changeMyMove(MOVE_SCISSORS) }
//            }
//
//            model.connectionState.observe(this) {
//                if (it != GameViewModel.ConnectionState.SETTING_PARAMETERS &&
//                    it != GameViewModel.ConnectionState.SERVER_CONNECTING && dlg?.isShowing == true) {
//                    dlg?.dismiss()
//                    dlg = null
//                }
//
//                if (it == GameViewModel.ConnectionState.CONNECTION_ERROR ||
//                    it == GameViewModel.ConnectionState.CONNECTION_ENDED)
//                    finish()
//            }
//
//            if (model.connectionState.value != GameViewModel.ConnectionState.CONNECTION_ESTABLISHED) {
//                when (intent.getIntExtra("mode", SERVER_MODE)) {
//                    SERVER_MODE -> startAsServer()
//                    CLIENT_MODE -> startAsClient()
//                }
//            }
//        }
//
//        override fun onBackPressed() {
//            super.onBackPressed()
//            //to do: should ask if the user wants to finish
//            model.stopGame()
//        }
//
//        override fun onPause() {
//            super.onPause()
//            dlg?.apply {
//                if (isShowing)
//                    dismiss()
//            }
//        }
//
//        private fun updateUI() {
//            tvInfo.visibility = if (model.myMove == MOVE_NONE) View.VISIBLE else View.INVISIBLE
//            imgRock.visibility = if (model.myMove == MOVE_NONE || model.myMove == MOVE_ROCK) View.VISIBLE else View.INVISIBLE
//            imgPaper.visibility = if (model.myMove == MOVE_NONE || model.myMove == MOVE_PAPER) View.VISIBLE else View.INVISIBLE
//            imgScissors.visibility = if (model.myMove == MOVE_NONE || model.myMove == MOVE_SCISSORS) View.VISIBLE else View.INVISIBLE
//
//            if (model.myMove != MOVE_NONE && model.otherMove != MOVE_NONE)
//                imgRock.postDelayed(5000) {
//                    model.startGame()
//                }
//        }
//
//        private fun startAsServer() {
//            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
//            val ip = wifiManager.connectionInfo.ipAddress
//            val strIPAddress = String.format("%d.%d.%d.%d",
//                ip and 0xff,
//                (ip shr 8) and 0xff,
//                (ip shr 16) and 0xff,
//                (ip shr 24) and 0xff
//            )
//
//            val ll = LinearLayout(this).apply {
//                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                this.setPadding(50, 50, 50, 50)
//                layoutParams = params
//                setBackgroundColor(Color.rgb(240, 224, 208))
//                orientation = LinearLayout.HORIZONTAL
//                addView(ProgressBar(context).apply {
//                    isIndeterminate = true
//                    val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                    paramsPB.gravity = Gravity.CENTER_VERTICAL
//                    layoutParams = paramsPB
//                    indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
//                })
//                addView(TextView(context).apply {
//                    val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//                    layoutParams = paramsTV
//                    text = String.format(getString(R.string.msg_ip_address),strIPAddress)
//                    textSize = 20f
//                    setTextColor(Color.rgb(96, 96, 32))
//                    textAlignment = View.TEXT_ALIGNMENT_CENTER
//                })
//            }
//
//            dlg = AlertDialog.Builder(this).run {
//                setTitle(getString(R.string.server_mode))
//                setView(ll)
//                setOnCancelListener {
//                    model.stopServer()
//                    finish()
//                }
//                create()
//            }
//            model.startServer()
//
//            dlg?.show()
//        }
//
//        private fun startAsClient() {
//            val edtBox = EditText(this).apply {
//                maxLines = 1
//                filters = arrayOf(object : InputFilter {
//                    override fun filter(
//                        source: CharSequence?,
//                        start: Int,
//                        end: Int,
//                        dest: Spanned?,
//                        dstart: Int,
//                        dend: Int
//                    ): CharSequence? {
//                        if (source?.none { it.isDigit() || it == '.' } == true)
//                            return ""
//                        return null
//                    }
//
//                })
//            }
//            val dlg = AlertDialog.Builder(this).run {
//                setTitle(getString(R.string.client_mode))
//                setMessage(getString(R.string.ask_ip))
//                setPositiveButton(getString(R.string.button_connect)) { _: DialogInterface, _: Int ->
//                    val strIP = edtBox.text.toString()
//                    if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
//                        Toast.makeText(this@GameActivity, getString(R.string.error_address), Toast.LENGTH_LONG).show()
//                        finish()
//                    } else {
//                        model.startClient(edtBox.text.toString())
//                    }
//                }
//                setNeutralButton(getString(R.string.btn_emulator)) { _: DialogInterface, _: Int ->
//                    model.startClient("10.0.2.2", SERVER_PORT-1)
//                    // Add port redirect on the Server Emulator:
//                    // telnet localhost <5554|5556|5558|...>
//                    // auth <key>
//                    // redir add tcp:9998:9999
//                }
//                setNegativeButton(getString(R.string.button_cancel)) { _: DialogInterface, _: Int ->
//                    finish()
//                }
//                setCancelable(false)
//                setView(edtBox)
//                create()
//            }
//            dlg.show()
//        }
//
//    }


}