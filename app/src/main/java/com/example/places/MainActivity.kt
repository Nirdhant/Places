package com.example.places

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.places.adapters.PlacesAdapters
import com.example.places.database.DatabaseHandler
import com.example.places.databinding.ActivityMainBinding
import com.example.places.models.PlacesModel
import com.example.places.utils.SwipeToDeleteCallBack
import com.example.places.utils.SwipeToEditCallBack

class MainActivity : AppCompatActivity() {
     private var binding:ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.addButton?.setOnClickListener {
            val intent=Intent(this,AddPlaces::class.java)
            startActivityForResult(intent, PLACE_ACTIVITY_REQUEST_CODE)
        }
        getAllPlaces()
    }
    private fun getAllPlaces()
    {
        val dbHandler=DatabaseHandler(this)
        val getAllPlaces:ArrayList<PlacesModel> =dbHandler.getPlaces()
        if(getAllPlaces.size>0){
        binding?.recyclerView?.visibility=View.VISIBLE
        binding?.noPlaceText?.visibility=View.GONE
        placesRecyclerViewSetUp(getAllPlaces)
        }
        else{
         binding?.recyclerView?.visibility=View.GONE
         binding?.noPlaceText?.visibility=View.VISIBLE
        }
    }
    private fun placesRecyclerViewSetUp(placesList:ArrayList<PlacesModel>)
    {
        //---------------For Adapter---------------------------------------
        binding?.recyclerView?.layoutManager=LinearLayoutManager(this)
        binding?.recyclerView?.setHasFixedSize(true)
        val placesAdapter=PlacesAdapters(this,placesList)
        binding?.recyclerView?.adapter=placesAdapter
        //--------------------For Click Listener---------------------------
        placesAdapter.setOnClickListenerForRecyclerView(object: PlacesAdapters.OnClickListener{
            override fun onClick(position: Int, model: PlacesModel) {
                val intent=Intent(this@MainActivity,ItemDetail::class.java)
                intent.putExtra(PLACE_DETAILS,model)
                startActivity(intent)
            }
        })
        //------------------For Edit Item----------------------------------
        val editSwipeHandler=object:SwipeToEditCallBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
              val adapter=binding?.recyclerView?.adapter as PlacesAdapters
              adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper=ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.recyclerView)
       //-------------------For Delete Item-------------------------------
       val deleteSwipeHandler=object:SwipeToDeleteCallBack(this){
           override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               val adapter=binding?.recyclerView?.adapter as PlacesAdapters
               adapter.removeAt(viewHolder.adapterPosition)
               getAllPlaces()          //try removing this
           }
       }
        val deleteItemTouchHelper=ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.recyclerView)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode==Activity.RESULT_OK){ getAllPlaces() }
            else { Log.e("Activity","Cancelled Or Back Pressed") }
        }
    }
    companion object{
        var PLACE_ACTIVITY_REQUEST_CODE=1
        var PLACE_DETAILS="ITEM_DETAILS"
    }
}
