package com.example.places.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.places.AddPlaces
import com.example.places.MainActivity
import com.example.places.R
import com.example.places.database.DatabaseHandler
import com.example.places.databinding.EachItemViewBinding
import com.example.places.models.PlacesModel

open class PlacesAdapters(private val context: Context, private var list:ArrayList<PlacesModel>):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener:OnClickListener?=null
    private var eachItemBinding:EachItemViewBinding?=null
    fun setOnClickListenerForRecyclerView(onClickListenerPassed:OnClickListener){
        this.onClickListener=onClickListenerPassed
    }

    interface OnClickListener { fun onClick(position: Int,model: PlacesModel) }

    private class PlacesViewHolder(view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
           return PlacesViewHolder(LayoutInflater.from(context).inflate(R.layout.each_item_view,parent,false))
    }

    override fun getItemCount(): Int { return list.size }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[position]
        if(holder is PlacesViewHolder){
            //EachItemBinding change it to holder.itemView
            eachItemBinding?.circleImage?.setImageURI(Uri.parse(model.image))
            eachItemBinding?.itemTitle?.text=model.title
            eachItemBinding?.itemDescription?.text=model.description
            holder.itemView.setOnClickListener{
                if (onClickListener!=null){ onClickListener!!.onClick(position,model) }
            }
        }
    }
    fun notifyEditItem(activity:Activity,position: Int,requestCode:Int){
        val intent=Intent(context,AddPlaces::class.java)
        intent.putExtra(MainActivity.PLACE_DETAILS,list[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }
    fun removeAt(position: Int){
        val dbHandler=DatabaseHandler(context)
        val isDelete=dbHandler.deletePlace(list[position])
        if(isDelete>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}