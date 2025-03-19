package com.capstone.temfore.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.capstone.temfore.MainActivity
import com.capstone.temfore.R
import com.capstone.temfore.databinding.ActivityWaitingVerificationBinding
import com.capstone.temfore.ui.auth.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class WaitingVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan pesan bahwa user perlu memverifikasi email
        binding.messageTextView.text =
            getString(R.string.silakan_cek_email_anda_untuk_verifikasi_akun)

        val user = FirebaseAuth.getInstance().currentUser
        binding.btnVerify.setOnClickListener {
            checkEmailVerificationStatus()
        }

        binding.btnSendVerify.setOnClickListener {
            user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                if (verifyTask.isSuccessful) {
                    showAlertDialog(
                        title = "Yeah!",
                        message = "Verifikasi email telah dikirim ke Akun dengan ${user.email}. Silakan cek email Anda.",
                        onPositiveAction = {
                            // Cek verifikasi email secara berkala setelah pengiriman
                            checkEmailVerificationStatus()
                            finish()
                        }
                    )
                } else {
                    showAlertDialog(
                        title = "Sorry!",
                        message = "Gagal mengirim email verifikasi: ${verifyTask.exception?.message}.",
                        onPositiveAction = {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }

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

    private fun checkEmailVerificationStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val verification = user.isEmailVerified
                Log.d(TAG, "Email Verify: $verification")

                if (verification) {
                    // Email sudah terverifikasi, lanjutkan proses
                    Toast.makeText(this, "Email sudah diverifikasi", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Email belum terverifikasi, tampilkan pesan atau lanjutkan ke WaitingVerificationActivity
                    Toast.makeText(
                        this,
                        "Email belum diverifikasi. Silakan cek email Anda untuk verifikasi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else {
                Log.d(TAG, "Gagal memuat status verifikasi: ${task.exception?.message}")
                Toast.makeText(this, "Gagal memuat status verifikasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "WaitingVerificationActivity"
    }

    fun onVerifyButtonClick(view: View) {}
}
