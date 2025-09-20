package com.example.htopstore.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.domain.useCase.localize.NAE.ae
import com.example.domain.util.CartHelper
import com.example.htopstore.databinding.FragmentCartBinding
import com.example.htopstore.util.CartHandler
import com.example.htopstore.util.adapters.CartRecycler
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.ceil
@AndroidEntryPoint
class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var cartHandler: CartHandler
    private var discount = 0
    private var incrementValue = 1
    private var discountValue = 0.0
    private var total = 0.0
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setControllers()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(CartHelper.getAddedTOCartProducts().isEmpty()){
            false.view()
        }
        else{
            cartHandler = CartHandler(CartHelper.getAddedTOCartProducts())
            total = cartHandler.getTheTotalCartPrice()
            onPriceOrDiscountChanged(total,discount)
            binding.recyclerView.adapter = CartRecycler(
                cartHandler.getListOfCartProducts(),
                onDelete = {
                    cartHandler.deleteFromTheCartList(it)
                    CartHelper.removeFromTheCartList(it.id)
                    if (cartHandler.getListOfCartProducts().isEmpty()) {
                        false.view()
                    }
                }
            )
            {
                total = cartHandler.getTheTotalCartPrice()
                onPriceOrDiscountChanged(total, discount)
            }
        }
    }
    private fun setControllers(){
        binding.increaseBtn.setOnClickListener {
            discount += incrementValue
            if(discount > 50) discount = 50
            onPriceOrDiscountChanged(total,discount)
        }
        binding.decreaseBtn.setOnClickListener {
            discount -= incrementValue
            if(discount < 0) discount = 0
            onPriceOrDiscountChanged(total,discount)
        }
        binding.sellNow.setOnClickListener {
            showAlertDialog()
        }
    }
    fun onPriceOrDiscountChanged(newPrice: Double, newDiscount: Int = 0) {
        total = newPrice
        discount = newDiscount
        discountValue = (total * discount) / 100
        // Update the UI or perform any other necessary actions
        binding.totalValue.text = total.ae()
        binding.discountValue.text = discountValue.ae()
        binding.percentageView.text = discount.ae()
        binding.totalAfter.text = ceil(total - discountValue).toInt().ae()
    }

    private fun afterSellOperation(){
        cartHandler.clearCartList()
        false.view()
    }
    private fun showAlertDialog(){
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            message = "Sell all items?",
            title = "Sell Confirmation",
            positiveButton = "Yes",
            negativeButton = "No",
            onConfirm = {
                Log.d("SELL_ERROR", "from cart fragment on Sell Now ${cartHandler.getListOfCartProducts().size}")
                viewModel.sell(
                    cartList = cartHandler.getListOfCartProducts(),
                    discount = discount){
                    Toast.makeText(requireContext(), "Sold Successfully", Toast.LENGTH_SHORT).show()
                    afterSellOperation()
                }
            },
            onCancel = {  }
        )
    }
    private fun Boolean.view() {
        this@CartFragment.binding.operationView.visibility = if(this) View.VISIBLE else View.GONE
        this@CartFragment.binding.hint.visibility = if(!this) View.VISIBLE else View.GONE
    }

}