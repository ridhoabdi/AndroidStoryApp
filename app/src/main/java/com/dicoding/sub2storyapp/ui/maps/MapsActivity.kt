package com.dicoding.sub2storyapp.ui.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.data.local.datastore.UserPreference
import com.dicoding.sub2storyapp.data.remote.response.ListStoryItem
import com.dicoding.sub2storyapp.data.remote.response.StoryResponse
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.sub2storyapp.databinding.ActivityMapsBinding
import com.dicoding.sub2storyapp.ui.main.MainViewModel
import com.dicoding.sub2storyapp.ui.main.ViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModels : MainViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val TAG = "MapsActivity"
        var LAT = 0.0
        var LON = 0.0
    }

    private val _storyLocation = MutableLiveData<List<ListStoryItem>>()
    private val storyLocation: LiveData<List<ListStoryItem>> = _storyLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
        getStoryLocation()
    }

    private fun getStoryLocation() {
        mapsViewModels.getUser().observe(this) {
            if (it != null) {
                val client =
                    ApiConfig.getApiService().getStoriesWithLocation("Bearer " + it.token, 1)
                client.enqueue(object : Callback<StoryResponse> {
                    override fun onResponse(
                        call: Call<StoryResponse>,
                        response: Response<StoryResponse>
                    ) {
                        val responseBody = response.body()
                        if (response.isSuccessful && responseBody?.message == "Stories fetched successfully") {
                            _storyLocation.value = responseBody.listStory
                        }
                    }

                    override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                        Toast.makeText(
                            this@MapsActivity,
                            getString(R.string.failed_get_location),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            }
        }
    }

    private fun setupView() {
        mapsViewModels = ViewModelProvider(
            this,
            ViewModels(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setMapStyle()
        getMyLocation()

        val Tulungagung = LatLng(-8.091221, 111.964173)
        storyLocation.observe(this) {
            for (i in storyLocation.value?.indices!!) {
                val location = LatLng(
                    storyLocation.value?.get(i)?.lat!!,
                    storyLocation.value?.get(i)?.lon!!
                )
                mMap.addMarker(
                    MarkerOptions().position(location).title(
                        getString(R.string.story_uploaded_by) + storyLocation.value?.get(i)?.name
                    )
                )
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Tulungagung, 2f))
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLocation()
                }
                else -> {
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mMap.isMyLocationEnabled = true
                    showMyLocationMarker(location)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        getString(R.string.location_not_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )
        }
    }

    private fun showMyLocationMarker(location: Location) {
        LAT = location.latitude
        LON = location.longitude

        val startLocation = LatLng(LAT, LON)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .draggable(true)
                .title(getString(R.string.current_location))
        )
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }
}