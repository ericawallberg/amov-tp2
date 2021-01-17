package pt.isec.a2017014841.tp2.Game

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.common.io.Files.map
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.lang.Thread.sleep
import kotlin.math.tan
import pt.isec.a2017014841.tp2.R

class GameActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private final val START_TIME_IN_MILLIS : Long = 600000

    private lateinit var map: GoogleMap
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val earthRadiusKm: Double = 6372.8
    }
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    //Distancias
    val arrayList : ArrayList<ClientesInfo> = arrayListOf()
    // val J1 = ClientesInfo("j1", Location(40.10790473531666, -8.509329157670718))
    val J1 = ClientesInfo("j1", Location("dummyprovider").apply {
        latitude= 40.10790473531666
        longitude= -8.509329157670718})
    val J2 = ClientesInfo("j2", Location("dummyprovider").apply {
        latitude=40.10801102798203
        longitude = -8.509188078904495})
    val J3 = ClientesInfo("j3", Location("dummyprovider").apply{
        latitude = 40.10808935954506
        longitude = -8.509309461820276}
    )
    val J4 = ClientesInfo("j4", Location("dummyprovider").apply{
        latitude = 40.10803338207712
        longitude = -8.509510885528076}
    )
    val arrayCoords : ArrayList<Location> = arrayListOf()
    val arrayDist : ArrayList<Double> = arrayListOf()
    val arrayAngles : ArrayList<Double> = arrayListOf()

    private var TimerState : Boolean = false
    private lateinit var timer : CountDownTimer
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 3600L

    private var dlg: AlertDialog?=null
    private lateinit var polygon : Polygon


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        arrayList.add(J1)
        arrayList.add(J2)
        arrayList.add(J3)
        arrayList.add(J4)

        startTimer()

        val button = findViewById<Button>(R.id.btleave)
        button.setOnClickListener {leave() }

        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val builder = LatLngBounds.Builder()

        for(i in 0..arrayList.size-1){
            val marker = map.addMarker(MarkerOptions().position(LatLng(arrayList.get(i).coordenadas.latitude,arrayList.get(i).coordenadas.longitude)).title(arrayList.get(i).nome).snippet(String.format("%.3f,%.3f",arrayList.get(i).coordenadas.latitude,arrayList.get(i).coordenadas.latitude)))
            builder.include(marker.position)
        }
        val bounds = builder.build()
        //  map.moveCamera(CameraUpdateFactory.newLocationZoom(arrayList.get(0).coordenadas,40.0f))
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,width, height, 100))

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        buildShapes()
        distancesAndAngles()
        checkVictory()
    }

    private fun buildShapes() {
        for(i in 0..arrayList.size-1)
            arrayCoords.add(arrayList.get(i).coordenadas)
        val circle = map.addCircle(CircleOptions().center(LatLng(arrayList.get(0).coordenadas.latitude, arrayList.get(0).coordenadas.longitude)).radius(100.0).strokeColor(Color.MAGENTA).fillColor(Color.TRANSPARENT))
        var arrayLatLng = ArrayList<LatLng>()
        for(i in 0..arrayList.size-1)
            arrayLatLng.add(LatLng(arrayCoords[i].latitude,arrayCoords[i].longitude))
        polygon = map.addPolygon(PolygonOptions().addAll(arrayLatLng).strokeColor(Color.DKGRAY).fillColor(Color.BLUE))
    }

    private fun distancesAndAngles(){
        //Distances
        for(i in 0..arrayCoords.size-1) {
            if (i == arrayCoords.size - 1)
                arrayDist.add(getDistance(arrayCoords[i], arrayCoords[0]))
            else
                arrayDist.add(getDistance(arrayCoords[i], arrayCoords[i + 1]))
        }

        /*
        for(i in 0..arrayCoords.size-1) {
            arrayDist.add(arrayCoords[i].))
        }
         */
        updateDisAngUI()

    }


    private fun getDistance(v1: Location, v2:Location) : Double{       //harversine formula
        val dLat = Math.toRadians(v1.latitude - v2.latitude);
        val dLon = Math.toRadians(v1.longitude - v2.longitude);
        val originLat = Math.toRadians(v2.latitude);
        val destinationLat = Math.toRadians(v1.latitude);

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLon / 2), 2.toDouble()) * Math.cos(originLat) * Math.cos(destinationLat);
        val c = 2 * Math.asin(Math.sqrt(a));
        return 100*earthRadiusKm * c;
    }

    private fun getAngle(v1: Location, v2: Location): Double{
        // val a1 = Math.toRadian(v1.latitude)
        val a1 = toRadians(v1.latitude)
        val a2 = toRadians(v2.latitude)
        val a1a2 = toRadians(v2.latitude-v1.latitude)

        val x = Math.cos(a1) * Math.sin(a2) - Math.sin(a1) * Math.cos(a2) * Math.cos(a1a2)
        val y = Math.sin(a1a2) * Math.cos(a2)
        val z = Math.atan2(y,x)

        val bearing = toDegrees(z)

        return bearing
    }

    private fun updateDisAngUI(){
        //distances
        val lldistances = findViewById<LinearLayout>(R.id.lldistances)

        for(i in 0..arrayDist.size-1) {
            val textView = TextView(this)
            if(i == arrayCoords.size - 1)
                textView.text = "${arrayList[i].nome} - ${arrayList[0].nome} : ${arrayDist[i]} "
            else
                textView.text = "${arrayList[i].nome} - ${arrayList[i+1].nome} : ${arrayDist[i]} "
            lldistances.addView(textView)
        }

        //angules
        /*
        val llangles = findViewById<LinearLayout>(R.id.llangles)
        for(i in 0..arrayCoords.size-1)
            if(i == arrayCoords.size - 1)
                textView.text = "${arrayList[i].nome} : ${arrayDist[i]} "
            else
                textView.text = "${arrayList[i].nome} : ${arrayDist[i]} "
        lldistances.addView(textView)*/

    }

    private fun leave(){
        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format("TOTAL AREA: "+ "${calculaArea()}")
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this).run {
            setTitle("YOU LEFT THE GAME")
            setView(ll)
            create()
        }

        dlg?.show()
    }

    private fun checkVictory(){
        val limsup = arrayDist[0]+0.1
        val liminf = arrayDist[0]-0.1
        var count : Int =0
        for(i in 0..arrayDist.size-1){
            if(arrayDist[i]>=liminf && arrayDist[i]<=limsup )
                count++;
        }
        if(count==arrayDist.size){
            finished()
        }
    }


    private fun finished(){
        polygon.fillColor = Color.GREEN
        sleep(2000L)
        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format("TOTAL AREA: "+ "${calculaArea()}")
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this).run {
            setTitle("GAME OVER")
            setView(ll)
            create()
        }

        dlg?.show()
        //cria alert
    }

    private fun calculaArea(): Double{
        return (1/2)*getPerimeter()*getApothem()
    }

    private fun getApothem() : Double{
        return (arrayDist.get(0) / (2*tan((180/arrayDist.size)*Math.PI/180)))
    }

    private fun getPerimeter():Double{
        var count: Double = 0.0
        for(i in 0..arrayDist.size-1)
            count+=arrayDist[i]
        return count
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
    }

    override fun onMarkerClick(p0: Marker?)=false


    private fun startTimer(){
        TimerState = true

        timer = object : CountDownTimer(secondsRemaining*1000,1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining/60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished*60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val tvcountdown = findViewById<TextView>(R.id.tvcountdown)
        // tvcountdown.text = "meque"
        tvcountdown.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
    }

    private fun onTimerFinished(){
        TimerState = false
        secondsRemaining = timerLengthSeconds
        updateCountdownUI()
        finished()
    }
}