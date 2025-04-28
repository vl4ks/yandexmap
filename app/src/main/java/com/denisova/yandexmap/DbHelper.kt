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
            places.add(
                Place(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description"))
                )
            )
        }
        cursor.close()
        return places
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("DROP TABLE IF EXISTS places")
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
            Place(
                "Тюменский государственный университет ИМиКН",
                "ул. Перекопская, 15А",
                57.159345,
                65.522219,
                "Образование",
                "ИМиКН ТюмГУ"
            ),
            Place(
                "Тюменский государственный университет ИГиП",
                "ул. Ленина, 38",
                57.155030,
                65.532082,
                "Образование",
                "ИГиП ТюмГУ"
            ),
            Place(
                "Тюменский государственный университет Библиотека",
                "ул. Семакова, 18",
                57.158364,
                65.529279,
                "Образование",
                "Библиотека ТюмГУ"
            ),
            Place(
                "Кафе '15/86'",
                "ул. Ленина, 86",
                57.160580,
                65.530214,
                "Кафе",
                "Уютное кафе в центре"
            ),
            Place(
                "Сыроварня",
                "ул. Советская, 54",
                57.156236,
                65.542386,
                "Кафе",
                "Ресторан, магазин сыров"
            ),
            Place("Кофе Брют", "ул. Советская, 55", 57.155279, 65.547946, "Кафе", "Кофейня"),
            Place(
                "Вкусно - и точка",
                "ул. Ленина, 54",
                57.152077,
                65.537472,
                "Кафе",
                "Быстрое питание"
            ),
            Place(
                "ТРЦ 'Вояж'",
                "ул. Герцена, 94",
                57.135043,
                65.493805,
                "Торговый центр",
                "Крупный торгово-развлекательный центр"
            ),
            Place(
                "ТРЦ 'Колумб'",
                "ул. Московский тракт, 118",
                57.135043,
                65.493805,
                "Торговый центр",
                "Крупный торговый центр"
            ),
            Place(
                "ТРЦ 'Кристалл'",
                "ул. Дмитрия Менделеева, 1А",
                57.118068,
                65.546585,
                "Торговый центр",
                "Крупный торгово-развлекательный центр"
            ),
            Place(
                "Парк 'Сквер имени Немцова'",
                "сквер имени Н.М. Немцова",
                57.149513,
                65.545138,
                "Парк",
                "Большой парк для отдыха"
            ),
            Place(
                "Парк 'Цветной бульвар'",
                "Цветной бульвар",
                57.151249,
                65.537605,
                "Парк",
                "Большой парк для отдыха"
            ),
            Place(
                "Парк 'Гилевская роща'",
                "ул. Гилевская роща, 18",
                57.125629,
                65.629456,
                "Парк",
                "Большой парк для отдыха"
            ),
            Place(
                "Кинотеатр 'КАРО'",
                "ул. Московский тракт, 118",
                57.135043,
                65.493805,
                "Кинотеатр",
                "Большой кинотеатр"
            ),
            Place(
                "Кинотеатр 'Премьер'",
                "ул. 50 лет ВЛКСМ, 63",
                57.133985,
                65.561381,
                "Кинотеатр",
                "Большой кинотеатр"
            ),
            Place(
                "Кинотеатр 'Синема Парк'",
                "ул. Максима Горького, 70",
                57.149141,
                65.559307,
                "Кинотеатр",
                "Большой кинотеатр"
            ),
            Place(
                "Супермаркет 'Пятёрочка'",
                "ул. Герцена, 55",
                57.153859,
                65.530411,
                "Супермаркет",
                "Продуктовый магазин"
            ),
            Place(
                "Супермаркет 'Пятёрочка'",
                "ул. Республики, 43",
                57.154386,
                65.538424,
                "Супермаркет",
                "Продуктовый магазин"
            ),
            Place(
                "Супермаркет 'Пятёрочка'",
                "ул. Радищева, 27к1",
                57.142511,
                65.542260,
                "Супермаркет",
                "Продуктовый магазин"
            ),
            Place(
                "Тюменский большой драматический театр",
                "ул. Республики, 129",
                57.144128,
                65.559832,
                "Культура",
                "Драмтеатр"
            ),
            Place(
                "Тюменская филармония",
                "ул. Республики, 34",
                57.156011,
                65.533355,
                "Культура",
                "Филармония"
            ),
            Place(
                "Тюменский театр кукол",
                "ул. Кирова, 36",
                57.157476,
                65.530487,
                "Культура",
                "Театр кукол"
            ),
            Place(
                "Театр Космос",
                "ул. Республики, 165А",
                57.134294,
                65.577530,
                "Культура",
                "Театр Космос"
            ),
            Place(
                "Городская поликлиника № 4",
                "ул. Холодильная, 136",
                57.139155,
                65.556345,
                "Медицина",
                "Поликлиника №4"
            ),
            Place(
                "Городская поликлиника № 3",
                "ул. Ленина, 23С1",
                57.158015,
                65.527913,
                "Медицина",
                "Поликлиника №3"
            ),
            Place(
                "Городская поликлиника № 2",
                "ул. Мельникайте, 75к3",
                57.145304,
                65.583792,
                "Медицина",
                "Поликлиника №2"
            ),
            Place(
                "Мессояханефтегаз",
                "ул. Холодильная, 77",
                57.139643,
                65.559013,
                "Нефтегазовая компания",
                "Нефтегазовая компания"
            ),
            Place(
                "Транснефть-Сибирь",
                "ул. Республики, 139",
                57.141705,
                65.564134,
                "Нефтегазовая компания",
                "Нефтегазовая компания"
            ),
            Place(
                "НТЦ Новатэк",
                "ул. Пожарных и Спасателей, 7",
                57.168108,
                65.598414,
                "Нефтегазовая компания",
                "Научно-технический центр, лабораторно-исследовательский центр"
            ),
            Place(
                "Газпромнефть-Заполярье",
                "ул. 50 лет Октября, 14",
                57.152379,
                65.566802,
                "Нефтегазовая компания",
                "Нефтегазовая компания"
            ),
            Place(
                "Роснефть-Уватнефтегаз",
                "ул. Ленина, 67",
                57.151852,
                65.539295,
                "Нефтегазовая компания",
                "Нефтегазовая компания"
            ),
            Place(
                "Лукойл-Инжиниринг",
                "ул. Республики, 41",
                57.155016,
                65.537445,
                "Нефтегазовая компания",
                "Научно-производственная организация"
            ),
            Place(
                "Ois",
                "ул. Максима Горького, 74",
                57.146306,
                65.556911,
                "IT-компания",
                "ГИС-АСУ проект"
            )

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
            places.add(
                Place(
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description"))
                )
            )
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
}

data class Place(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val description: String
)