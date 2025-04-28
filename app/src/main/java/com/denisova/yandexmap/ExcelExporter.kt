package com.denisova.yandexmap

import android.content.Context
import android.os.Environment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelExporter(private val context: Context) {
    fun exportPlacesToExcel(places: List<Place>): File? {
        return try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "places_export.xlsx"
            )

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Places")


            val headerRow = sheet.createRow(0)
            listOf(
                "Название",
                "Адрес",
                "Категория",
                "Широта",
                "Долгота",
                "Описание"
            ).forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }


            places.forEachIndexed { rowIndex, place ->
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(place.name)
                row.createCell(1).setCellValue(place.address)
                row.createCell(2).setCellValue(place.category)
                row.createCell(3).setCellValue(place.latitude)
                row.createCell(4).setCellValue(place.longitude)
                row.createCell(5).setCellValue(place.description)
            }



            sheet.setColumnWidth(0, 15 * 256)
            sheet.setColumnWidth(1, 25 * 256)
            sheet.setColumnWidth(2, 15 * 256)
            sheet.setColumnWidth(3, 10 * 256)
            sheet.setColumnWidth(4, 10 * 256)
            sheet.setColumnWidth(5, 30 * 256)


            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}