package com.example.htopstore.ui.cart

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.htopstore.domain.model.CartProduct
import com.example.htopstore.domain.useCase.CartHandler
import com.example.htopstore.util.CartHelper
import com.example.htopstore.util.Seller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil

class CartViewModel (
    // Inject any use cases or repositories here if needed
) : ViewModel() {

    private lateinit var cartHandler: CartHandler

    // LiveData for UI states
    private val _cartProducts = MutableLiveData<List<CartProduct>>()
    val cartProducts: LiveData<List<CartProduct>> = _cartProducts

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    private val _discount = MutableLiveData<Int>()
    val discount: LiveData<Int> = _discount

    private val _discountValue = MutableLiveData<Double>()
    val discountValue: LiveData<Double> = _discountValue

    private val _totalAfterDiscount = MutableLiveData<Double>()
    val totalAfterDiscount: LiveData<Double> = _totalAfterDiscount

    private val _isCartEmpty = MutableLiveData<Boolean>()
    val isCartEmpty: LiveData<Boolean> = _isCartEmpty

    private val _sellComplete = MutableLiveData<Boolean>()
    val sellComplete: LiveData<Boolean> = _sellComplete

    private var currentDiscount = 0
    private val incrementValue = 1
    private val maxDiscount = 50

    init {
        _discount.value = 0
        initializeCart()
    }

    fun initializeCart() {
        val cartItems = CartHelper.getAddedTOCartProducts()
        if (cartItems.isEmpty()) {
            _isCartEmpty.value = true
        } else {
            _isCartEmpty.value = false
            cartHandler = CartHandler(cartItems)
            _cartProducts.value = cartHandler.getListOfCartProducts()
            updateTotal()
        }
    }

    fun increaseDiscount() {
        currentDiscount += incrementValue
        if (currentDiscount > maxDiscount){
            currentDiscount = maxDiscount}
        _discount.value = currentDiscount
        calculatePriceWithDiscount()
    }

    fun decreaseDiscount() {
        currentDiscount -= incrementValue
        if (currentDiscount < 0) {
            currentDiscount = 0
        }
        _discount.value = currentDiscount
        calculatePriceWithDiscount()
    }

    fun deleteFromCart(cartProduct: CartProduct) {
        if (::cartHandler.isInitialized) {
            cartHandler.deleteFromTheCartList(cartProduct)
            CartHelper.removeFromTheCartList(cartProduct.id)
            _cartProducts.value = cartHandler.getListOfCartProducts()

            if (cartHandler.getListOfCartProducts().isEmpty()) {
                _isCartEmpty.value = true
            } else {
                updateTotal()
            }
        }
    }

    fun onQuantityChanged() {
        if (::cartHandler.isInitialized) {
            updateTotal()
        }
    }

    private fun updateTotal() {
        if (::cartHandler.isInitialized) {
            val newTotal = cartHandler.getTheTotalCartPrice()
            _total.value = newTotal
            calculatePriceWithDiscount()
        }
    }

    private fun calculatePriceWithDiscount() {
        val totalValue = _total.value ?: 0.0
        val discountPercent = _discount.value ?: 0
        val discount = (totalValue * discountPercent) / 100

        _discountValue.value = discount
        _totalAfterDiscount.value = ceil(totalValue - discount)
    }

    fun sellAllItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (::cartHandler.isInitialized) {
                    Log.d("SELL_ERROR", "from cart viewmodel on Sell Now ${cartHandler.getListOfCartProducts().size}")
                    Seller.sellAllItems(
                        context = context,
                        cartList = cartHandler.getListOfCartProducts(),
                        discount = currentDiscount
                    )

                    // Clear cart after successful sale
                    cartHandler.clearCartList()

                    // Update UI on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        _sellComplete.value = true
                        _isCartEmpty.value = true
                        _cartProducts.value = emptyList()
                        currentDiscount = 0
                        _discount.value = 0
                    }
                }
            } catch (e: Exception) {
                Log.e("SELL_ERROR", "Error selling items: ${e.message}")
                // Handle error case if needed
            }
        }
    }

    fun resetSellComplete() {
        _sellComplete.value = false
    }
}