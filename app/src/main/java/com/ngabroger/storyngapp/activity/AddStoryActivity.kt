package com.ngabroger.storyngapp.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.alperenbabagil.simpleanimationpopuplibrary.SapDialog
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.databinding.ActivityAddStoryBinding
import com.ngabroger.storyngapp.viewmodel.StoryModel
import com.ngabroger.storyngapp.viewmodel.StoryModelFactory
import java.io.ByteArrayOutputStream
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private var currentImageFile: File? = null


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (cameraGranted && storageGranted) {
            // Izin diberikan, bisa melanjutkan ke pemilihan gambar
            openImageChooser()
        } else {
            Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show()
            finish() // Menutup activity jika izin ditolak
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = StoryModelFactory.getInstance(this)
        val viewModel = ViewModelProvider(this, factory)[StoryModel::class.java]


        viewModel.postStoryResult.observe(this){
            when(it){
                is Result.Error -> {
                    binding.loadingItem.visibility = View.GONE
                    SapDialog(this).apply {
                        titleText = "Not Valid"
                        messageText = it.error
                        isCancellable = true
                    }.build().show()
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

        binding.sendButton.setOnClickListener{
            val description = binding.edDescriptionText.toString()
            if (description.isEmpty()){
                SapDialog(this).apply {
                    titleText = "Not Valid"
                    messageText = "Description cannot be empty"
                    isCancellable = true
                }.build().show()

            }else{
                currentImageFile?.let { it1 -> viewModel.postStory(description, it1.path) } ?: run {
                    SapDialog(this).apply {
                        titleText = "Not Valid"
                        messageText = "Please choose an image"
                        isCancellable = true
                    }.build().show()
                }
                Log.e("ERROR", " ${currentImageFile?.path}")
            }
        }

    }





    private fun checkPermissionsAndOpenChooser() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )


        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            openImageChooser()
        } else {

            requestPermissionLauncher.launch(permissions)
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

    private fun openImageChooser() {

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val imageFile = createImageFile()
        currentImageUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            imageFile
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
        }
        currentImageFile = imageFile


        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image Source")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        launcherChooser.launch(chooserIntent)
    }

    private fun compressImage(imageFile: File?) {
        imageFile?.let {
            val bitmap = BitmapFactory.decodeFile(it.path)
            val outputStream = ByteArrayOutputStream()


            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            var quality = 80
            while (outputStream.size() > 1_000_000) { // Maksimum 1 MB
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
        }
    }
}