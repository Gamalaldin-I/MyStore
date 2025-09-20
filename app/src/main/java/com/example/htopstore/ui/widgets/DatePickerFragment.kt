package com.example.htopstore.ui.widgets

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DatePickerFragment(
    private val listener: (day: Int, month: Int, year: Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), { _, y, m, d ->
            listener(d, m + 1, y)
        }, year, month, day)
    }
}