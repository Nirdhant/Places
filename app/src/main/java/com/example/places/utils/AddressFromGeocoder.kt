package com.example.places.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.Locale

class AddressFromGeocoder(context:Context,private val latitude:Double,private val longitude:Double):AsyncTask<Void,String,String>(){
    private val geocoder:Geocoder= Geocoder(context, Locale.getDefault())
    private lateinit var addressListener:AddressListener

    fun setAddressListener(newAddressListener: AddressListener){addressListener=newAddressListener}
    fun getAddress(){execute()}
    override fun doInBackground(vararg params: Void?): String {
        val addressList:List<Address>?=geocoder.getFromLocation(latitude,longitude,1)
        if (!addressList.isNullOrEmpty()){
            try {
                val address:Address=addressList[0]
                val sb=StringBuilder()
                for (i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append("")
                }
                sb.deleteCharAt(sb.length-1)
                return sb.toString()
            }
            catch (e:Exception) { e.printStackTrace() }
        }
        return ""
    }

    override fun onPostExecute(result: String?) {
        if (result==null)
            addressListener.onError()
        else
            addressListener.onAddressFound(result)
        super.onPostExecute(result)
    }
    interface AddressListener{
        fun onAddressFound(address:String?)
        fun onError()
    }
}


