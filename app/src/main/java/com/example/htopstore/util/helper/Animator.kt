package com.example.htopstore.util.helper

import android.view.View
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



}