package com.example.htopstore.util.helper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.example.htopstore.R
import com.google.android.material.chip.Chip

object Animator {
    fun View.animateGridItem(onEnd: () -> Unit){
        this.animate().scaleY(0.8f).scaleX(0.8f).setDuration(150).withEndAction {
            this.animate().scaleY(1f).scaleX(1f).setDuration(150).withEndAction {
                onEnd()
            }.start()
        }.start()
    }
    fun ImageView.animateAddToCart(onEnd: () -> Unit){
        this.animate().scaleY(0.8f).scaleX(0.8f).setDuration(50).withEndAction {
            this.setImageResource(R.drawable.nav_bag_smile)
            onEnd()
            this.animate().scaleY(1f).scaleX(1f).setDuration(1000).withEndAction {
                this.setImageResource(R.drawable.ic_add_btn)
            }.start()
        }
    }
    fun View.animateRotate360(onEnd: () -> Unit){
        this.animate().rotationBy(360f).setDuration(1000).withEndAction {
            onEnd()
        }.start()
    }
    fun View.animateStockItem( onEnd: () -> Unit){
        this.animate().scaleY(0.8f).scaleX(0.8f).setDuration(100).withEndAction {
            this.animate().scaleY(1f).scaleX(1f).setDuration(200).withEndAction {
                onEnd()
            }.start()
        }.start()
    }

    fun Chip.animateSelectedChip(selected: Boolean){
        if (selected){
            this.animate().scaleY(0.8f).scaleX(0.8f).setDuration(200).start()}
        else{
            this.animate().scaleY(1f).scaleX(1f).setDuration(200).start()
        }
    }
    fun fadeIn(
            view: View,
            duration: Long = 300,
            onEnd: (() -> Unit)? = null
        ) {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { onEnd?.invoke() }
                .start()
        }

        /**
         * Fade out animation
         */
        fun fadeOut(
            view: View,
            duration: Long = 300,
            hideOnEnd: Boolean = true,
            onEnd: (() -> Unit)? = null
        ) {
            view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    if (hideOnEnd) view.visibility = View.GONE
                    onEnd?.invoke()
                }
                .start()
        }

        /**
         * Scale in with bounce effect
         */
        fun scaleInBounce(
            view: View,
            duration: Long = 500,
            overshoot: Float = 2f,
            onEnd: (() -> Unit)? = null
        ) {
            view.scaleX = 0f
            view.scaleY = 0f
            view.visibility = View.VISIBLE

            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(OvershootInterpolator(overshoot))
                .withEndAction { onEnd?.invoke() }
                .start()
        }

        /**
         * Scale out animation
         */
        fun scaleOut(
            view: View,
            duration: Long = 300,
            targetScale: Float = 0f,
            onEnd: (() -> Unit)? = null
        ) {
            view.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { onEnd?.invoke() }
                .start()
        }

        /**
         * Slide up animation
         */
        fun slideUp(
            view: View,
            duration: Long = 400,
            onEnd: (() -> Unit)? = null
        ) {
            view.translationY = view.height.toFloat()
            view.visibility = View.VISIBLE

            view.animate()
                .translationY(0f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { onEnd?.invoke() }
                .start()
        }

        /**
         * Slide down animation
         */
        fun slideDown(
            view: View,
            duration: Long = 400,
            hideOnEnd: Boolean = true,
            onEnd: (() -> Unit)? = null
        ) {
            view.animate()
                .translationY(view.height.toFloat())
                .setDuration(duration)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    if (hideOnEnd) view.visibility = View.GONE
                    onEnd?.invoke()
                }
                .start()
        }

        /**
         * Pulse animation (scale up and down)
         */
        fun pulse(
            view: View,
            scaleAmount: Float = 1.1f,
            duration: Long = 300
        ) {
            val scaleUp = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, scaleAmount)
            val scaleUpY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, scaleAmount)
            val scaleDown = ObjectAnimator.ofFloat(view, View.SCALE_X, scaleAmount, 1f)
            val scaleDownY = ObjectAnimator.ofFloat(view, View.SCALE_Y, scaleAmount, 1f)

            val animatorSet = AnimatorSet()
            animatorSet.play(scaleUp).with(scaleUpY)
            animatorSet.play(scaleDown).with(scaleDownY).after(scaleUp)
            animatorSet.duration = duration
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.start()
        }

        /**
         * Shake animation (for error feedback)
         */
        fun shake(view: View, duration: Long = 500) {
            val shake = ObjectAnimator.ofFloat(
                view,
                View.TRANSLATION_X,
                0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f
            )
            shake.duration = duration
            shake.start()
        }

        /**
         * Rotate animation
         */
        fun rotate(
            view: View,
            fromDegrees: Float = 0f,
            toDegrees: Float = 360f,
            duration: Long = 500,
            onEnd: (() -> Unit)? = null
        ) {
            view.animate()
                .rotation(toDegrees)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { onEnd?.invoke() }
                .start()
        }

        /**
         * Success sequence animation (for success overlay)
         */
        fun successSequence(
            overlayView: View,
            iconView: View,
            duration: Long = 500,
            holdDuration: Long = 1500,
            onEnd: (() -> Unit)? = null
        ) {
            // Step 1: Fade in overlay
            fadeIn(overlayView, duration / 2) {
                // Step 2: Scale in icon with bounce
                scaleInBounce(iconView, duration) {
                    // Step 3: Hold
                    overlayView.postDelayed({
                        // Step 4: Scale out icon
                        scaleOut(iconView, duration / 2, 0.8f) {
                            // Step 5: Fade out overlay
                            fadeOut(overlayView, duration / 2) {
                                // Reset for next time
                                iconView.scaleX = 0f
                                iconView.scaleY = 0f
                                onEnd?.invoke()
                            }
                        }
                    }, holdDuration)
                }
            }
        }

        /**
         * Error shake animation for input fields
         */
        fun shakeError(view: View) {
            shake(view)
            // Optional: Change border color temporarily
            val originalAlpha = view.alpha
            view.animate()
                .alpha(0.7f)
                .setDuration(250)
                .withEndAction {
                    view.animate()
                        .alpha(originalAlpha)
                        .setDuration(250)
                        .start()
                }
                .start()
        }

        /**
         * Loading rotation animation (continuous)
         */
        fun startLoadingRotation(view: View): ValueAnimator {
            val rotation = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f)
            rotation.duration = 1000
            rotation.repeatCount = ValueAnimator.INFINITE
            rotation.interpolator = AccelerateDecelerateInterpolator()
            rotation.start()
            return rotation
        }

        /**
         * Cancel all animations on a view
         */
        fun cancelAnimations(view: View) {
            view.animate().cancel()
            view.clearAnimation()
        }




}