package pt.isec.a2017014841.tp2

import android.os.Bundle
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.android.synthetic.main.activity_test_d_b.*
import pt.isec.a2017014841.tp2.Dados.JogosCollection

class TestDB : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_d_b)

    //        (1..100)
//            .forEach { i ->
//                textview = TextView(this)
//                textview.setText("texto $i")
//                activitydb_layout.addView(textview)
//            }

    }


    override fun onStart() {
        super.onStart()
        criarbase_button.setOnClickListener{ onCreateDados() }
        updatebase_button.setOnClickListener{ onUpdateDados()}
        deletebase_button.setOnClickListener{onDeleteDados()}
    }

    fun addview(text : String){
        activitydb_layout.addView(TextView(this).apply {setText(text)})
    }

    var dadoN = 1;
    val db = Firebase.firestore
    fun onCreateDados(){

        val scores = hashMapOf(
                "nossa" to 0,
                "caralho" to 0
        )
        db.collection(JogosCollection).document("e").set(scores)
    }

    fun onUpdateDados(){
        val db = Firebase.firestore
        val
                v = db.collection("").document("Level1")
        db.runTransaction {transition ->
            val doc = transition.get(v)
            var geopoint = 2
            transition.update(v,"nrgames",geopoint)

            null
        }
    }
    fun onDeleteDados(){
        db.collection("Amov").document("e").delete()
    }
}