package com.example.htopstore.ui.pendingSell

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.htopstore.databinding.ActivityPendingSellActionsBinding

class PendingSellActionsActivity : AppCompatActivity() {
    companion object{
    }
    private lateinit var binding: ActivityPendingSellActionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPendingSellActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}