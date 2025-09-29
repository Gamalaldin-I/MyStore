package com.example.htopstore.util

import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlin.random.Random

object Visualiser {

    fun drawPieChart(
        view: PieChart,
        entries: List<PieEntry>,
        title: String,
        showLegend: Boolean = false
    ) {
        if(entries.isEmpty()){
            view.clear()
            view.centerText = "No data"
            view.invalidate()
            return
        }

        // توليد ألوان ديناميكية لكل شريحة
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
            if(showLegend){
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
}
