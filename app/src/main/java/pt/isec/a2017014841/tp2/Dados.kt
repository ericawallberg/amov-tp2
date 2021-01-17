package pt.isec.a2017014841.tp2

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap

internal object Dados  {
    const val MIN_USERS = 3
    const val CLIENT_MODE = 1
    const val SERVER_MODE = 0
    const val RC_SIGN_IN = 0
    var nomeDaEquipa = ""
    val JogosCollection = "Jogos"
    var userNumber = 1
    var actualLocation: Location = Location("dummylocation").apply {
        longitude = 20.0
        latitude = 30.0
    }

    val PERMISSION_REQUEST_CODE = 100
    var isServer = false;
    var server_client_mode_text: String = ""

    var playerName: String? = null
    private fun getDb(): FirebaseFirestore {
        return Firebase.firestore
    }

    var teamName: String = ""
    fun onCreateDados(teamName: String, localizations: HashMap<String, String>) {
        val db = getDb()
        //nome da team
//        As coordenadas iniciais (jogador 1), o
//        número de jogadores da equipa e a data/hora de início permitirão a constituição de um
//        identificador único para a equipa, transmitido a todos os seus elementos
        this.teamName = teamName
        db.collection(JogosCollection).document(teamName).set(localizations)
    }



    fun onUpdateDados() {
        val db = Firebase.firestore
        val
                v = db.collection(JogosCollection).document(teamName)
        db.runTransaction { transition ->
            val doc = transition.get(v)
            var geopoint = 2
            Firebase.auth.currentUser?.displayName?.let { transition.update(v, it, geopoint) }
        }
    }

    fun errorDialog(title: String, msg: String, context: Context) {
        AlertDialog.Builder(context).run {
            setTitle(title)
            setMessage(msg)
            setCancelable(true)
            setNeutralButton("Ok") { _: DialogInterface, _: Int -> {} }
        }.show()
    }


//    fun onDeleteDados() {
//        db.collection("Amov").document("e").delete()
//    }

    data class LonLat(var lon : Double,var lat : Double)

    fun stringToLongitureLatitude(valoresString : String): LonLat {
        val valoresTokenizer = StringTokenizer(valoresString)
        val lat = valoresTokenizer.nextToken()
        val log = valoresTokenizer.nextToken()
        return LonLat(log.toDouble(), lat.toDouble())
    }

    fun locationToString(location : Location): String{
        return location.longitude.toString() + " " + location.latitude.toString()
    }
}