package com.capstone.temfore.ui.auth.login

import android.app.ActivityOptions
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.capstone.temfore.BuildConfig
import com.capstone.temfore.MainActivity
import com.capstone.temfore.R
import com.capstone.temfore.databinding.ActivityLoginBinding
import com.capstone.temfore.ui.auth.register.RegisterActivity
import com.capstone.temfore.ui.auth.register.WaitingVerificationActivity
import com.capstone.temfore.ui.auth.resetpassword.ResetPasswordActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginAnimationHelper = LoginAnimationHelper()
        loginAnimationHelper.playAnimation(binding)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            if (checkAllField()) {
                binding.progressBar.visibility = View.VISIBLE
                // Proses login menggunakan EmailAndPassword
                login()
            }
        }

        binding.btnLoginWithGoogle.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            loginWithGoogle()
        }

        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,    // Context
                android.R.anim.fade_in,    // Animasi saat Activity pertama muncul
                android.R.anim.fade_out    // Animasi saat Activity pertama hilang
            )
            startActivity(intent, options.toBundle())
            finish()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            // Membuat transisi animasi
            val options = ActivityOptions.makeCustomAnimation(
                this,    // Context
                android.R.anim.fade_in,    // Animasi saat Activity pertama muncul
                android.R.anim.fade_out    // Animasi saat Activity pertama hilang
            )
            startActivity(intent, options.toBundle())
            finish()
        }
    }

    private fun login(){
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Cek status verifikasi email
                checkEmailVerificationStatus()
            } else {
                // Menampilkan pesan error
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Login gagal: Email atau Password yang Anda masukkan salah.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkEmailVerificationStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        val verification = user?.isEmailVerified
        Log.d(TAG,"Email Verify: $verification")

        // Email sudah terverifikasi, lanjutkan proses
        if (user != null && verification == true) {
            binding.progressBar.visibility = View.GONE
            // Tampilkan dialog sukses setelah login berhasil
            AlertDialog.Builder(this).apply {
                setTitle("Sukses Login!")
                setMessage("Akun dengan email ${user.email} berhasil login. Selamat datang kembali!")
                setPositiveButton("Lanjut") { _, _ ->
                    // Pindah ke MainActivity
                    val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        } else {
            binding.progressBar.visibility = View.GONE
            // Email belum terverifikasi, tampilkan pesan atau lanjutkan ke WaitingVerificationActivity
            Toast.makeText(
                this,
                "Email belum diverifikasi. Silakan cek email Anda untuk verifikasi.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, WaitingVerificationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

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
        return true
    }

    private fun loginWithGoogle() {

        val credentialManager =
            CredentialManager.create(this) //import from androidx.CredentialManager

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder() //import from androidx.CredentialManager
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    //import from androidx.CredentialManager
                    request = request,
                    context = this@LoginActivity,
                )
                handleLoginWithGoogle(result)
            } catch (e: GetCredentialException) {
                Log.d("Error", e.message.toString())
                binding.progressBar.visibility = View.GONE
            }
        }


    }

    private fun handleLoginWithGoogle(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and authenticate on your server.
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Toast.makeText(
                            this,
                            "Token tidak valid, silakan coba lagi.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "Received an invalid google id token response", e)
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                    binding.progressBar.visibility = View.GONE
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}