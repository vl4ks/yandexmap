package com.denisova.yandexmap

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "yandexmap", factory, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "email TEXT UNIQUE,"
                + "password TEXT)")
        db?.execSQL(createUsersTable)

        val createPlacesTable = ("CREATE TABLE places ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "address TEXT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "category TEXT,"
                + "description TEXT)")
        db?.execSQL(createPlacesTable)

        fillInitialPlaces(db)
    }

    fun getAllPlaces(): List<Place> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM places", null)

        val places = mutableListOf<Place>()
        while (cursor.moveToNext()) {
            places.add(Place(
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getString(cursor.getColumnIndexOrThrow("description"))
            ))
        }
        cursor.close()
        return places
    }

//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db?.execSQL("DROP TABLE IF EXISTS users")
//        onCreate(db)
//    }
override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    if (oldVersion < 2) {
        db?.execSQL(
            "CREATE TABLE places ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "address TEXT,"
                    + "latitude REAL,"
                    + "longitude REAL,"
                    + "category TEXT,"
                    + "description TEXT)"
        )
        fillInitialPlaces(db)
    }
}
    private fun fillInitialPlaces(db: SQLiteDatabase?) {
        // Примеры организаций в Тюмени
        val places = listOf(
            Place("Тюменский государственный университет", "ул. Семакова, 10", 57.155461, 65.535104, "Образование", "Главный корпус ТюмГУ"),
            Place("Кафе '15/86'", "ул. Ленина, 86", 57.160580, 65.530214, "Кафе", "Уютное кафе в центре"),
            Place("ТРЦ 'Колумб'", "ул. 50 лет Октября, 14", 57.152964, 65.548735, "Торговый центр", "Крупный торговый центр"),
            Place("Парк 'Гилевская роща'", "ул. 50 лет ВЛКСМ", 57.1375, 65.5869, "Парк", "Большой парк для отдыха"),
            Place("Кинотеатр 'Синема Парк'", "ул. Первомайская, 10", 57.1478, 65.5389, "Кинотеатр", "Многозальный кинотеатр")
        )

        places.forEach { place ->
            val values = ContentValues().apply {
                put("name", place.name)
                put("address", place.address)
                put("latitude", place.latitude)
                put("longitude", place.longitude)
                put("category", place.category)
                put("description", place.description)
            }
            db?.insert("places", null, values)
        }
    }

    fun searchPlaces(query: String): List<Place> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM places WHERE name LIKE ? OR address LIKE ? OR category LIKE ?",
            arrayOf("%$query%", "%$query%", "%$query%")
        )

        val places = mutableListOf<Place>()
        while (cursor.moveToNext()) {
            places.add(Place(
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getString(cursor.getColumnIndexOrThrow("description"))
            ))
        }
        cursor.close()
        return places
    }

    fun getPlaceById(id: Int): Place? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM places WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            Place(
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("address")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                cursor.getString(cursor.getColumnIndexOrThrow("category")),
                cursor.getString(cursor.getColumnIndexOrThrow("description"))
            )
        } else {
            null
        }.also { cursor.close() }
    }

//    fun addUser(email: String, password: String): Long {
//        val db = this.writableDatabase
//        val values = ContentValues().apply {
//            put("email", email)
//            put("password", password)
//        }
//        return db.insert("users", null, values)
//    }
//
//    fun checkUser(email: String, password: String): Boolean {
//        val db = this.readableDatabase
//        val query = "SELECT * FROM users WHERE email = ? AND password = ?"
//        val cursor: Cursor = db.rawQuery(query, arrayOf(email, password))
//        return cursor.count > 0
//    }
//
//    fun isUserExists(email: String): Boolean {
//        val db = this.readableDatabase
//        val query = "SELECT * FROM users WHERE email = ?"
//        val cursor: Cursor = db.rawQuery(query, arrayOf(email))
//        return cursor.count > 0
//    }
}
data class Place(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String
)