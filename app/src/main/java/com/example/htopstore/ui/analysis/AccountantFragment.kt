package com.example.htopstore.ui.analysis

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.useCase.localize.NAE.digit
import com.example.htopstore.databinding.FragmentAccountantBinding
import com.example.htopstore.ui.widgets.DatePickerFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AccountantFragment : Fragment() {
    private lateinit var binding: FragmentAccountantBinding
    private val vm: AnalysisViewModel by activityViewModels()
    private var from = ""
    private var to = ""
    private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountantBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        binding.accountBrief.root.visibility = View.GONE
        setControllers()
        observe()
        return binding.root
    }
    private fun setControllers(){
        binding.reset.setOnClickListener {
            onReset()
        }
        binding.calculate.setOnClickListener {
            onCalc()
        }
        binding.PickFrom.setOnClickListener {
            val datePicker = DatePickerFragment { day, month, year ->
                from = LocalDate.of(year, month, day).format(displayFormatter)
                binding.PickFrom.text = from
        }
            datePicker.show(parentFragmentManager, "datePicker")
        }
        binding.pickTo.setOnClickListener{
            val datePicker = DatePickerFragment{
                day, month, year ->
                to = LocalDate.of(year, month, day).format(displayFormatter)
                binding.pickTo.text = to
            }
            datePicker.show(parentFragmentManager, "datePicker")
        }

    }

    private fun onReset(){
        val pickTxt = "Pick date"
        binding.pickTo.text = pickTxt
        binding.PickFrom.text = pickTxt
        binding.accountBrief.root.visibility = View.GONE
    }

    private fun onCalc() {
        if(to.isEmpty() or from.isEmpty()){
            showError("Please pick dates")
            return
        }
        if(from>to){
            showError("From date must be before to date")
            return
        }
        vm.getBrief(from,to)
    }
    @SuppressLint("DefaultLocale")
    private fun observe(){
        vm.brief.observe(viewLifecycleOwner){
            val sales = it[0]
            val expenses = it[1]
            val profit = it[2]
            val netProfit = profit - expenses
            val buyingValue = sales - profit
            binding.accountBrief.totalSales.text = sales.digit(1)
            binding.accountBrief.totalExpenses.text = expenses.digit(1)
            binding.accountBrief.totalProfit.text = profit.digit(1)
            binding.accountBrief.ValueOfBuying.text = buyingValue.digit(1)
            binding.accountBrief.netProfit.text = netProfit.digit(1)
            binding.accountBrief.root.visibility = View.VISIBLE
        }
    }



    private fun showError(msg:String){
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


}
