package com.example.places.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.places.models.PlacesModel

class DatabaseHandler(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION){

    companion object{
        private const val DATABASE_VERSION=1
        private const val DATABASE_NAME="PlacesDatabase"
        private const val TABLE_NAME="PlacesTable"
        //All columns
        private const val KEY_ID="_id"
        private const val KEY_TITLE="title"
        private const val KEY_IMAGE="image"
        private const val KEY_DESCRIPTION="description"
        private const val KEY_DATE="date"
        private const val KEY_LOCATION="location"
        private const val KEY_LATITUDE="latitude"
        private const val KEY_LONGITUDE="longitude"
    }
    override fun onCreate(db: SQLiteDatabase?){
        val placesTable: String = ("CREATE TABLE $TABLE_NAME (" +
                "$KEY_ID INTEGER PRIMARY KEY," +
                "$KEY_TITLE TEXT," +
                "$KEY_IMAGE TEXT," +
                "$KEY_DESCRIPTION TEXT," +
                "$KEY_DATE TEXT," +
                "$KEY_LOCATION TEXT," +
                "$KEY_LATITUDE TEXT," +
                "$KEY_LONGITUDE TEXT)")

        db?.execSQL(placesTable)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addPlaces(newPlace:PlacesModel):Long{
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_TITLE,newPlace.title)
        contentValues.put(KEY_DESCRIPTION,newPlace.description)
        contentValues.put(KEY_IMAGE,newPlace.image)
        contentValues.put(KEY_DATE,newPlace.date)
        contentValues.put(KEY_LOCATION,newPlace.location)
        contentValues.put(KEY_LATITUDE,newPlace.latitude)
        contentValues.put(KEY_LONGITUDE,newPlace.longitude)

        val insertNewPlace=db.insert(TABLE_NAME,null,contentValues)
        db.close()
        return insertNewPlace
    }
    fun updatePlaces(newPlace:PlacesModel):Int{
        val db=this.writableDatabase
        val contentValues=ContentValues()
        contentValues.put(KEY_TITLE,newPlace.title)
        contentValues.put(KEY_IMAGE,newPlace.image)
        contentValues.put(KEY_DESCRIPTION,newPlace.description)
        contentValues.put(KEY_DATE,newPlace.date)
        contentValues.put(KEY_LOCATION,newPlace.location)
        contentValues.put(KEY_LATITUDE,newPlace.latitude)
        contentValues.put(KEY_LONGITUDE,newPlace.longitude)

        val updateResult=db.update(TABLE_NAME,contentValues, KEY_ID +"="+ newPlace.id,null)
        db.close()
        return updateResult
    }
    fun deletePlace(place:PlacesModel):Int{
        val db=this.writableDatabase
        val deleteResult=db.delete(TABLE_NAME, KEY_ID +"="+ place.id,null)
        db.close()
        return deleteResult
    }
    fun getPlaces():ArrayList<PlacesModel>
    {
        val places=ArrayList<PlacesModel>()
        val query="SELECT * FROM $TABLE_NAME"
        val db=this.readableDatabase
        try {
            val cursor: Cursor=db.rawQuery(query,null)
            if (cursor.moveToFirst()){
                do {
                    val place=PlacesModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE))
                    )
                    places.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }
        catch(e:SQLiteException){
            db.execSQL(query)
            return ArrayList()
        }
        return places
    }


}