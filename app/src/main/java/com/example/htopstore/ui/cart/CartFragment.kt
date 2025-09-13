package com.example.htopstore.ui.cart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.htopstore.databinding.FragmentCartBinding
import com.example.htopstore.util.DialogBuilder
import com.example.htopstore.util.NAE.ae
import com.example.htopstore.util.adapters.CartRecycler

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartRecycler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupUI()
        setupObservers()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.initializeCart()
    }

    private fun setupUI() {
        setupControllers()
        setupRecyclerView()
    }

    private fun setupControllers() {
        binding.increaseBtn.setOnClickListener {
            viewModel.increaseDiscount()
        }

        binding.decreaseBtn.setOnClickListener {
            viewModel.decreaseDiscount()
        }

        binding.sellNow.setOnClickListener {
            showSellConfirmationDialog()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartRecycler(
            arrayListOf(),
            onDelete = { cartProduct ->
                viewModel.deleteFromCart(cartProduct)
            }
        ) {
            // On quantity changed callback
            viewModel.onQuantityChanged()
        }
        binding.recyclerView.adapter = cartAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.cartProducts.observe(viewLifecycleOwner) { products ->
            cartAdapter.updateData(products)
        }

        viewModel.isCartEmpty.observe(viewLifecycleOwner) { isEmpty ->
            updateViewVisibility(!isEmpty)
        }

        viewModel.total.observe(viewLifecycleOwner) { total ->
            binding.totalValue.text = total.ae()
        }

        viewModel.discount.observe(viewLifecycleOwner) { discount ->
            binding.percentageView.text = discount.ae()
        }

        viewModel.discountValue.observe(viewLifecycleOwner) { discountValue ->
            binding.discountValue.text = discountValue.ae()
        }

        viewModel.totalAfterDiscount.observe(viewLifecycleOwner) { totalAfter ->
            binding.totalAfter.text = totalAfter.toInt().ae()
        }

        viewModel.sellComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                // Handle post-sell operations if needed
                viewModel.resetSellComplete()
            }
        }
    }

    private fun showSellConfirmationDialog() {
        DialogBuilder.showAlertDialog(
            context = requireContext(),
            message = "Sell all items?",
            title = "Sell Confirmation",
            positiveButton = "Yes",
            negativeButton = "No",
            onConfirm = {
                viewModel.sellAllItems(requireContext())
            },
            onCancel = {
                // Do nothing on cancel
            }
        )
    }

    private fun updateViewVisibility(hasItems: Boolean) {
        binding.operationView.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.hint.visibility = if (!hasItems) View.VISIBLE else View.GONE
    }
}