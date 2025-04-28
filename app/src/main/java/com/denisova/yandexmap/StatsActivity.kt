package com.denisova.yandexmap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.denisova.yandexmap.databinding.ActivityStatsBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DbHelper(this, null)

        try {
            setupCategoryChart()
            setupBackButton()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при создании графиков: ${e.message}", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    private fun setupCategoryChart() {
        val places = dbHelper.getAllPlaces()
        if (places.isEmpty()) {
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
            return
        }

        val categories = places.groupBy { it.category }.mapValues { it.value.size }

        binding.pieChart.apply {
            data = PieData(
                PieDataSet(
                    categories.map { PieEntry(it.value.toFloat(), it.key) },
                    "Категории мест"
                ).apply {
                    colors = ColorTemplate.COLORFUL_COLORS.toList()
                    valueTextSize = 12f
                }
            )
            description.text = "Распределение по категориям"
            animateY(1000)
            invalidate()
        }

        val categoryList = categories.entries.toList()
        val barEntries = categoryList.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        binding.barChart.apply {
            data = BarData(
                BarDataSet(barEntries, "Количество мест").apply {
                    colors = ColorTemplate.COLORFUL_COLORS.toList()
                    valueTextSize = 12f
                }
            )
            xAxis.valueFormatter = IndexAxisValueFormatter(categories.keys.toList())
            description.text = "Количество мест по категориям"
            animateY(1000)
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}