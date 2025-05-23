package com.denisova.yandexmap

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.traffic.TrafficLayer
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError


class MainActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener,
    CameraListener, DrivingSession.DrivingRouteListener {
    lateinit var mapView: MapView
    lateinit var trafficImageView: ImageView
    lateinit var trafficLayer: TrafficLayer
    var isTrafficEnabled = false
    lateinit var searchEdit: EditText
    lateinit var searchManager: SearchManager
    lateinit var locationMapKit: UserLocationLayer
    lateinit var searchSession: Session
    private lateinit var accountButton: ImageView
    private lateinit var sharedPref: SharedPreferences
    private var placeMarkers: MapObjectCollection? = null

    private var mapObjects: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null

    private lateinit var exportButton: ImageView


    private val authResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            updateAccountButton()
        }
    }

    private fun submitQuery(query: String) {
        searchSession = searchManager.submit(
            query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions(), this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setLocale("ru_RU")
        MapKitFactory.setApiKey("")
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences("AuthPref", MODE_PRIVATE)
        accountButton = findViewById(R.id.accountButton)
        updateAccountButton()

        accountButton.setOnClickListener {
            if (isUserLoggedIn()) {
                showLogoutDialog()
            } else {
                val intent = Intent(this, AuthActivity::class.java)
                authResultLauncher.launch(intent)
            }
        }

        val statsButton: ImageView = findViewById(R.id.statsButton)
        statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        mapView = findViewById(R.id.mapview)
        mapView.map.mapObjects.clear()
        requestLocationPermission()
        val point = Point(57.155461, 65.535104)
        mapView.map.move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f),
            null
        )
        var mapKit: MapKit = MapKitFactory.getInstance()
        trafficLayer = mapKit.createTrafficLayer(mapView.mapWindow)

        trafficImageView = findViewById(R.id.trafficonoff)

        trafficImageView.setOnClickListener {
            isTrafficEnabled = !isTrafficEnabled

            if (isTrafficEnabled) {
                trafficLayer.isTrafficVisible = true
                trafficImageView.setImageResource(R.drawable.trafficon)
                Log.d("Traffic", "Трафик включен")
            } else {
                trafficLayer.isTrafficVisible = false
                trafficImageView.setImageResource(R.drawable.trafficoff)
                Log.d("Traffic", "Трафик отключен")
            }
        }

        locationMapKit = mapKit.createUserLocationLayer(mapView.mapWindow)
        locationMapKit.isVisible = false
        locationMapKit.setObjectListener(this)

        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapView.map.addCameraListener(this)
        searchEdit = findViewById(R.id.search_edit)
        searchEdit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitQuery(searchEdit.text.toString())
            }
            false
        }

        val homeButton: ImageView = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val homePoint = Point(57.155461, 65.535104)
            mapView.map.move(
                CameraPosition(homePoint, 11.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }

        drivingRouter =
            DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
        mapObjects = mapView.map.mapObjects.addCollection()

        handlePlaceIntent()

        val placesButton: ImageView = findViewById(R.id.placesButton)
        placesButton.setOnClickListener {
            startActivity(Intent(this, PlacesActivity::class.java))
        }

        exportButton = findViewById(R.id.exportButton)
        exportButton.setOnClickListener {
            exportToExcel()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            setIntent(it)
            handlePlaceIntent()
        }
    }

    private fun handlePlaceIntent() {
        intent?.extras?.let { extras ->
            if (extras.containsKey("PLACE_LAT") && extras.containsKey("PLACE_LON")) {
                val lat = extras.getDouble("PLACE_LAT")
                val lon = extras.getDouble("PLACE_LON")
                val name = extras.getString("PLACE_NAME", "")

                placeMarkers?.clear()

                val point = Point(lat, lon)

                mapView.map.move(
                    CameraPosition(point, 19.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 0.5f),
                    null
                )

                mapView.map.mapObjects.addPlacemark(
                    point,
                    ImageProvider.fromResource(this, R.drawable.place_marker),
                    IconStyle().setScale(0.05f)
                )

                Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    private fun updateAccountButton() {
        if (isUserLoggedIn()) {
            accountButton.setImageResource(R.drawable.ic_account_circle_filled)
        } else {
            accountButton.setImageResource(R.drawable.ic_account_circle)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { _, _ ->
                AuthActivity.logout(this)
                updateAccountButton()
                Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationMapKit.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(this, R.drawable.user_arrow),
            IconStyle().setScale(0.1f)
        )
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon(
            "icon", ImageProvider.fromResource(this, R.drawable.search_result),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(0.1f)
        )
        picIcon.setIcon(
            "pin", ImageProvider.fromResource(this, R.drawable.nothing),
            IconStyle().setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE)
                .setZIndex(1f).setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

    override fun onSearchResponse(response: Response) {
        val mapObjects: MapObjectCollection = mapView.map.mapObjects
        mapObjects.clear()
        for (searchResult in response.collection.children) {
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            if (response != null) {
                mapObjects.addPlacemark(
                    resultLocation, ImageProvider.fromResource(this, R.drawable.search_result),
                    IconStyle().setScale(0.05f)
                )
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная ошибка!"
        if (error is RemoteError) {
            errorMessage = "Беспроводная ошибка!"
        } else if (error is NetworkError) {
            errorMessage = "Проблема с интернетом!"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
    }

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            mapObjects!!.addPolyline(route.geometry)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = "Неизвестная ошибка!"
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
    }

    private fun exportToExcel() {
        val dbHelper = DbHelper(this, null)
        val places = dbHelper.getAllPlaces()

        if (places.isEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        val exporter = ExcelExporter(this)
        val file = exporter.exportPlacesToExcel(places)

        if (file != null) {
            Toast.makeText(this, "Файл сохранен: ${file.name}", Toast.LENGTH_LONG).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(
                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.fileprovider",
                        file
                    )
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться файлом"))
        } else {
            Toast.makeText(this, "Ошибка при экспорте", Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            exportToExcel()
        } else {
            Toast.makeText(this, "Для экспорта нужно разрешение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndExport() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            exportToExcel()
        } else {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}