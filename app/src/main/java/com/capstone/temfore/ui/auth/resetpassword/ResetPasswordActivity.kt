package com.capstone.temfore.ui.auth.resetpassword

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

            // Mengecek apakah email valid
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mengecek apakah email terdaftar di Firebase
            checkEmailAndSendReset(email)
        }
    }

    // Fungsi untuk mengecek apakah email terdaftar di Firebase dan mengirim email reset
    private fun checkEmailAndSendReset(email: String) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                // Email terdaftar, lakukan logout dan kirim email reset
                showLogoutDialog(email)
            } else {
                // Gagal memeriksa email
                Toast.makeText(this, "Gagal memeriksa email. Coba lagi nanti.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Menampilkan dialog konfirmasi untuk logout sebelum mengirim email reset
    private fun showLogoutDialog(email: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isLogin = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLogin) {
            // Dialog untuk logout dan reset password ketika belum login
            AlertDialog.Builder(this).apply {
                setTitle("Konfirmasi Logout")
                setMessage("Untuk mengirim email reset password, akun akan logout. Lanjutkan?")
                setPositiveButton("Ya") { _, _ ->
                    logOutAndSendResetEmail(email)
                }
                setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        } else {
            // Dialog untuk reset password ketika sudah login
            AlertDialog.Builder(this).apply {
                setTitle("Reset Password")
                setMessage("Anda yakin ingin mengirimkan email untuk reset password?")
                setPositiveButton("Ya") { _, _ ->
                    sendResetEmail(email)
                }
                setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }

    // Logout pengguna dan mengirim email reset password
    private fun logOutAndSendResetEmail(email: String) {
        auth.signOut()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

        sendResetEmail(email)
    }

    // Mengirim email reset password
    private fun sendResetEmail(email: String) {
        binding.progressBar.visibility = View.VISIBLE
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                val maskedEmail = maskEmail(email)
                Toast.makeText(
                    this,
                    "Email reset password telah dikirim ke $maskedEmail",
                    Toast.LENGTH_SHORT
                ).show()

                // Kembali ke halaman login
                navigateToLogin()
            } else {
                val errorMessage =
                    task.exception?.message ?: "Terjadi kesalahan saat mengirim email."
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Navigasi ke halaman login
    private fun navigateToLogin() {
        val intent = Intent(this, com.capstone.temfore.ui.auth.login.LoginActivity::class.java)
        startActivity(intent)
        finish()
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
