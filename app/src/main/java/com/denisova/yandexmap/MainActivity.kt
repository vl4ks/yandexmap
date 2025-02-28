package com.denisova.yandexmap

import android.Manifest
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
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

class MainActivity : AppCompatActivity(), UserLocationObjectListener, Session.SearchListener, CameraListener {
    lateinit var mapView: MapView
    lateinit var trafficImageView: ImageView
    lateinit var trafficLayer: TrafficLayer
    var isTrafficEnabled = false
    lateinit var searchEdit:EditText
    lateinit var searchManager: SearchManager
    lateinit var locationMapKit: UserLocationLayer
    lateinit var searchSession: Session


    private fun sumbitQuery(query:String) {
        val searchOptions = SearchOptions().apply {
            geometry = true
            resultPageSize = 10
        }
        searchSession = searchManager.submit(query, VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
            SearchOptions(), this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setLocale("ru_RU")
        MapKitFactory.setApiKey(" ")
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val accountButton: ImageView = findViewById(R.id.accountButton)

        accountButton.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        mapView = findViewById(R.id.mapview)
        requestLocationPermission()
        val point = Point(57.155461, 65.535104)
        mapView.map.move(
            CameraPosition(point, 11.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 10f),
            null
        )
        var mapKit:MapKit = MapKitFactory.getInstance()
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
        locationMapKit.isVisible = true
        locationMapKit.setObjectListener(this)

        SearchFactory.initialize(this)
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapView.map.addCameraListener(this)
        searchEdit = findViewById(R.id.search_edit)
        searchEdit.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sumbitQuery(searchEdit.text.toString())
            }
            false
        }

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
        userLocationView.arrow.setIcon(ImageProvider.fromResource(this, R.drawable.user_arrow),
            IconStyle().setScale(0.1f))
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon("icon", ImageProvider.fromResource(this, R.drawable.search_result),
            IconStyle().setAnchor(PointF(0f, 0f))
                .setRotationType(RotationType.ROTATE).setZIndex(0f).setScale(0.1f)
        )
        picIcon.setIcon("pin", ImageProvider.fromResource(this, R.drawable.nothing),
        IconStyle().setAnchor(PointF(0.5f, 0.5f)).setRotationType(RotationType.ROTATE)
            .setZIndex(1f).setScale(0.5f))
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {

    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

    override fun onSearchResponse(response: Response) {
        val mapObjects:MapObjectCollection = mapView.map.mapObjects
        mapObjects.clear()
        for (searchResult in response.collection.children) {
            val resultLocation = searchResult.obj!!.geometry[0].point!!
            if(response != null) {
                mapObjects.addPlacemark(resultLocation, ImageProvider.fromResource(this, R.drawable.search_result),
                    IconStyle().setScale(0.1f))
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "Неизвестная ошибка!"
        if (error is RemoteError) {
            errorMessage = "Беспроводная ошибка!"
        }
        else if (error is NetworkError) {
            errorMessage = "Проблема с интернетом!"
        }
        Toast.makeText(this,errorMessage, Toast.LENGTH_SHORT).show()

    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {
            sumbitQuery(searchEdit.text.toString())
        }
    }
}