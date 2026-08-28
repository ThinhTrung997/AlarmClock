package com.example.alarmclock.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.alarmclock.R
import com.example.alarmclock.data.SettingsStorage
import com.example.alarmclock.ui.alarm.MainActivity
import com.example.alarmclock.ui.settings.LanguageActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2200L
        private const val DOT_ANIM_INTERVAL = 400L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var dotIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme before setContentView
        SettingsStorage.initAppTheme(this)

        setContentView(R.layout.activity_splash)

        // Hide system UI for full immersive
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        startEnterAnimation()
        startDotAnimation()

        handler.postDelayed({
            navigateNext()
        }, SPLASH_DURATION)
    }

    private fun startEnterAnimation() {
        val logo = findViewById<ImageView>(R.id.ivSplashLogo)
        val appName = findViewById<TextView>(R.id.tvSplashAppName)
        val tagline = findViewById<TextView>(R.id.tvSplashTagline)
        val loadingDots = findViewById<LinearLayout>(R.id.loadingDots)

        // Initial state
        logo.alpha = 0f
        logo.scaleX = 0.3f
        logo.scaleY = 0.3f
        appName.alpha = 0f
        appName.translationY = 40f
        tagline.alpha = 0f
        tagline.translationY = 30f
        loadingDots.alpha = 0f

        // Logo animation: scale up with overshoot
        val logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1.0f).apply {
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator(1.5f)
        }
        val logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1.0f).apply {
            duration = 600
            startDelay = 200
            interpolator = OvershootInterpolator(1.5f)
        }
        val logoFade = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 200
        }

        // App name animation: slide up + fade in
        val nameSlide = ObjectAnimator.ofFloat(appName, "translationY", 40f, 0f).apply {
            duration = 500
            startDelay = 700
            interpolator = AccelerateDecelerateInterpolator()
        }
        val nameFade = ObjectAnimator.ofFloat(appName, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 700
        }

        // Tagline: slide up + fade
        val tagSlide = ObjectAnimator.ofFloat(tagline, "translationY", 30f, 0f).apply {
            duration = 400
            startDelay = 950
            interpolator = AccelerateDecelerateInterpolator()
        }
        val tagFade = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 950
        }

        // Dots fade in
        val dotsFade = ObjectAnimator.ofFloat(loadingDots, "alpha", 0f, 1f).apply {
            duration = 400
            startDelay = 1200
        }

        // Logo subtle pulse animation after entrance
        handler.postDelayed({
            val pulse = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.08f, 1f).apply { duration = 1000 },
                    ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.08f, 1f).apply { duration = 1000 }
                )
                interpolator = AccelerateDecelerateInterpolator()
            }
            pulse.start()
        }, 1100)

        AnimatorSet().apply {
            playTogether(
                logoScaleX, logoScaleY, logoFade,
                nameSlide, nameFade,
                tagSlide, tagFade,
                dotsFade
            )
            start()
        }
    }

    private fun startDotAnimation() {
        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        val dotRunnable = object : Runnable {
            override fun run() {
                dots.forEach { it.animate().alpha(0.3f).setDuration(150).start() }
                dots[dotIndex % 3].animate().alpha(1f).setDuration(150).start()
                dotIndex++
                handler.postDelayed(this, DOT_ANIM_INTERVAL)
            }
        }
        handler.postDelayed(dotRunnable, 1400)
    }

    private fun navigateNext() {
        val intent = if (SettingsStorage.isFirstLaunch(this)) {
            Intent(this, LanguageActivity::class.java).apply {
                putExtra(LanguageActivity.EXTRA_FIRST_LAUNCH, true)
            }
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        // Fade out transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
