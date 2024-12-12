package com.capstone.temfore.ui.auth.resetpassword

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.temfore.MainActivity
import com.capstone.temfore.R
import com.capstone.temfore.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Menghubungkan binding dengan layout
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ketika tombol reset password diklik
        binding.btnResetPassword.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()

            // Mengecek apakah email kosong
            if (email.isEmpty()) {
                Toast.makeText(this, "Email harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Menampilkan progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Mengecek apakah email terdaftar di Firebase Auth
            checkEmailInFirebase(email)
        }
    }

    // Fungsi untuk mengecek apakah email terdaftar di Firebase Auth
    private fun checkEmailInFirebase(email: String) {
        // Mengecek apakah email valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        @Suppress("DEPRECATION")
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Email TERDAFTAR
                    sendResetEmail(email)
                } else {
                    // Gagal memeriksa
                    Toast.makeText(this, "Gagal memeriksa email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk mengirim email reset password
    private fun sendResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Menyembunyikan progress bar setelah proses selesai
                binding.progressBar.visibility = View.GONE
                binding.ivEmailResult.visibility = View.VISIBLE
                if (task.isSuccessful) {
                    // Menampilkan email yang disensor di UI
                    val maskedEmail = maskEmail(email)
                    binding.ivEmailResult.text =
                        getString(R.string.email_valid_untuk_reset_password, maskedEmail)

                    Toast.makeText(this, "Email reset password telah dikirim!", Toast.LENGTH_SHORT)
                        .show()

                    // Kembali ke halaman utama setelah beberapa detik
                    binding.root.postDelayed({
                        // To navigate back
                        onSupportNavigateUp()
                    }, 2000) // Delay 2 detik sebelum kembali
                } else {
                    val errorMessage =
                        task.exception?.message ?: "Terjadi kesalahan saat mengirim email."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        finish()
        return super.onSupportNavigateUp()
    }

    // Fungsi untuk menyensor email
    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf("@")
        if (atIndex != -1) {
            val localPart = email.substring(0, atIndex)
            val domainPart = email.substring(atIndex)
            val maskedLocalPart = localPart.take(2) + "****" + localPart.takeLast(2)
            return maskedLocalPart + domainPart
        }
        return email
    }
}
