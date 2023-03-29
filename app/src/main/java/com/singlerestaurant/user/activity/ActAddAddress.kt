package com.singlerestaurant.user.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.ActAddAddressBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.openActivity
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.*

class ActAddAddress : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var addAddressBinding: ActAddAddressBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_CODE = 101
    private var map: GoogleMap? = null
    private var firstAddress: String = ""
    private var secondAddress: String = ""
    private var addressid: String = ""
    private var addressType: String = ""
    private var address: String = ""
    private var houseNumber: String = ""
    private var area: String = ""
    private var type: String = ""
    private var pageType: String = ""
    private var mapKey: String = ""
    private lateinit var driver: LatLng
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(0.0, 0.0)
    private val DEFAULT_ZOOM = 18f
    private var lat = ""
    private var long = ""

    private val cameraPosition = CameraPosition.Builder()
        .target(defaultLocation)
        .zoom(18f)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapKey = SharePreference.getStringPref(
            this@ActAddAddress,
            SharePreference.mapKey
        )!!
        addAddressBinding = ActAddAddressBinding.inflate(layoutInflater)
        setContentView(addAddressBinding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        addressType = intent.getStringExtra("addressType").toString()
        addressid = intent.getStringExtra("address_id").toString()
        address = intent.getStringExtra("Address").toString()
        houseNumber = intent.getStringExtra("houseNumber").toString()
        type = intent.getStringExtra("Type").toString()
        pageType = intent.getStringExtra("pageType").toString()
        area = intent.getStringExtra("area").toString()

        if (type == "1") {
            addAddressBinding.tvtitle.text = resources.getString(R.string.edit_address)
        } else {
            addAddressBinding.tvtitle.text = resources.getString(R.string.addaddress)
        }

        Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        val mapFragment =
                            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                        mapFragment?.getMapAsync(this@ActAddAddress)
                        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        Common.settingDialog(this@ActAddAddress)
                    }


                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()

        initClickListeners()


        if (SharePreference.getStringPref(
                this@ActAddAddress,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {

            addAddressBinding.ivBack.rotation = 180f
        } else {
            addAddressBinding.ivBack.rotation = 0f

        }


    }


    private fun initClickListeners() {
        addAddressBinding.ivBack.setOnClickListener {
            finish()
        }
        Places.initialize(this, resources.getString(R.string.google_free_api_key))
        addAddressBinding.btnsave.setOnClickListener {
            if (SharePreference.getBooleanPref(this@ActAddAddress,SharePreference.isLogin)) {
                val intent = Intent(this@ActAddAddress, ActConfirmAddress::class.java)
                intent.putExtra("FirstAddress", firstAddress)
                intent.putExtra("SecondAddress", secondAddress)
                intent.putExtra("lat", lat)
                intent.putExtra("lang", long)
                intent.putExtra("Address", address)
                intent.putExtra("addressType", addressType)
                intent.putExtra("address_id", addressid)
                intent.putExtra("houseNumber", houseNumber)
                intent.putExtra("area", area)
                intent.putExtra("Type", type)
                startActivity(intent)
                finish()
            }else
            {
                startActivity(Intent(this@ActAddAddress,LoginActivity::class.java))
                finish()
                finishAffinity()
            }


        }
        addAddressBinding.btnChange.setOnClickListener {
            val fields: List<Place.Field> = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG,
                Place.Field.PLUS_CODE
            )
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            ).build(this)
            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val place = Autocomplete.getPlaceFromIntent(result.data!!)
                        lat = place.latLng?.latitude.toString()
                        long = place.latLng?.longitude.toString()
                        driver = LatLng(place.latLng!!.latitude, place.latLng!!.longitude)
                        addAddressBinding.tvAddressfull.text = place.address!!.toString()


                        val cameraPosition = CameraPosition.Builder()
                            .target(
                                LatLng(
                                    lat.toDouble(),
                                    long.toDouble()
                                )
                            )
                            .zoom(12f)
                            .bearing(5f)
                            .build()

                        map?.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            1000,
                            null
                        )
                    }

                    AutocompleteActivity.RESULT_ERROR -> {
                        addAddressBinding.tvAddressfull.text = getString(R.string.error)
                    }
                    AutocompleteActivity.RESULT_CANCELED -> {
                        Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()

        googleMap?.setOnCameraIdleListener {
            val addresses: List<Address>?
            lat = googleMap.cameraPosition.target.latitude.toString()
            long = googleMap.cameraPosition.target.longitude.toString()


            val gcd = Geocoder(this@ActAddAddress, Locale.getDefault())
            try {
                addresses = gcd.getFromLocation(
                    lat.toDouble(),
                    long.toDouble(),
                    1
                )
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    val city = addresses[0].locality
                    val state = addresses[0].adminArea
                    val country = addresses[0].countryName
                    val knownName = addresses[0].featureName
                    lat = googleMap.cameraPosition.target.latitude.toString()
                    long = googleMap.cameraPosition.target.longitude.toString()
                    addAddressBinding.tvAddressfull.text = "$address $city  $state $country"
                    addAddressBinding.tvAddressArea.text = knownName
                    secondAddress = "$address $city  $state $country"
                    firstAddress = knownName
                    SharePreference.setStringPref(
                        this@ActAddAddress,
                        SharePreference.city,
                        firstAddress
                    )
                }
                if (addresses?.isNotEmpty() == true) {
                    println(addresses[0].locality)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //TODO egt lcoation permission
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {

                            lat = lastKnownLocation?.latitude.toString()
                            long = lastKnownLocation?.longitude.toString()
                            if (type == "1") {
                                lat = intent.getStringExtra("lat").toString()
                                long = intent.getStringExtra("lang").toString()
                                map?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lat.toDouble(),
                                            long.toDouble()
                                        ), DEFAULT_ZOOM
                                    )
                                )
                            } else {
                                map?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation?.latitude!!,
                                            lastKnownLocation?.longitude!!
                                        ), DEFAULT_ZOOM
                                    )
                                )
                            }
                        }
                    } else {
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("securityError", e.message.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this, false)
    }
}