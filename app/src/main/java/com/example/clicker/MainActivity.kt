package com.example.clicker

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.view.animation.OvershootInterpolator

class MainActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvPerClick: TextView
    private lateinit var tvPerSec: TextView
    private lateinit var btnClick: Button
    private lateinit var btnUpgradeClick: Button
    private lateinit var btnUpgradeAuto: Button

    private var score: Long = 0
    private var perClick: Int = 1
    private var perSec: Int = 0
    private var clickCost: Long = 50
    private var autoCost: Long = 100

    private val prefs by lazy {
        getSharedPreferences("clicker", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScore = findViewById(R.id.tvScore)
        tvPerClick = findViewById(R.id.tvPerClick)
        tvPerSec = findViewById(R.id.tvPerSec)
        btnClick = findViewById(R.id.btnClick)
        btnUpgradeClick = findViewById(R.id.btnUpgradeClick)
        btnUpgradeAuto = findViewById(R.id.btnUpgradeAuto)

        loadState()

        btnClick.setOnClickListener {
            score += perClick
            animateClick()
            updateUI()
        }

        btnUpgradeClick.setOnClickListener {
            if (score >= clickCost) {
                score -= clickCost
                perClick++
                clickCost = (clickCost * 1.5).toLong()
                updateUI()
            }
        }

        btnUpgradeAuto.setOnClickListener {
            if (score >= autoCost) {
                score -= autoCost
                perSec++
                autoCost = (autoCost * 1.6).toLong()
                updateUI()
            }
        }

        // Авто-начисление каждую секунду
        val handler = android.os.Handler(mainLooper)
        handler.postDelayed(object : Runnable {
            override fun run() {
                score += perSec
                updateUI()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun animateClick() {
        val anim = ObjectAnimator.ofFloat(btnClick, "scaleX", 1f, 0.9f, 1f)
        anim.duration = 150
        anim.interpolator = OvershootInterpolator()
        anim.start()
    }

    private fun updateUI() {
        tvScore.text = formatNumber(score)
        tvPerClick.text = "Клик: +$perClick"
        tvPerSec.text = "Авто: +$perSec / сек"
        btnUpgradeClick.text = "⚡ Клик +1\nЦена: ${formatNumber(clickCost)}"
        btnUpgradeAuto.text = "🤖 Авто +1\nЦена: ${formatNumber(autoCost)}"
        btnUpgradeClick.isEnabled = score >= clickCost
        btnUpgradeAuto.isEnabled = score >= autoCost
        saveState()
    }

    private fun formatNumber(n: Long): String {
        return when {
            n < 1_000 -> n.toString()
            n < 1_000_000 -> "${n/1000}K"
            n < 1_000_000_000 -> "${n/1_000_000}M"
            else -> "${n/1_000_000_000}B"
        }
    }

    private fun saveState() {
        prefs.edit()
            .putLong("score", score)
            .putInt("perClick", perClick)
            .putInt("perSec", perSec)
            .putLong("clickCost", clickCost)
            .putLong("autoCost", autoCost)
            .apply()
    }

    private fun loadState() {
        score = prefs.getLong("score", 0)
        perClick = prefs.getInt("perClick", 1)
        perSec = prefs.getInt("perSec", 0)
        clickCost = prefs.getLong("clickCost", 50)
        autoCost = prefs.getLong("autoCost", 100)
        updateUI()
    }
}
