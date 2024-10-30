package ru.netology.statsview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.animation.AnimationUtils
import ru.netology.statsview.ui.StatsView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val viewParallel = findViewById<StatsView>(R.id.statsViewParallel)
        val viewSequential = findViewById<StatsView>(R.id.statsViewSequential)
        val viewBidirectional = findViewById<StatsView>(R.id.statsViewBidirectional)

        viewParallel.data = listOf(
            500F,
            500F,
            500F,
            500F
        )

        viewSequential.data = listOf(
            500F,
            500F,
            500F,
            500F
        )

        viewBidirectional.data = listOf(
            500F,
            500F,
            500F,
            500F
        )
    }
}