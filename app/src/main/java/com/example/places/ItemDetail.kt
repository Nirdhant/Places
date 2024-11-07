package com.example.places

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.createBitmap
import com.example.places.databinding.ActivityItemDetailBinding
import com.example.places.models.PlacesModel

class ItemDetail : AppCompatActivity() {
    private var itemDetailBinding:ActivityItemDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemDetailBinding=ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(itemDetailBinding?.root)
        var placeModel:PlacesModel?=null
        if (intent.hasExtra(MainActivity.PLACE_DETAILS)){
            placeModel=intent.getSerializableExtra(MainActivity.PLACE_DETAILS)as PlacesModel
        }
        if (placeModel!=null){
            setSupportActionBar(itemDetailBinding?.itemDetailToolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=placeModel.title
            itemDetailBinding?.itemDetailToolbar?.setNavigationOnClickListener { onBackPressedDispatcher }
        }
        itemDetailBinding?.itemDetailImage?.setImageURI(Uri.parse(placeModel?.image))
        itemDetailBinding?.itemDetailLocation?.text=placeModel?.location
        itemDetailBinding?.itemDetailDescription?.text=placeModel?.description
        itemDetailBinding?.itemDetailButton?.setOnClickListener {
            val intent=Intent(this,MapsActivity::class.java)
            intent.putExtra(MainActivity.PLACE_DETAILS,placeModel)
            startActivity(intent)

        }
    }
}