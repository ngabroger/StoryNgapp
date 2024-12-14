package com.ngabroger.storyngapp.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.ngabroger.storyngapp.R
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.databinding.ActivityMapsBinding
import com.ngabroger.storyngapp.viewmodel.MapsViewModel
import com.ngabroger.storyngapp.viewmodel.MapsViewModelFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val  factory = MapsViewModelFactory.getInstance(this)
        val viewModel =ViewModelProvider(this, factory)[MapsViewModel::class.java]
        viewModel.fetchStoriesWithLocation()



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getMyLocation()
        observeViewModel()
        setMapStyle()
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }catch (e: Exception){
            Log.e(TAG, "Can't find style. Error: ", e)
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){ isGranted : Boolean ->
            if(isGranted){
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true

        }
            else{
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun observeViewModel(){
        val viewModel = ViewModelProvider(this, MapsViewModelFactory.getInstance(this))[MapsViewModel::class.java]
       viewModel.storyResult.observe(this){result->
            when(result){
                is Result.Error -> {

                }
                Result.Loading -> {

                }
                is Result.Success -> {
                    val stories = result.data
                    for (story in stories) {
                        val position = LatLng(story.lat as Double, story.lon as Double)
                        mMap.addMarker(MarkerOptions().position(position).title("${story.name}: ${story.description}"))                    }
                    if (stories.isNotEmpty()) {
                        val firstStory = stories[0]
                        val firstPosition = LatLng(firstStory.lat as Double,
                            firstStory.lon as Double
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 10f))
                    }
                }
            }
        }

    }
    companion object{
        private const val TAG = "MapsActivity"

    }

}