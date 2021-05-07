package com.irvan.smartlaundry

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil

class MapsLaundryActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var modalMapFragment = ModalMapFragment()
    lateinit var bundle: Bundle
    var gpsStatus: Boolean = false

    var lat_user_permanen: Double = -7.0459639
    var lng_user_permanen: Double = 110.3940006
    var lat_destination: Double = 0.0
    var lng_destination: Double = 0.0

    var distanceText = ""
    var distanceValue = 0
    var durationText = ""

    var bitmapMarker: BitmapDrawable? = null
    var bitmapResize: Bitmap? = null
    var bitmap:Bitmap? = null

    lateinit var laundryCollection: ArrayList<Laundry>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_laundry)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Fungsi pencarian laundry
        searchLaundry()

        //Fungsi ketika lokasi diaktifkan
        locationEnabled()

        //bundle passing fragment
        bundle = Bundle()

        //Laundry
        laundryCollection = ArrayList<Laundry>()
        addData()

        //Kondisi GPS on / off
        if (gpsStatus) {
            Toast.makeText(this@MapsLaundryActivity, "GPS ON", Toast.LENGTH_LONG).show()

            //Mendapatkan hasil lokasi secara sekarang ini
            getLocationAccess()
        } else {
            Toast.makeText(this@MapsLaundryActivity, "Silahkan Nyalakan GPS", Toast.LENGTH_LONG)
                .show()
        }

    }

    private fun searchLaundry() {

        //Initial variabel
        val search = findViewById<SearchView>(R.id.searchLaundry)
        val listView = findViewById<ListView>(R.id.listViewSearch)

        //Menyiapkan variabel
        val names = arrayOf("Aananda Laundry", "Bimo Laundry", "Cilo Laundry")

        //Adapter list view
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, names
        )

        listView.adapter = adapter

        //opsi menampilkan list view
        listView.isVisible = false

        //Ketika search view di click
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                //opsi menampilkan list view
                listView.isVisible = true

                search.clearFocus()

                //kondisi hasil pencarian
                if (names.contains(p0)) {
                    adapter.filter.filter(p0)
                } else {
                    Toast.makeText(applicationContext, "Item tidak ada", Toast.LENGTH_LONG).show()
                }

                return false
            }

            //ketika seacrh view change text
            override fun onQueryTextChange(p0: String?): Boolean {
                //listview show
                listView.isVisible = true

                adapter.filter.filter(p0)
                return false
            }


        })

        // Search view close
        search.setOnCloseListener(SearchView.OnCloseListener {
            listView.isVisible = false
            false
        })

    }

    private fun locationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocationAccess() {

        //Kondisi akse perizinan lokasi
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            var manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //Mendapatkan lokasi sekarang ini
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                lat_user_permanen = location!!.latitude
                lng_user_permanen = location!!.longitude

            }

            var listener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {
                    val pospasien = LatLng(p0.latitude, p0.longitude)
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pospasien,16f))
                }

                override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

                }

                override fun onProviderEnabled(p0: String?) {

                }

                override fun onProviderDisabled(p0: String?) {

                }
            }

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, listener)


        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }

    fun myLocationEnabled() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings.isCompassEnabled = true

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        myLocationEnabled()

        val height = 200
        val width = 170
        bitmapMarker = resources.getDrawable(R.mipmap.laundrybaruforeground) as BitmapDrawable
        val bitmap: Bitmap = bitmapMarker!!.getBitmap()
        bitmapResize = Bitmap.createScaledBitmap(bitmap, width, height, false)

        for(item in laundryCollection){

            //Add a marker in Sydney and move the camera
            lat_destination = item.lat
            lng_destination = item.long
//            val latLongLaundry = LatLng(lat_destination, lng_destination)
            map.addMarker(
                MarkerOptions().position(LatLng(item.lat, item.long)).title(item.name).icon(
                    BitmapDescriptorFactory.fromBitmap(
                       bitmapResize
                    )
                )
            )

            map.setOnMarkerClickListener {

                if(it.equals(""))
//                map.clear()
                if (lat_user_permanen == 0.0 && lng_user_permanen == 0.0) {
                    Toast.makeText(
                        this@MapsLaundryActivity,
                        "Silahkan Nyalakan GPS terlebih dahulu",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    getDirection(
                        lat_user_permanen, lng_user_permanen, lat_destination, lng_destination
                    )
                }

                bundle.putString("nama", item.name)
                bundle.putString("opsi1", item.opsi1)
                bundle.putString("opsi2", item.opsi2)
                bundle.putString("opsi3", item.opsi3)
                bundle.putString("harga1", item.harga1)
                bundle.putString("harga2", item.harga2)
                bundle.putString("harga3", item.harga3)
                bundle.putString("durasi1", item.durasi1)
                bundle.putString("durasi2", item.durasi2)
                bundle.putString("durasi3", item.durasi3)
                bundle.putInt("cover", item.img)
                modalMapFragment.arguments = bundle
                modalMapFragment.show(supportFragmentManager, "BottomSheetDialog")
                return@setOnMarkerClickListener false
            }

            val zoomLevel = 16.0f //This goes up to 21
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.lat, item.long), zoomLevel))

        }

    }

    fun getDirection(
        lat_user_permanen: Double,
        lng_user_permanen: Double,
        lat_destination: Double,
        lng_destination: Double
    ) {

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat_user_permanen + "," + lng_user_permanen + "&destination=" + lat_destination + "," + lng_destination + "&key=AIzaSyDs91q4g9okkhZweA86z-QbQIOp1Xdil6g"
        Log.e("error GPS", urlDirections)
        //   val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=-6.969861,110.434915&destination=-7.0563254,110.3982569&key=AIzaSyDs91q4g9okkhZweA86z-QbQIOp1Xdil6g"

        //Log.d("serverGoogleMap",urlDirections)

        var rq = Volley.newRequestQueue(this)
        val sr = JsonObjectRequest(
            Request.Method.GET, urlDirections, null, Response.Listener { response ->
                val routes = response.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    map.addPolyline(PolylineOptions().addAll(path[i]).color(Color.BLUE))
                }

                distanceText = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                distanceValue = legs.getJSONObject(0).getJSONObject("distance").getInt("value")
                durationText = legs.getJSONObject(0).getJSONObject("duration").getString("text")
                //durationValue = legs.getJSONObject(0).getJSONObject("duration").getString("value")

            },
            Response.ErrorListener { error ->
                //Toast.makeText(this, error.message,Toast.LENGTH_LONG).show()
                Log.d("error query", error.message)
            })

        rq.add(sr)
    }

    public fun addData() {

        laundryCollection.add(
            Laundry(
                "Aby Laundry",
                "Standart",
                "One Day",
                "Kilat",
                "Rp. 5.000/1kg",
                "Rp. 7.000/1kg",
                "Rp. 8.000/1kg",
                "2-3 Hari",
                "24 jam/1 hari",
                "4 jam",
                R.drawable.aby_laundry,
                -7.054838977849363,
                110.39487326768356
            )
        )

        laundryCollection.add(
            Laundry(
                "Bunda Laundry",
                "Standart", "Cuci Kering", "",
                "Rp. 5.000/1kg", "", "",
                "3 Hari", "", "", R.drawable.bunda_laundry, -7.045911032725791, 110.39376189734186
            )
        )

        laundryCollection.add(
            Laundry(
                "Graha Laundry",
                "Standart",
                "Kilat",
                "Ekspress",
                "Rp. 4.500/1kg",
                "Rp. 6.000/1kg",
                "Rp. 8.000/1kg",
                "2 Hari",
                "1 hari",
                "< 1 hari",
                R.drawable.graha_laundry,
                -7.06248797263869,
                110.39670200882114
            )
        )

        laundryCollection.add(
            Laundry(
                "Green Wash Laundry ",
                "Standart",
                "One Day",
                "Kilat",
                "Rp. 4.000/1kg",
                "Rp. 6.000/1kg",
                "Rp. 8.000/1kg",
                "2 Hari",
                "24 jam",
                "2-3 jam",
                R.drawable.green_wash,
                -7.056789273082317,
                110.39753343011496
            )
        )

        laundryCollection.add(
            Laundry(
                "Nakula Laundry",
                "Standart",
                "",
                "",
                "Rp. 4.000/1kg",
                "",
                "",
                "2-3 Hari",
                "",
                "",
                R.drawable.nakula_laundry,
                -7.043907609369913,
                110.39408261855873
            )
        )

        laundryCollection.add(
            Laundry(
                "Rahma Laundry",
                "Standart",
                "One Day",
                "Express",
                "Rp. 5.000/1kg",
                "Rp. 8.000/1kg",
                "Rp. 10.000/1kg",
                "3 Hari",
                "24 jam/1 hari",
                "3 jam",
                R.drawable.rahma_laundry,
                -7.0565829627780765,
                110.39823379640279
            )
        )

        laundryCollection.add(
            Laundry(
                "Ranee Laundry",
                "Standart",
                "One Day",
                "Express",
                "Rp. 5.000/1kg",
                "Rp. 8.500/1kg",
                "Rp. 10.500/1kg",
                "2 Hari",
                "24 jam/1 hari",
                "5 jam",
                R.drawable.rahma_laundry,
                -7.053792266424189,
                110.39697255461819
            )
        )

        laundryCollection.add(
            Laundry(
                "Sunday Laundry",
                "Standart",
                "Cuci Kering",
                "",
                "Rp. 5.000/1kg",
                "Rp. 4.000",
                "",
                "2 Hari",
                "1 hari",
                "",
                R.drawable.sunday_laundry,
                -7.060663694612196,
                110.39716352397978
            )
        )

        laundryCollection.add(
            Laundry(
                "Tirtana Laundry",
                "Standart",
                "Kilat",
                "Ekspress",
                "Rp. 4.000/1kg",
                "Rp. 8.000/1kg",
                "Rp. 10.000/1kg",
                "2 Hari",
                "1 hari",
                "4 jam",
                R.drawable.tirtana_laundry,
                -7.0563002577717375,
                110.39827981267997
            )
        )

        laundryCollection.add(
            Laundry(
                "Zara Laundry",
                "Standart",
                "Kilat",
                "",
                "Rp. 4.500/1kg",
                "Rp. 10.000/1kg",
                "",
                "3-4 Hari",
                "4 jam/1 hari",
                "",
                R.drawable.zara_laundry,
                -7.057640674231844,
                110.39706886448303
            )
        )
    }

    override fun onResume() {
        super.onResume()
        map.clear()
    }

}
