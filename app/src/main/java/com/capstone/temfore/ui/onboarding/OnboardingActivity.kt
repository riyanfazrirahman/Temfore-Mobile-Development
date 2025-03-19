package com.capstone.temfore.ui.onboarding

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.capstone.temfore.MainActivity
import com.capstone.temfore.R
import com.capstone.temfore.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById(R.id.viewPager)
        val fragmentList = listOf(
            OnboardingFragment.newInstance(R.drawable.img_onboarding_1, R.string.onboarding_1),
            OnboardingFragment.newInstance(R.drawable.img_onboarding_2, R.string.onboarding_2),
            OnboardingFragment.newInstance(R.drawable.img_onboarding_3, R.string.onboarding_3),
            OnboardingFragment.newInstance(R.drawable.img_onboarding_4, R.string.onboarding_4_1)
        )

        adapter = OnboardingAdapter(this, fragmentList)
        viewPager.adapter = adapter

        val springDotsIndicator = binding.dotsIndicator
        val viewPager = binding.viewPager

        springDotsIndicator.attachTo(viewPager)

        // Menghubungkan ViewPager2 dengan indikator
        binding.dotsIndicator.setViewPager2(viewPager)

        // Menampilkan tombol Get Started di slide terakhir
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == fragmentList.size - 1) {
                    // Animasi untuk mengubah latar belakang
                    val colorFade = ValueAnimator.ofArgb(
                        binding.root.solidColor, // Warna awal
                        getColor(R.color.white) // Warna akhir
                    )
                    colorFade.duration = 700 // Durasi animasi (0.7 detik)
                    colorFade.addUpdateListener { animator ->
                        binding.root.setBackgroundColor(animator.animatedValue as Int)
                    }
                    colorFade.start()

                    // Animasi fade-in untuk tombol Get Started
                    binding.btnStart.animate()
                        .alpha(1f)
                        .setDuration(700) // Durasi animasi (0.7 detik)
                        .withStartAction {
                            binding.btnStart.visibility = View.VISIBLE
                        }
                        .start()

                    // Animasi fade-out untuk elemen lain
                    binding.dotsIndicator.animate()
                        .alpha(0f)
                        .setDuration(700) // Durasi lebih lama
                        .withEndAction {
                            binding.dotsIndicator.visibility = View.GONE
                        }
                        .start()

                    binding.viewPager.animate()
                        .alpha(0f)
                        .setDuration(700) // Durasi lebih lama
                        .withEndAction {
                            binding.viewPager.visibility = View.GONE
                        }
                        .start()

                    binding.cLayoutWelcome.animate()
                        .alpha(1f)
                        .setDuration(500) // Durasi lebih cepat
                        .withEndAction {
                            binding.cLayoutWelcome.visibility = View.VISIBLE
                        }
                        .start()
                } else {
                    // Tampilkan kembali elemen-elemen saat bukan di slide terakhir
                    binding.btnStart.visibility = View.GONE
                    binding.dotsIndicator.visibility = View.VISIBLE
                    binding.viewPager.visibility = View.VISIBLE
                    binding.cLayoutWelcome.visibility = View.GONE

                    binding.btnStart.alpha = 0f // Reset alpha
                    binding.dotsIndicator.alpha = 1f
                    binding.viewPager.alpha = 1f
                    binding.cLayoutWelcome.alpha = 0f
                }
            }
        })

        binding.btnStart.setOnClickListener {
            // Simpan status onboarding selesai dan pindah ke MainActivity
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isOnboardingCompleted", true).apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
