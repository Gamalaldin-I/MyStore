package com.example.htopstore.ui.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.domain.util.Constants.CASHIER_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.R
import com.example.htopstore.databinding.FragmentRoleSelectionBinding

class RoleSelectionFragment() : Fragment() {
    private lateinit var binding: FragmentRoleSelectionBinding
    private lateinit var oNextStep: (role: Int) -> Unit
    private lateinit var onLoginChoice: () -> Unit

    private var selectedRole = OWNER_ROLE

    companion object {
        fun newInstance(): RoleSelectionFragment {
            return RoleSelectionFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // تحديث الشكل في البداية بدون أنيميشن
        updateCardStates(animate = false)

        // عند الضغط على Owner Card
        binding.ownerCard.setOnClickListener {
            if (selectedRole != OWNER_ROLE) {
                selectedRole = OWNER_ROLE
                updateCardStates(animate = true)
            }
        }

        binding.employeeCard.setOnClickListener {
            if (selectedRole != CASHIER_ROLE) {
                selectedRole = CASHIER_ROLE
                updateCardStates(animate = true)
            }
        }

        // زر Next
        binding.next.setOnClickListener {
            animateButton(it)

            if (::oNextStep.isInitialized) {
                it.postDelayed({
                    oNextStep(selectedRole)
                }, 200)
            }
        }

        // زر Login
        binding.loginBtn.setOnClickListener {
            if (::onLoginChoice.isInitialized) {
                onLoginChoice()
            }
        }
    }

    private fun updateCardStates(animate: Boolean = true) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.action_primary)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        val whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white)

        if (selectedRole == OWNER_ROLE) {
            // Owner
            animateCardSelection(
                selectedCard = binding.ownerCard,
                unselectedCard = binding.employeeCard,
                selectedCheck = binding.ownerCheck,
                unselectedCheck = binding.employeeCheck,
                selectedColor = ContextCompat.getColor(requireContext(), R.color.background_dark),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                whiteColor = whiteColor,
                animate = animate
            )
        } else {
            // Employee
            animateCardSelection(
                selectedCard = binding.employeeCard,
                unselectedCard = binding.ownerCard,
                selectedCheck = binding.employeeCheck,
                unselectedCheck = binding.ownerCheck,
                selectedColor = ContextCompat.getColor(requireContext(), R.color.background_dark),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                whiteColor = whiteColor,
                animate = animate
            )
        }
    }

    private fun animateCardSelection(
        selectedCard: com.google.android.material.card.MaterialCardView,
        unselectedCard: com.google.android.material.card.MaterialCardView,
        selectedCheck: View,
        unselectedCheck: View,
        selectedColor: Int,
        primaryColor: Int,
        secondaryColor: Int,
        whiteColor: Int,
        animate: Boolean
    ) {
        if (animate) {
            //  للـ Card المحدد
            animateSelectedCard(selectedCard, selectedCheck, selectedColor, primaryColor)

            // أنيميشن للـ Card الغير محدد
            animateUnselectedCard(unselectedCard, unselectedCheck, secondaryColor, secondaryColor)
        } else {
            // بدون أنيميشن (للتحميل الأولي)
            setCardSelected(selectedCard, selectedCheck, selectedColor, primaryColor)
            setCardUnselected(unselectedCard, unselectedCheck, secondaryColor, secondaryColor)
        }
    }

    private fun animateSelectedCard(
        card: com.google.android.material.card.MaterialCardView,
        check: View,
        backgroundColor: Int,
        strokeColor: Int
    ) {
        // أنيميشن Scale (تكبير وتصغير)
        val scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.05f, 1f)
        val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.05f, 1f)
        scaleX.duration = 300
        scaleY.duration = 300
        scaleX.interpolator = OvershootInterpolator()
        scaleY.interpolator = OvershootInterpolator()

        // أنيميشن للحدود (Stroke)
        val strokeAnimator = ValueAnimator.ofInt(dpToPx(1), dpToPx(3))
        strokeAnimator.duration = 200
        strokeAnimator.addUpdateListener { animation ->
            card.strokeWidth = animation.animatedValue as Int
        }

        // أنيميشن للظل (Elevation)
        val elevationAnimator = ObjectAnimator.ofFloat(
            card,
            "cardElevation",
            card.cardElevation,
            dpToPx(8).toFloat()
        )
        elevationAnimator.duration = 200

        // تغيير الألوان
        card.strokeColor = strokeColor
        card.setCardBackgroundColor(backgroundColor)

        // أنيميشن لعلامة الصح
        check.visibility = View.VISIBLE
        check.alpha = 0f
        check.scaleX = 0.5f
        check.scaleY = 0.5f

        val checkAlpha = ObjectAnimator.ofFloat(check, "alpha", 0f, 1f)
        val checkScaleX = ObjectAnimator.ofFloat(check, "scaleX", 0.5f, 1.2f, 1f)
        val checkScaleY = ObjectAnimator.ofFloat(check, "scaleY", 0.5f, 1.2f, 1f)
        checkAlpha.duration = 300
        checkScaleX.duration = 400
        checkScaleY.duration = 400
        checkScaleX.interpolator = BounceInterpolator()
        checkScaleY.interpolator = BounceInterpolator()

        // تشغيل كل الأنيميشنات
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            scaleX, scaleY,
            strokeAnimator,
            elevationAnimator,
            checkAlpha, checkScaleX, checkScaleY
        )
        animatorSet.start()
    }

    private fun animateUnselectedCard(
        card: com.google.android.material.card.MaterialCardView,
        check: View,
        backgroundColor: Int,
        strokeColor: Int
    ) {
        // أنيميشن للحدود
        val strokeAnimator = ValueAnimator.ofInt(card.strokeWidth, dpToPx(1))
        strokeAnimator.duration = 200
        strokeAnimator.addUpdateListener { animation ->
            card.strokeWidth = animation.animatedValue as Int
        }

        // أنيميشن للظل
        val elevationAnimator = ObjectAnimator.ofFloat(
            card,
            "cardElevation",
            card.cardElevation,
            dpToPx(2).toFloat()
        )
        elevationAnimator.duration = 200

        // تغيير الألوان
        card.strokeColor = strokeColor
        card.setCardBackgroundColor(backgroundColor)

        // إخفاء علامة الصح مع أنيميشن
        val checkAlpha = ObjectAnimator.ofFloat(check, "alpha", 1f, 0f)
        val checkScale = ObjectAnimator.ofFloat(check, "scaleX", 1f, 0.5f)
        val checkScaleY = ObjectAnimator.ofFloat(check, "scaleY", 1f, 0.5f)
        checkAlpha.duration = 150
        checkScale.duration = 150
        checkScaleY.duration = 150

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(checkAlpha, checkScale, checkScaleY)
        animatorSet.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                check.visibility = View.GONE
            }
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })

        // تشغيل الأنيميشنات
        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playTogether(strokeAnimator, elevationAnimator, animatorSet)
        mainAnimatorSet.start()
    }

    private fun setCardSelected(
        card: com.google.android.material.card.MaterialCardView,
        check: View,
        backgroundColor: Int,
        strokeColor: Int
    ) {
        card.strokeWidth = dpToPx(3)
        card.strokeColor = strokeColor
        card.cardElevation = dpToPx(8).toFloat()
        card.setCardBackgroundColor(backgroundColor)
        check.visibility = View.VISIBLE
        check.alpha = 1f
        check.scaleX = 1f
        check.scaleY = 1f
    }

    private fun setCardUnselected(
        card: com.google.android.material.card.MaterialCardView,
        check: View,
        backgroundColor: Int,
        strokeColor: Int
    ) {
        card.strokeWidth = dpToPx(1)
        card.strokeColor = strokeColor
        card.cardElevation = dpToPx(2).toFloat()
        card.setCardBackgroundColor(backgroundColor)
        check.visibility = View.GONE
    }

    private fun animateButton(button: View) {
        // أنيميشن بسيط للزر عند الضغط
        val scaleDown = AnimatorSet()
        val scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f)
        scaleDown.playTogether(scaleDownX, scaleDownY)
        scaleDown.duration = 100

        val scaleUp = AnimatorSet()
        val scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f)
        scaleUp.playTogether(scaleUpX, scaleUpY)
        scaleUp.duration = 100

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(scaleDown, scaleUp)
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun setOnLogin(onLoginChoice: () -> Unit) {
        this.onLoginChoice = onLoginChoice
    }

    fun setOnNext(doThis: (role: Int) -> Unit) {
        this.oNextStep = doThis
    }
}