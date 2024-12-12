package com.capstone.temfore.ui.auth.register

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.capstone.temfore.MainActivity
import com.capstone.temfore.R
import com.capstone.temfore.databinding.ActivityRegisterBinding
import com.capstone.temfore.ui.auth.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val registerAnimationHelper = RegisterAnimationHelper()
        registerAnimationHelper.playAnimation(binding)

        setupAction()

    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Membuat transisi animasi
            val options = ActivityOptions.makeCustomAnimation(
                this,    // Context
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            finish()
        }

    }

    private fun registerUser() {
        val username = binding.inputUsername.text.toString()
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Username, Email, dan Password tidak boleh kosong",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (checkAllField()) {
            binding.progressBar.visibility = View.VISIBLE
            // Membuat user dengan Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        // Menyimpan username di profil pengguna Firebase
                        user?.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName(username).build()
                        )?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Kirim email verifikasi
                                user.sendEmailVerification().addOnCompleteListener { verifyTask ->
                                    binding.progressBar.visibility = View.GONE
                                    if (verifyTask.isSuccessful) {
                                        showAlertDialog(
                                            title = "Yeah!",
                                            message = "Verifikasi email telah dikirim. Silakan cek email Anda.",
                                            onPositiveAction = {
                                                // Tunggu verifikasi email
                                                checkEmailVerificationStatus()
                                            }
                                        )
                                    } else {
                                        showAlertDialog(
                                            title = "Sorry!",
                                            message = "Gagal mengirim email verifikasi: ${verifyTask.exception?.message}",
                                            onPositiveAction = {
                                                val intent = Intent(
                                                    this,
                                                    WaitingVerificationActivity::class.java
                                                )
                                                // Membuat transisi animasi
                                                val options = ActivityOptions.makeCustomAnimation(
                                                    this,    // Context
                                                    android.R.anim.fade_in,
                                                    android.R.anim.fade_out
                                                )
                                                startActivity(intent, options.toBundle())
                                                finish()
                                            }
                                        )
                                    }
                                }
                            } else {
                                binding.progressBar.visibility = View.GONE
                                Log.d(TAG, "${updateTask.exception?.message}")
                                Toast.makeText(this, "Gagal mengupdate profil", Toast.LENGTH_SHORT)
                                    .show()

                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        Log.d(TAG, "${task.exception?.message}")
                        showAlertDialog(
                            title = "Registrasi gagal!",
                            message = "${task.exception?.message}",
                        )
                    }
                }
        }
    }

    private fun checkEmailVerificationStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        val verification = user?.isEmailVerified
        Log.d(TAG, "Email Verify: $verification")

        if (user != null && verification == true) {
            // Email sudah terverifikasi, lanjutkan proses
            val intent = Intent(this, MainActivity::class.java)
            // Membuat transisi animasi
            val options = ActivityOptions.makeCustomAnimation(
                this,    // Context
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            finish()
        } else {
            // Email belum terverifikasi, tampilkan pesan atau lanjutkan ke WaitingVerificationActivity
            val intent = Intent(this, WaitingVerificationActivity::class.java)
            // Membuat transisi animasi
            val options = ActivityOptions.makeCustomAnimation(
                this,    // Context
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            finish()
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

        // Validasi input
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong.", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.error = "Check Email Format!"
            binding.layoutEmail.setErrorTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
            )
            Handler(Looper.getMainLooper()).postDelayed({
                binding.layoutEmail.isErrorEnabled = false
            }, 3000)
            return false
        }

        if (password.length < 8) {
            binding.layoutPassword.error = "Password must be at least 8 characters!"
            binding.layoutPassword.setErrorTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
            )
            Handler(Looper.getMainLooper()).postDelayed({
                binding.layoutPassword.isErrorEnabled = false
            }, 3000)
            return false
        }

        if (confirmPassword != password) {
            binding.layoutConfirmPassword.error = "Passwords do not match!"
            binding.layoutConfirmPassword.setErrorTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
            )
            Handler(Looper.getMainLooper()).postDelayed({
                binding.layoutConfirmPassword.isErrorEnabled = false
            }, 3000)
            return false
        }

        return true
    }

    private fun showAlertDialog(
        title: String,
        message: String,
        positiveButtonText: String = "Lanjut",
        onPositiveAction: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveAction?.invoke()
            }
            .setIcon(
                if (title == "Yeah!") R.drawable.ic_notifications_black_24dp // Ikon sukses
                else R.drawable.ic_notifications_black_24dp // Ikon error
            )
            .create()
            .show()
    }


    companion object {
        private const val TAG = "RegisterActivity"
    }
}