package com.capstone.temfore.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.capstone.temfore.R
import com.capstone.temfore.databinding.FragmentProfileBinding
import com.capstone.temfore.ui.auth.resetpassword.ResetPasswordActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        observeViewModel()
        setupButtons()


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBarImg.visibility = View.VISIBLE
                binding.imageProfile.alpha = 0.5f // Transparansi saat loading
            } else {
                binding.progressBarImg.visibility = View.GONE
                binding.imageProfile.alpha = 1f // Kembali normal
            }
        }

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.userName.text = it.displayName
                binding.userEmail.text = it.email

                Glide.with(requireContext())
                    .load(it.photoUrl)
                    .placeholder(R.drawable.ic_person_outline_24dp)
                    .into(binding.imageProfile)
            }
        }

        viewModel.profilePictureUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.ic_person_outline_24dp)
                    .into(binding.imageProfile)
            }
        }

        viewModel.isOperationSuccessful.observe(viewLifecycleOwner) { isSuccessful ->
            val message = if (isSuccessful) {
                "Operasi berhasil"
            } else {
                "Operasi gagal"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isDeleteSuccessful.observe(viewLifecycleOwner) { isSuccessful ->
            val message = if (isSuccessful) {
                "Foto profil berhasil dihapus"
            } else {
                "Gagal menghapus foto profil"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        binding.btnLogout.setOnClickListener { logOut() }
        binding.btnProfileCamera.setOnClickListener { showProfileOptions() }
        binding.btnChangeUsername.setOnClickListener { showChangeUsernameDialog() }
        binding.btnResetPassword.setOnClickListener {
            val intent = Intent(requireContext(), ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showProfileOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_profile_options, null)

        // Bind views
        val btnCamera = view.findViewById<ImageView>(R.id.btnCamera)
        val btnGallery = view.findViewById<ImageView>(R.id.btnGallery)
        val btnDelete = view.findViewById<ImageView>(R.id.btnDelete)

        btnCamera.setOnClickListener {
            launchCamera()
            bottomSheetDialog.dismiss()
        }

        btnGallery.setOnClickListener {
            openGallery()
            bottomSheetDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            viewModel.deleteProfilePicture()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                viewModel.updateProfilePicture(it)
            }
        }

    private fun launchCamera() {
        takePictureLauncher.launch()
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.updateProfilePicture(it)
            }
        }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    @SuppressLint("InflateParams")
    private fun showChangeUsernameDialog() {
        // Implementation of change username dialog
        val dialogView = android.view.LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_username, null)
        val inputUsername = dialogView.findViewById<android.widget.EditText>(R.id.inputUsername)

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Ganti Username")
            setView(dialogView)
            setPositiveButton("Simpan") { _, _ ->
                val newUsername = inputUsername.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    viewModel.updateUsername(newUsername)
                } else {
                    Toast.makeText(context, "Username tidak boleh kosong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun logOut() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Apakah Anda yakin ingin keluar?")

        builder.setPositiveButton("Ya") { dialog, _ ->
            // Jika pengguna menekan "Ya", logout dilakukan
            viewModel.logOut()

            val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

            val intent = Intent(
                requireContext(),
                com.capstone.temfore.ui.auth.login.LoginActivity::class.java
            )
            startActivity(intent)
            requireActivity().finish()
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            // Jika pengguna menekan "Batal", dialog ditutup
            dialog.dismiss()
        }

        // Tampilkan dialog
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
