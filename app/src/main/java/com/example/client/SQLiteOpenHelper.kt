package com.example.client

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TYPE_COL + " TEXT," +
                CONTENT_COL + " TEXT," +
                TIMESTAMP_COL + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // add message row to the database
    fun addMessage(type: String, content: String, timestamp: String){
        // values to insert into the database
        val values = ContentValues()

        // insert the values in the form of key-value pairs
        values.put(TYPE_COL, type)
        values.put(CONTENT_COL, content)
        values.put(TIMESTAMP_COL, timestamp)


        // writable
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(TABLE_NAME, null, values)

        db.close()
    }

    // get all data from messages database
    fun getName(): Cursor? {

        val db = this.readableDatabase

        // read data from database and return cursor
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)

    }

    companion object {
        // variables for our database
        private const val DATABASE_NAME = "CLIENT_DATABASE"

        // below is the variable for database version
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "messages"

        // column names
        const val ID_COL = "id"
        const val TYPE_COL = "type"
        const val CONTENT_COL = "content"
        const val TIMESTAMP_COL = "timestamp"
    }
}
