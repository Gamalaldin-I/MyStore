package com.example.htopstore.ui.main

import android.content.Intent
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
import com.example.htopstore.ui.scan.ScanActivity
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupUI()
        setControllers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshCart()
    }

    private fun setupUI() {
        // Initial setup if needed
    }

    private fun refreshCart() {
        if (CartHelper.getAddedTOCartProducts().isEmpty()) {
            showEmptyState()
        } else {
            showCartContent()
        }
    }

    private fun showCartContent() {
        cartHandler = CartHandler(CartHelper.getAddedTOCartProducts())
        total = cartHandler.getTheTotalCartPrice()

        // Reset discount when cart is refreshed
        discount = 0
        onPriceOrDiscountChanged(total, discount)

        // Setup RecyclerView adapter
        binding.recyclerView.adapter = CartRecycler(
            cartHandler.getListOfCartProducts(),
            onDelete = { product ->
                cartHandler.deleteFromTheCartList(product)
                CartHelper.removeFromTheCartList(product.id)

                // Recalculate total
                total = cartHandler.getTheTotalCartPrice()
                onPriceOrDiscountChanged(total, discount)

                // Check if cart is empty
                if (cartHandler.getListOfCartProducts().isEmpty()) {
                    showEmptyState()
                }
            },
            onIncOrDec = {
                // Recalculate when quantity changes
                total = cartHandler.getTheTotalCartPrice()
                onPriceOrDiscountChanged(total, discount)
            }
        )

        // Show cart content, hide empty state
        binding.operationView.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.operationView.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE

        // Reset values
        discount = 0
        total = 0.0
        discountValue = 0.0
    }

    private fun setControllers() {
        // Increase discount button
        binding.increaseBtn.setOnClickListener {
            discount += incrementValue
            if (discount > 50) discount = 50
            onPriceOrDiscountChanged(total, discount)

            // Animate button
            animateButton(binding.increaseBtn)
        }

        // Decrease discount button
        binding.decreaseBtn.setOnClickListener {
            discount -= incrementValue
            if (discount < 0) discount = 0
            onPriceOrDiscountChanged(total, discount)

            // Animate button
            animateButton(binding.decreaseBtn)
        }

        // Complete sale button
        binding.sellNow.setOnClickListener {
            if (::cartHandler.isInitialized && cartHandler.getListOfCartProducts().isNotEmpty()) {
                showAlertDialog()
            } else {
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear cart button
        binding.clearCartBtn.setOnClickListener {
            if (::cartHandler.isInitialized && cartHandler.getListOfCartProducts().isNotEmpty()) {
                showClearCartDialog()
            }
        }

        // Scan product button (from empty state)
        binding.scanProductBtn.setOnClickListener {
            // Navigate to scan activity or main fragment
            // You can implement this based on your navigation setup
            Toast.makeText(requireContext(), "Opening scanner...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), ScanActivity::class.java))
        }
    }

    private fun onPriceOrDiscountChanged(newPrice: Double, newDiscount: Int = 0) {
        total = newPrice
        discount = newDiscount
        discountValue = (total * discount) / 100

        // Update all price displays
        binding.totalValue.text = total.ae()
        binding.discountValue.text = discountValue.ae()
        binding.percentageView.text = discount.toString()
        binding.totalAfter.text = ceil(total - discountValue).toInt().ae()
    }

    private fun afterSellOperation() {
        cartHandler.clearCartList()
        CartHelper.clearCartList()
        showEmptyState()
    }

    private fun showAlertDialog() {
        val totalAmount = ceil(total - discountValue).toInt()
        val itemCount = cartHandler.getListOfCartProducts().size

        DialogBuilder.showAlertDialog(
            context = requireContext(),
            message = "Complete sale of $itemCount item(s) for ${totalAmount.ae()} LE?",
            title = "Complete Sale",
            positiveButton = "Confirm",
            negativeButton = "Cancel",
            onConfirm = {
                Log.d("SELL_OPERATION", "Selling ${cartHandler.getListOfCartProducts().size} items")
                viewModel.sell(
                    cartList = cartHandler.getListOfCartProducts(),
                    discount = discount
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Sale completed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    afterSellOperation()
                }
            },
            onCancel = { }
        )
    }

    private fun showClearCartDialog() {
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            message = "Are you sure you want to clear all items from the cart?",
            title = "Clear Cart",
            positiveButton = "Clear",
            negativeButton = "Cancel",
            onConfirm = {
                afterSellOperation()
                Toast.makeText(
                    requireContext(),
                    "Cart cleared",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCancel = { }
        )
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up if needed
    }
}