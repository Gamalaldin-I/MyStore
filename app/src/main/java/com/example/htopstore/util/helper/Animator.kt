package com.example.htopstore.util.helper

import android.view.View

object Animator {
    fun View.animateGridItem(onEnd: () -> Unit){
        this.animate().scaleY(0.8f).scaleX(0.8f).setDuration(150).withEndAction {
            this.animate().scaleY(1f).scaleX(1f).setDuration(150).withEndAction {
                onEnd()
            }.start()
        }.start()
    }
}