package com.denisova.yandexmap

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "yandexmap", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "email TEXT UNIQUE,"
                + "password TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun addUser(email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("email", email)
            put("password", password)
        }
        return db.insert("users", null, values)
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE email = ? AND password = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email, password))
        return cursor.count > 0
    }

    fun isUserExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM users WHERE email = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email))
        return cursor.count > 0
    }

}