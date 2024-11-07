package com.example.places

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.places.database.DatabaseHandler
import com.example.places.databinding.ActivityAddPlacesBinding
import com.example.places.models.PlacesModel
import com.example.places.utils.AddressFromGeocoder
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Objects
import java.util.UUID

class AddPlaces : AppCompatActivity(), View.OnClickListener{
    private var placesBinding:ActivityAddPlacesBinding?=null   //try using lateinit
    private var cal=Calendar.getInstance()
    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    private var saveImageToStorage:Uri?=null
    private var latitude:Double=0.0
    private var longitude:Double=0.0

    private var placeDetails:PlacesModel?=null
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placesBinding=ActivityAddPlacesBinding.inflate(layoutInflater)
        setContentView(placesBinding?.root)
        setSupportActionBar(placesBinding?.AddPlacesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        placesBinding?.AddPlacesToolbar?.setNavigationOnClickListener{ onBackPressed() }

        if (intent.hasExtra(MainActivity.PLACE_DETAILS)){
            placeDetails=intent.getSerializableExtra(MainActivity.PLACE_DETAILS) as PlacesModel
        }
        //-------------------------------Places----------------------------------------------
        try{
            if(!Places.isInitialized()){
                Places.initialize(applicationContext, getString(R.string.API_KEY)) //this
            }
        }catch (e:Exception){ e.printStackTrace() }

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)
        //-----------------------------------------------------------------------------------

        dateSetListener=DatePickerDialog.OnDateSetListener{view, year, month, dayOfMonth ->

            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDate()
        }
        updateDate()
        if (placeDetails!=null){
            supportActionBar?.title="Edit Place"
            placesBinding?.title?.setText(placeDetails!!.title)
            placesBinding?.description?.setText(placeDetails!!.description)
            placesBinding?.Location?.setText(placeDetails!!.location)
            latitude=placeDetails!!.latitude
            longitude=placeDetails!!.longitude

            saveImageToStorage=Uri.parse(placeDetails!!.image)
            placesBinding?.image?.setImageURI(saveImageToStorage)
            placesBinding?.addPlaceButton?.text="Update"
        }
        placesBinding?.date?.setOnClickListener(this)
        placesBinding?.addImageText?.setOnClickListener(this)
        placesBinding?.addPlaceButton?.setOnClickListener(this)
        placesBinding?.Location?.setOnClickListener(this)
        placesBinding?.getLocationButton?.setOnClickListener(this)
    }

    private fun updateDate(){
        val myFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.getDefault())
        placesBinding?.date?.setText(sdf.format(cal.time).toString())
    }
    private fun isLocationEnabledFroPlaceButton():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    @SuppressLint("MissingPermission")
    private fun requestNewLocationForPlaceButton(){
        var locationRequest= LocationRequest()
        locationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=1000
        locationRequest.numUpdates=1
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.myLooper())
    }
    //why after this only
    private val locationCallBack=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            val lastLocation:Location?=result.lastLocation
            if(lastLocation!=null){
                latitude= lastLocation.latitude
                longitude=lastLocation.longitude
                val address=AddressFromGeocoder(this@AddPlaces,latitude, longitude)
                address.setAddressListener(object :AddressFromGeocoder.AddressListener{
                    override fun onAddressFound(address: String?) {
                        placesBinding?.Location?.setText(address)
                    }
                    override fun onError() {
                        Log.e("Get Address:","Something went wrong")
                    }
                })
                address.getAddress()
            }
        }
    }
    private fun photoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object :MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                shouldShowDialogForPermissionRationale()
            }
        }).onSameThread().check()
    }
    private fun shouldShowDialogForPermissionRationale()
    {
        AlertDialog.Builder(this).setMessage("You Have Turned Off Permission." +
                "It can be enabled under Application Settings").setPositiveButton("Settings"){ _, _->
                    try {
                        val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri=Uri.fromParts("package",packageName,null)
                        intent.data=uri
                        startActivity(intent)
                    }
                    catch (e:ActivityNotFoundException){
                        e.printStackTrace()
                    } }.setNegativeButton("Cancel"){ dialog,_->
                        dialog.dismiss()
                    }.show()
    }
    private fun photoFromCamera()
    {
         Dexter.withActivity(this).withPermissions(
             Manifest.permission.READ_EXTERNAL_STORAGE,
             Manifest.permission.WRITE_EXTERNAL_STORAGE,
             Manifest.permission.CAMERA
             ).withListener(object:MultiplePermissionsListener{
             override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                       if (report!!.areAllPermissionsGranted()){
                           val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                           startActivityForResult(cameraIntent,CAMERA)
                       }
             }
             override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                          shouldShowDialogForPermissionRationale()
             }
             }).onSameThread().check()
    }
    private fun saveImages(imageBitmap:Bitmap):Uri
    {
        val wrapper=ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file= File(file,"${UUID.randomUUID()}.jpeg")
        try {
            val stream:OutputStream=FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }
        catch (e:IOException) { e.printStackTrace() }
        return Uri.parse(file.absolutePath)
    }
    override fun onClick(v: View?) {
        when(v!!.id){
           R.id.date->{
                DatePickerDialog(this@AddPlaces,dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.addImageText->{
                val permissionDialog=AlertDialog.Builder(this)
                permissionDialog.setTitle("Image Source")
                val sourceItems= arrayOf("Photo From Gallery","Take Picture")
                permissionDialog.setItems(sourceItems){_,indexOfItems->
                    when(indexOfItems){
                        0-> photoFromGallery()
                        1-> photoFromCamera()
                    }
                }
                permissionDialog.show()
            }
            R.id.addPlaceButton->{
                when{
                    placesBinding?.title?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter Title",Toast.LENGTH_LONG).show()
                    }
                    placesBinding?.description?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter Description",Toast.LENGTH_LONG).show()
                    }
                    placesBinding?.Location?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter Location",Toast.LENGTH_LONG).show()
                    }
                    saveImageToStorage==null->{
                        Toast.makeText(this,"Please Select An Image",Toast.LENGTH_LONG).show()
                    }
                    else->{
                     val newPlace=PlacesModel(
                         0,
                         placesBinding?.title?.text.toString(),
                         saveImageToStorage.toString(),
                         placesBinding?.description?.text.toString(),
                         placesBinding?.date?.text.toString(),
                         placesBinding?.Location?.text.toString(),
                         latitude,
                         longitude
                     )
                        val dbHandlerClass=DatabaseHandler(this)
                        if (placeDetails==null){
                            val addPlace=dbHandlerClass.addPlaces(newPlace)
                            if (addPlace>0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            else{
                                val updatePlace=dbHandlerClass.updatePlaces(newPlace)
                                if (updatePlace>0){
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
            R.id.getLocationButton->{
                if(!isLocationEnabledFroPlaceButton()){
                    Toast.makeText(this,"Location is turned off",Toast.LENGTH_LONG).show()
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    startActivity(intent)
                }
                else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION).withListener(
                            object :MultiplePermissionsListener{
                                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                    if(report!!.areAllPermissionsGranted()){
                                        requestNewLocationForPlaceButton()
                                    }
                                }
                                override fun onPermissionRationaleShouldBeShown(
                                    permissions: MutableList<PermissionRequest>?,
                                    token: PermissionToken?
                                ) {
                                    shouldShowDialogForPermissionRationale()
                                }
                            }
                        ).onSameThread().check()
                }
            }
            R.id.Location->{
                try {
                    val fields= listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
                    val intent=Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                        .setInitialQuery(placesBinding?.Location?.text?.toString())
                        .build(applicationContext)
                    startActivityForResult(intent, MAPS_AUTOCOMPLETE)
                }
                catch (e:Exception) { e.printStackTrace() }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==GALLERY){
                if(data!=null){
                    val contentUri=data.data
                    try {
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        placesBinding?.image?.setImageBitmap(selectedImageBitmap)
                        saveImageToStorage=saveImages(selectedImageBitmap)
                        Log.e("Saved Image","Path::$saveImageToStorage")
                    }
                    catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this,"Something Went Wrong",Toast.LENGTH_LONG).show()
                    }
                }
            }
            else if(requestCode== CAMERA){
                if(data!=null){
                    val cameraImageBitmap:Bitmap=data.extras!!.get("data") as Bitmap
                    placesBinding?.image?.setImageBitmap(cameraImageBitmap)
                    saveImageToStorage=saveImages(cameraImageBitmap)
                    Log.e("Saved Image Camera","Path::$saveImageToStorage")
                }
            }
            else if(requestCode== MAPS_AUTOCOMPLETE){
                val placeLocation:Place=Autocomplete.getPlaceFromIntent(data!!)
                placesBinding?.Location?.setText(placeLocation.address)
                latitude=placeLocation.latLng!!.latitude
                longitude=placeLocation.latLng!!.longitude
            }
        }
    }


companion object{
    private const val GALLERY=1
    private const val CAMERA=2
    private const val IMAGE_DIRECTORY="PlacesImages"
    private const val MAPS_AUTOCOMPLETE=3
}
}