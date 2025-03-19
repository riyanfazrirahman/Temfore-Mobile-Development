package com.capstone.temfore

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.capstone.temfore.databinding.ActivityMainBinding
import com.capstone.temfore.ui.auth.login.LoginActivity
import com.capstone.temfore.ui.onboarding.OnboardingActivity
import com.capstone.temfore.ui.profile.ProfileViewModel
import com.capstone.temfore.utils.TextFormat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var isNotificationActive: Boolean = false
    private val viewModelProfile: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Authentication
        auth = Firebase.auth
        val firebaseUser = auth.currentUser

        // SharedPreferences untuk cek status onboarding
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("isOnboardingCompleted", false)
        val isLogin = sharedPreferences.getBoolean("isLoggedIn", false)

        // Redirect ke Onboarding jika belum selesai
        if (!isOnboardingCompleted) {
            navigateToOnboarding()
            return
        }


        // Redirect ke Login jika user belum sign in
        if (firebaseUser == null) {
            navigateToLogin()
            return
        }
        if (!isLogin) {
            navigateToLogin()
            return
        }

        // Cek verifikasi email user
        if (!firebaseUser.isEmailVerified) {
            logOut()  // Logout jika email belum diverifikasi
            return  // Hentikan eksekusi lebih lanjut
        }

        // Setup Toolbar sebagai ActionBar
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // Setup BottomNavigationView dan NavController
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Cek verifikasi email user
        checkEmailVerification(navController)

        // Membaca status notifikasi dari SharedPreferences
        isNotificationActive = getNotificationStatus(this)

        // Konfigurasi AppBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_favorite,
                R.id.navigation_search,
                R.id.navigation_profile
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Menyembunyikan toolbar pada fragment tertentu
            when (destination.id) {
                R.id.navigation_search -> {
                    supportActionBar?.hide()
                }

                else -> {
                    supportActionBar?.show()
                }
            }
        }

        // Setup ActionBar dan BottomNavigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        updateNotificationIcon(menu)
        return true
    }

    private fun updateNotificationIcon(menu: Menu?) {
        val notificationItem = menu?.findItem(R.id.action_notifications)
        if (isNotificationActive) {
            // Jika notifikasi aktif, ganti ikon
            notificationItem?.setIcon(R.drawable.img_notifications) // Ikon untuk status aktif
        } else {
            // Jika notifikasi tidak aktif, ganti ikon
            notificationItem?.setIcon(R.drawable.img_notifications_active) // Ikon untuk status tidak aktif
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                isNotificationActive = !isNotificationActive
                invalidateOptionsMenu()  // Memperbarui ikon saat status berubah
                if (isNotificationActive) {
                    // Jika notifikasi aktif, matikan notifikasi
                    cancelNotification()
                    Toast.makeText(this, "Notifikasi dimatikan.", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika notifikasi tidak aktif, jalankan notifikasi
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Jalankan Worker
                        scheduleNotification(applicationContext)
                        Toast.makeText(this, "Notifikasi diaktifkan.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Minta izin dari Activity
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            1
                        )
                    }
                    showCustomAlertDialog()
                }
                // Toggle status notifikasi dan simpan ke SharedPreferences
                saveNotificationStatus(this, isNotificationActive) // Simpan status baru
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Inisialisasi dialog dan tampilkan
    fun showCustomAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_notification_info, null)

        // Set the message dynamically (optional)
        val notificationMessage = dialogView.findViewById<TextView>(R.id.notification_message)
        notificationMessage.text = TextFormat.formatEnter(getString(R.string.info_notification))

        // Set up the dialog builder with the custom view
        builder.setView(dialogView)
            .setCancelable(false)  // Prevent closing by clicking outside
            .create()

        val dialog = builder.show()

        // Handle the OK button click
        val okButton = dialogView.findViewById<Button>(R.id.ok_button)
        okButton.setOnClickListener {
            dialog.dismiss()  // Close the dialog when the OK button is clicked
        }
    }

    fun saveNotificationStatus(context: Context, isActive: Boolean) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isNotificationActive", isActive)
        editor.apply()
    }

    fun getNotificationStatus(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isNotificationActive", true) // Default ke false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, jalankan notifikasi
                scheduleNotification(applicationContext)
                Toast.makeText(this, "Notifikasi diaktifkan.", Toast.LENGTH_SHORT).show()
            } else {
                // Izin ditolak, beri tahu pengguna
                Toast.makeText(this, "Izin untuk notifikasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleNotification(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "NotificationWork1",
            ExistingPeriodicWorkPolicy.KEEP, // Hindari duplikasi pekerjaan
            workRequest
        )
    }

    fun cancelNotification() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)  // Pastikan ID notifikasi sesuai
    }

    // Fungsi untuk cek verifikasi email
    private fun checkEmailVerification(navController: NavController) {
        val user = auth.currentUser
        val isVerified = user?.isEmailVerified ?: false
        if (isVerified) {
            val userDisplayName = user?.displayName ?: "User"
            val destination = navController.graph.findNode(R.id.navigation_home)
            destination?.label = "Hai, $userDisplayName"
        }else{
            logOut()
        }
    }

    // Fungsi untuk logout
    private fun logOut() {
        auth.signOut()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
        navigateToLogin()
    }

    // Fungsi untuk navigasi ke Onboarding
    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Fungsi untuk navigasi ke Login
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}