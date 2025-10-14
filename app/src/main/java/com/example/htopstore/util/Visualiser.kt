package com.example.htopstore.util

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlin.random.Random

object Visualiser {

    fun drawPieChart(
        view: PieChart,
        entries: List<PieEntry>,
        title: String,
        showLegend: Boolean = false,
    ) {
        if (entries.isEmpty()) {
            view.clear()
            view.centerText = "No data"
            view.invalidate()
            return
        }

        val colors = entries.map {
            Color.HSVToColor(
                floatArrayOf(Random.nextFloat() * 360f, 0.7f, 0.9f)
            )
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextColor = Color.WHITE
            valueTextSize = 14f
            sliceSpace = 2f
            selectionShift = 6f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(view))
            setValueTextSize(13f)
            setValueTextColor(Color.WHITE)
        }

        view.apply {
            this.data = data
            setUsePercentValues(true)
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)

            // Center text
            centerText = title
            setCenterTextSize(18f)
            setCenterTextColor(Color.DKGRAY)

            // Doughnut style
            isDrawHoleEnabled = true
            holeRadius = 50f
            setHoleColor(Color.TRANSPARENT)
            transparentCircleRadius = 55f

            // Legend
            legend.isEnabled = showLegend
            if (showLegend) {
                legend.apply {
                    isEnabled = true
                    textSize = 14f
                    form = Legend.LegendForm.CIRCLE
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    orientation = Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                    yEntrySpace = 5f
                }
            }

            // Description
            description.text = ""
            description.isEnabled = true

            // Animations
            animateX(1200, Easing.EaseInOutQuad)
            animateY(1200, Easing.EaseInOutQuad)

            invalidate()
        }
    }

    fun drawLineChart(
        view: LineChart,
        sales: List<Float?>,
        profit: List<Float?>,
        labels: List<String>
    ) {
        if (labels.isEmpty()) return

        // نملأ القيم الناقصة بصفر علشان الأطوال تبقى متساوية
        val filledSales = labels.mapIndexed { i, _ -> sales.getOrNull(i) ?: 0f }
        val filledProfit = labels.mapIndexed { i, _ -> profit.getOrNull(i) ?: 0f }

        val salesEntries = filledSales.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val profitEntries =
            filledProfit.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        // Sales Dataset (أزرق - خطي)
        val salesDataSet = LineDataSet(salesEntries, "Sales").apply {
            color = "#2196F3".toColorInt()
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor("#2196F3".toColorInt())
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER // خطي عشان يبقى مختلف
            setDrawFilled(true)
            fillColor = "#2196F3".toColorInt()
            fillAlpha = 30
        }

        // Profit Dataset (أخضر - منحني)
        val profitDataSet = LineDataSet(profitEntries, "Profit").apply {
            color = "#4CAF50".toColorInt()
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor("#4CAF50".toColorInt())
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER // منحني
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillColor = "#4CAF50".toColorInt()
            fillAlpha = 30
        }

        // نضيف الاتنين مع بعض بشكل مظبوط
        val lineData = LineData()
        lineData.addDataSet(salesDataSet)
        lineData.addDataSet(profitDataSet)
        view.data = lineData

        // إعدادات الشارت
        view.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            animateXY(1000, 1000)

            // Legend
            legend.apply {
                isEnabled = true
                textSize = 12f
                textColor = "#757575".toColorInt()
                form = Legend.LegendForm.LINE
                formSize = 12f
                xEntrySpace = 10f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            }

            // X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 11f
                textColor = "#757575".toColorInt()
                setDrawGridLines(false)
                setDrawAxisLine(true)
                axisLineColor = "#E0E0E0".toColorInt()
                axisLineWidth = 1f
                labelRotationAngle = -45f
            }

            // Y Axis
            val maxValue = (filledSales + filledProfit).maxOrNull() ?: 0f
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = maxValue * 1.2f // زيادة 20% للتجميل
                granularity = (maxValue / 5).coerceAtLeast(1f)
                textSize = 11f
                textColor = "#757575".toColorInt()
                setDrawGridLines(true)
                gridColor = "#E0E0E0".toColorInt()
                gridLineWidth = 0.5f
                setDrawAxisLine(false)
                enableGridDashedLine(10f, 10f, 0f)
            }
            axisRight.isEnabled = false
        }

        view.invalidate()
    }
}

