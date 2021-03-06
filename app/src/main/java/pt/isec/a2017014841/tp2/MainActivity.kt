package pt.isec.a2017014841.tp2

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import pt.isec.a2017014841.tp2.Dados.CLIENT_MODE
import pt.isec.a2017014841.tp2.Dados.PERMISSION_REQUEST_CODE
import pt.isec.a2017014841.tp2.Dados.RC_SIGN_IN
import pt.isec.a2017014841.tp2.Dados.SERVER_MODE
import pt.isec.a2017014841.tp2.Dados.actualLocation
import pt.isec.a2017014841.tp2.Dados.errorDialog
import pt.isec.a2017014841.tp2.Game.GameActivity
import pt.isec.a2017014841.tp2.Loading.LoadingActivity
import pt.isec.a2017014841.tp2.helpers.NetUtils


class MainActivity : AppCompatActivity(), LocationListener {

    private val TEST_DB = -1;
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                INTERNET
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            errorDialog("Ask Permissions", "Accept permissions for the app to work", this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setup permissions


        setContentView(R.layout.activity_main)

        while (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                INTERNET
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_WIFI_STATE
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
                    ACCESS_WIFI_STATE,
                    ACCESS_NETWORK_STATE,
                    INTERNET,
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }

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
            startActivityForResult(Intent(this, GameActivity::class.java), TEST_DB)
        }


    }

    var mAuth: FirebaseAuth? = null
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.

        val currentUser = mAuth!!.currentUser
        if (currentUser == null) {
            val tag = "Authenticação Anonima"
            mAuth?.signInAnonymously()
                ?.addOnCompleteListener(this) { task ->
                    val CollectionGames = "Jogos"
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInAnonymously:success")
                        val user = mAuth?.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInAnonymously:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        if (currentUser != null) {
            Toast.makeText(baseContext, "User logged.", Toast.LENGTH_SHORT).show()
            activateLocationlistener()
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


    override fun onLocationChanged(location: Location) {
        actualLocation = location
        Toast.makeText(this, Dados.locationToString(actualLocation),Toast.LENGTH_LONG).show()
    }

    fun activateLocationlistener() {
        val lm = getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        while (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5f, this)
    }
}

