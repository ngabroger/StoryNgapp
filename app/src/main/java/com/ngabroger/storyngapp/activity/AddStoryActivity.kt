package com.ngabroger.storyngapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.alperenbabagil.simpleanimationpopuplibrary.SapDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.databinding.ActivityAddStoryBinding
import com.ngabroger.storyngapp.viewmodel.StoryModel
import com.ngabroger.storyngapp.viewmodel.StoryModelFactory
import java.io.ByteArrayOutputStream
import java.io.File
import com.ngabroger.storyngapp.R

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private var currentImageFile: File? = null
    private var latLng: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true

        when {
            cameraGranted && storageGranted -> {

                openImageChooser(includeCamera = true, includeGallery = true)
            }
            !cameraGranted -> {

                showPermissionDeniedMessage("Camera permission is required.")
            }
            !storageGranted -> {
                openImageChooser(includeCamera = false, includeGallery = true)            }
            else ->{
                showPermissionDeniedMessage("Storage permission is required.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val factory = StoryModelFactory.getInstance(this)
        val viewModel = ViewModelProvider(this, factory)[StoryModel::class.java]


        viewModel.postStoryResult.observe(this){
            when(it){
                is Result.Error -> {
                    binding.loadingItem.visibility = View.GONE
                   showErrorDialog(it.error)
                }
                Result.Loading -> {
                    binding.loadingItem.visibility = View.VISIBLE
                    binding.loadingItem.progress = 0f
                    binding.loadingItem.addAnimatorUpdateListener {animator ->
                        if (animator.animatedValue as Float >= 0.3){
                            binding.loadingItem.loop(true)
                        }
                    }
                }
                is Result.Success -> {
                    binding.loadingItem.visibility = View.GONE
                    Toast.makeText(this, "Story posted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.chooseImage.setOnClickListener {
            checkPermissionsAndOpenChooser()
        }
        binding.backButton.setOnClickListener{
            onBackPressed()
        }

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermission()
            } else {
                latLng = null
            }
        }

        binding.sendButton.setOnClickListener {
            val description = binding.edDescriptionText.text.toString()
            if (description.isEmpty()) {
                showErrorDialog("Description cannot be empty")
            } else if (currentImageFile == null) {
                showErrorDialog("Please choose an image")
            } else {
                val location = if (binding.switchLocation.isChecked) latLng else null
                viewModel.postStory(description, currentImageFile!!.path, location)
            }
        }

    }

    private fun showPermissionDeniedMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latLng = LatLng(location.latitude, location.longitude)
                        Log.d("Location", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    } else {
                        Toast.makeText(this, "Unable to get last location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun checkPermissionsAndOpenChooser() {
        val cameraPermission = Manifest.permission.CAMERA
        val cameraGranted = checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED

        when {
            cameraGranted -> {
                openImageChooser(includeCamera = true, includeGallery = true)
            }
            else -> {

                requestPermissionLauncher.launch(arrayOf(cameraPermission))
            }
        }
    }
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File.createTempFile("temp_image", ".jpg", cacheDir)
        file.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return file
    }

    private fun showErrorDialog(message: String) {
        SapDialog(this).apply {
            titleText = "Not Valid"
            messageText = message
            isCancellable = true
        }.build().show()
    }

    private fun openImageChooser(includeCamera: Boolean, includeGallery: Boolean) {
        val intents = mutableListOf<Intent>()

        if (includeGallery) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intents.add(galleryIntent)
        }

        if (includeCamera) {
            val imageFile = createImageFile()
            currentImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                imageFile
            )
            currentImageFile = imageFile

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
            }
            intents.add(cameraIntent)
        }

        if (intents.isNotEmpty()) {
            val chooserIntent = Intent.createChooser(intents.removeAt(0), "Select Image Source")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
            launcherChooser.launch(chooserIntent)
        } else {
            Toast.makeText(this, "No options available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage(imageFile: File?) {
        imageFile?.let {
            val bitmap = BitmapFactory.decodeFile(it.path)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            var quality = 80
            while (outputStream.size() > 1_000_000) {
                outputStream.reset()
                quality -= 5
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val compressedImageFile = File(cacheDir, "compressed_image.jpg")
            compressedImageFile.writeBytes(outputStream.toByteArray())
            currentImageFile = compressedImageFile


        }
    }


    private fun createImageFile(): File {
        val fileName = "JPEG_${System.currentTimeMillis()}"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    private val launcherChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            if (data?.data != null) {

                currentImageUri = data.data
                currentImageFile = uriToFile(currentImageUri!!)
                binding.imageCurrent.setImageURI(currentImageUri)
            } else {

                binding.imageCurrent.setImageURI(currentImageUri)
            }

            compressImage(currentImageFile)
        } else {

            Log.d("Image Chooser", "No media selected")
            currentImageFile = null
            currentImageUri = null
            binding.imageCurrent.setImageResource(R.drawable.image_chooser)
        }
    }
}