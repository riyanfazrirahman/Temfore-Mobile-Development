package com.capstone.temfore.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.capstone.temfore.R
import com.capstone.temfore.databinding.FragmentProfileBinding
import com.capstone.temfore.ui.auth.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val user = auth.currentUser

        // Menampilkan data pengguna seperti nama dan email
        val userNameTextView: TextView = binding.userName
        val userEmailTextView: TextView = binding.userEmail
        val profileImageView: ImageView = binding.profileImage

        // Contoh: Menampilkan data statis (ubah ini dengan data nyata)
        if (user != null){
            userNameTextView.text = user.displayName
            userEmailTextView.text = user.email
            // Menampilkan gambar profil menggunakan Glide (gunakan URL atau resource gambar)
            val profileImageUrl = user.photoUrl.toString()
            Glide.with(requireContext())
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_person_outline_24dp)
                .into(profileImageView)
        }

        // Button untuk logout
        binding.btnLogout.setOnClickListener {
            logOut()

        }

        return root

    }

    private fun logOut() {
        // Logout dari Firebase
        auth.signOut()

        // Hapus data login dari SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", AppCompatActivity.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

        // Redirect ke LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()  // Finish ProfileFragment agar tidak bisa kembali
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}