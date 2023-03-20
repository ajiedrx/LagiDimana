package com.project.lagidimana.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.project.lagidimana.databinding.ActivitySplashScreenBinding
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding by lazy { _binding!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        Timer().schedule(1500){
            toNextPage()
        }
    }

    private fun playAnimation(){
        val firstFadeAnimation = ObjectAnimator.ofFloat(binding.tvFirstLine, View.ALPHA, 1f).setDuration(500)
        val firstSlideRightAnimation = ObjectAnimator.ofFloat(binding.tvFirstLine, View.TRANSLATION_X, -30f, 0f).setDuration(500)

        val secondFadeAnimation = ObjectAnimator.ofFloat(binding.tvSecondLine, View.ALPHA, 1f).setDuration(500)
        val secondSlideLeftAnimation = ObjectAnimator.ofFloat(binding.tvSecondLine, View.TRANSLATION_X, 30f, 0f).setDuration(500)

        AnimatorSet().apply {
            playTogether(firstFadeAnimation, firstSlideRightAnimation, secondFadeAnimation, secondSlideLeftAnimation)
            start()
        }
    }

    private fun toNextPage(){
        finish()
        DashboardActivity.start(this)
    }
}