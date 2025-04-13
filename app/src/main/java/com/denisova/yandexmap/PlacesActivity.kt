package com.denisova.yandexmap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denisova.yandexmap.databinding.ActivityPlacesBinding
import com.denisova.yandexmap.databinding.ItemPlaceBinding

class PlacesActivity : AppCompatActivity() {
    private lateinit var dbHelper: DbHelper
    private lateinit var adapter: PlacesAdapter
    private lateinit var binding: ActivityPlacesBinding
    private var allPlaces: List<Place> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DbHelper(this, null)

        allPlaces = dbHelper.getAllPlaces().sortedBy { it.name }

        setupRecyclerView()
        setupSearch()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlacesAdapter(emptyList()) { place ->
            // При клике на место открываем карту с этим местом
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("PLACE_LAT", place.latitude)
                putExtra("PLACE_LON", place.longitude)
                putExtra("PLACE_NAME", place.name)
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
        }

        binding.placesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.placesRecyclerView.adapter = adapter
    }

//    private fun setupSearch() {
//        binding.searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val query = s.toString()
//                if (query.length >= 2) {
//                    val places = dbHelper.searchPlaces(query)
//                    adapter.updatePlaces(places)
//                } else {
//                    adapter.updatePlaces(emptyList())
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {

                    adapter.updatePlaces(allPlaces)
                } else {
                    val filtered = allPlaces.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.address.contains(query, ignoreCase = true) ||
                                it.category.contains(query, ignoreCase = true)
                    }
                    adapter.updatePlaces(filtered)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}

class PlacesAdapter(
    private var places: List<Place>,
    private val onItemClick: (Place) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.binding.apply {
            placeName.text = place.name
            placeAddress.text = place.address
            placeCategory.text = place.category

            root.setOnClickListener { onItemClick(place) }
        }
    }

    override fun getItemCount() = places.size

    fun updatePlaces(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }
}