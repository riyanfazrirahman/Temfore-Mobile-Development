package com.capstone.temfore.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class ProfileViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference.child("profile_pictures/${auth.currentUser?.uid}.jpg")
    }

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _profilePictureUri = MutableLiveData<Uri?>()
    val profilePictureUri: LiveData<Uri?> = _profilePictureUri

    private val _isOperationSuccessful = MutableLiveData<Boolean>()
    val isOperationSuccessful: LiveData<Boolean> = _isOperationSuccessful

    private val _isDeleteSuccessful = MutableLiveData<Boolean>()
    val isDeleteSuccessful: LiveData<Boolean> = _isDeleteSuccessful

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        _user.value = auth.currentUser
    }

    fun updateProfilePicture(bitmap: Bitmap?) {
        _isLoading.value = true
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            storageReference.putBytes(data)
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        updateFirebaseUserProfile(uri)
                    }
                }
                .addOnFailureListener {
                    _isOperationSuccessful.value = false
                }
        }
    }

    fun updateProfilePicture(uri: Uri?) {
        _isLoading.value = true
        uri?.let {
            storageReference.putFile(it)
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateFirebaseUserProfile(downloadUri)
                    }
                }
                .addOnFailureListener {
                    _isOperationSuccessful.value = false
                }
        }
    }

    private fun updateFirebaseUserProfile(photoUri: Uri) {
        _isLoading.value = true
        val user = auth.currentUser
        user?.updateProfile(userProfileChangeRequest { this.photoUri = photoUri })
            ?.addOnCompleteListener { task ->
                _isLoading.value = false
                _isOperationSuccessful.value = task.isSuccessful
                if (task.isSuccessful) {
                    _profilePictureUri.value = photoUri
                }
            }
    }

    fun deleteProfilePicture() {
        _isLoading.value = true
        storageReference.delete()
            .addOnSuccessListener {
                val user = auth.currentUser
                user?.updateProfile(userProfileChangeRequest { photoUri = null })
                    ?.addOnCompleteListener { task ->
                        _isLoading.value = false
                        _isDeleteSuccessful.value = task.isSuccessful
                        if (task.isSuccessful) {
                            _profilePictureUri.value = null
                        }
                    }
            }
            .addOnFailureListener {
                _isDeleteSuccessful.value = false
            }
    }

    fun updateUsername(newUsername: String) {
        val user = auth.currentUser
        user?.updateProfile(userProfileChangeRequest { displayName = newUsername })
            ?.addOnCompleteListener { task ->
                _isOperationSuccessful.value = task.isSuccessful
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                }
            }
    }

    fun logOut() {
        auth.signOut()
    }
}
