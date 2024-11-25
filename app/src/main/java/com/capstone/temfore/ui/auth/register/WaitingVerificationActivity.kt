package com.capstone.temfore.ui.auth.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.temfore.MainActivity
import com.capstone.temfore.databinding.ActivityWaitingVerificationBinding
import com.google.firebase.auth.FirebaseAuth


class WaitingVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan pesan bahwa user perlu memverifikasi email
        binding.messageTextView.text = "Silakan cek email Anda untuk verifikasi akun."

        // Menambahkan listener untuk TextView agar bisa klik
        binding.messageTextView.setOnClickListener {
            // Intent untuk membuka aplikasi email (Gmail, Outlook, dll.)
            val user = FirebaseAuth.getInstance().currentUser
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                val emailUser = user?.email
                data = Uri.parse("mailto: $emailUser") // Membuka aplikasi email
                putExtra(Intent.EXTRA_SUBJECT, "Verifikasi Akun Temfore")
                putExtra(Intent.EXTRA_TEXT, "Tolong verifikasi akun saya pada aplikasi Temfore.")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Tidak ada aplikasi email yang terpasang", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        binding.verifyButton.setOnClickListener {
            if (user != null) {
                Log.d("HomeFragment", "Email verified: ${user.isEmailVerified}")
                // Jika email sudah terverifikasi, tampilkan tombol dan arahkan ke halaman login
                Toast.makeText(
                    this@WaitingVerificationActivity,
                    "Email terverifikasi, sekarang Anda bisa login",
                    Toast.LENGTH_SHORT
                ).show()
                finish() // Tutup WaitingVerificationActivity
                startActivity(Intent(this@WaitingVerificationActivity, MainActivity::class.java))
            } else {
                // Ulangi pengecekan verifikasi
                Toast.makeText(
                    this@WaitingVerificationActivity,
                    "Akun Anda belum di verifikasi .",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
