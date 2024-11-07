package com.example.places

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.places.databinding.ActivityMainBinding
import com.example.places.databinding.ActivityMapsBinding
import com.example.places.models.PlacesModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mapsBinding:ActivityMapsBinding?=null
    private var placeModel:PlacesModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsBinding=ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding?.root)

        if(intent.hasExtra(MainActivity.PLACE_DETAILS)){
            placeModel=intent.getSerializableExtra(MainActivity.PLACE_DETAILS) as PlacesModel
        }
        if (placeModel!=null){
            setSupportActionBar(mapsBinding?.mapsToolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=placeModel!!.title
            mapsBinding?.mapsToolbar?.setNavigationOnClickListener { onBackPressed() }
        }
        val supportFragment:SupportMapFragment=supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        supportFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
       val position=LatLng(placeModel!!.latitude,placeModel!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position).title(placeModel!!.location))
        val newPosition=CameraUpdateFactory.newLatLngZoom(position,15f)
        googleMap.animateCamera(newPosition)
    }
}