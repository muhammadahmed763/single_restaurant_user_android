package com.singlerestaurant.user.activity

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActConfirmAddressBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.showErrorFullMsg
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.singlerestaurant.user.utils.Constants
import kotlinx.coroutines.launch
import java.util.*

class ActConfirmAddress : BaseActivity() {
    private lateinit var binding:ActConfirmAddressBinding
    override fun setLayout(): View = binding.root
    private var saveAS: String = ""
    private var lat = ""
    private var long = ""
    var type = ""
    private var addressId = "0"
    private var addressType = ""
    private var isClick = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun InitView() {
        binding= ActConfirmAddressBinding.inflate(layoutInflater)
        type = intent.getStringExtra("Type").toString()
        if (type == "1") {
            isClick = true
            getData()
        }

        init()


        if (SharePreference.getStringPref(
                this@ActConfirmAddress,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }

        getLocationPermission()

        if(ApiClient.System_environment==Constants.SendBox)
        {
            sendBoxSetupData()
        }
    }

    private fun sendBoxSetupData()
    {

            binding.tvAddressTitle.text="New York"
            binding.tvAddress.text="20 Cooper Square, New York, NY 10003, USA"
            lat="40.727949"
            long="-73.991533"

            binding.edCompleteAddress.setText("20 Cooper Square, New York, NY 10003, USA")
            binding.edHouse.setText("20")
            binding.edApartment.setText("Cooper Square")


            binding.btnHome.background = getDrawable(R.drawable.black_rect2)
            binding.btnoffice.background = getDrawable(R.drawable.white_rect2)
            binding.btnother.background = getDrawable(R.drawable.white_rect2)
            binding.btnHome.setTextColor(ResourcesCompat.getColor(resources,R.color.white,null))
            binding.btnoffice.setTextColor(ResourcesCompat.getColor(resources,R.color.black,null))
            binding.btnother.setTextColor(ResourcesCompat.getColor(resources,R.color.black,null))
            saveAS = "1"

    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        binding.ivBack.setOnClickListener { finish() }

        lat = intent.getStringExtra("lat").toString()
        long = intent.getStringExtra("lang").toString()
        addressId = intent.getStringExtra("address_id").toString()
        binding.btnHome.setOnClickListener {
            binding.btnHome.background = getDrawable(R.drawable.black_rect2)
            binding.btnoffice.background = getDrawable(R.drawable.white_rect2)
            binding. btnother.background = getDrawable(R.drawable.white_rect2)
            binding. btnHome.setTextColor(getColor(R.color.white))
            binding. btnoffice.setTextColor(getColor(R.color.black))
            binding. btnother.setTextColor(getColor(R.color.black))
            saveAS = "1"
        }
        binding.btnoffice.setOnClickListener {
            binding. btnHome.background = getDrawable(R.drawable.white_rect2)
            binding.btnoffice.background = getDrawable(R.drawable.black_rect2)
            binding.  btnother.background = getDrawable(R.drawable.white_rect2)
            binding. btnHome.setTextColor(getColor(R.color.black))
            binding. btnoffice.setTextColor(getColor(R.color.white))
            binding.  btnother.setTextColor(getColor(R.color.black))
            saveAS = "2"
        }
        binding. btnother.setOnClickListener {
            binding. btnHome.background = getDrawable(R.drawable.white_rect2)
            binding.btnoffice.background = getDrawable(R.drawable.white_rect2)
            binding. btnother.background = getDrawable(R.drawable.black_rect2)
            binding. btnHome.setTextColor(getColor(R.color.black))
            binding. btnoffice.setTextColor(getColor(R.color.black))
            binding.  btnother.setTextColor(getColor(R.color.white))
            saveAS = "3"
        }

        binding.  btnsave.setOnClickListener {
            if (binding.edCompleteAddress.text.isNullOrEmpty()) {
                showErrorFullMsg(this@ActConfirmAddress, resources.getString(R.string.validation_all))
            } else if (binding.edHouse.text.isNullOrEmpty()) {
                showErrorFullMsg(this@ActConfirmAddress, resources.getString(R.string.validation_all))
            } else if (binding.edApartment.text.isNullOrEmpty()) {
                showErrorFullMsg(this@ActConfirmAddress, resources.getString(R.string.validation_all))

            }  else if (saveAS.isEmpty()) {
                showErrorFullMsg(
                    this@ActConfirmAddress,
                    resources.getString(R.string.validation_all)
                )

            } else {
                val addadrress = HashMap<String, String>()
                addadrress["user_id"] = SharePreference.getStringPref(this@ActConfirmAddress, SharePreference.userId) ?: ""
                addadrress["address_type"] = saveAS
                addadrress["address"] = binding.edCompleteAddress.text.toString()
                addadrress["area"] = binding.edApartment.text.toString()
                addadrress["house_no"] = binding.edHouse.text.toString()
                addadrress["lat"] = lat
                addadrress["lang"] = long

                if (type == "1") {
                    addadrress["address_id"] = intent.getStringExtra("address_id").toString()
                    Log.e("request", addadrress.toString())
                    callApiUpdateAddress(addadrress)
                } else {
                    callApiAddAddress(addadrress)
                }
            }
        }
    }



    private fun getLocationPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if(intent.getStringExtra("SecondAddress")?.isNotEmpty() == true)
                    {
                        binding.tvAddress.text=intent.getStringExtra("SecondAddress")
                        binding.tvAddressTitle.text=intent.getStringExtra("FirstAddress")


                    }else
                    {
                        if (report.areAllPermissionsGranted()) {
                            getCurrentLocation()
                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Common.settingDialog(this@ActConfirmAddress)
                        }
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
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        val geocoder = Geocoder(this@ActConfirmAddress, Locale.getDefault())
        var addresses: List<Address>

        LocationServices.getFusedLocationProviderClient(this@ActConfirmAddress)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@ActConfirmAddress)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0) {
                        val locIndex = locationResult.locations.size - 1

                        val latitude = locationResult.locations[locIndex].latitude
                        val longitude = locationResult.locations[locIndex].longitude
                        lat = latitude.toString()
                        long = longitude.toString()

                        addresses = geocoder.getFromLocation(latitude, longitude, 1)!!

                        val address: String = addresses[0].getAddressLine(0)

                        binding.tvAddress.text=address

                    }
                }
            }, Looper.getMainLooper())

    }








    @RequiresApi(Build.VERSION_CODES.M)
    private fun getData() {
        binding.edCompleteAddress.setText(intent.getStringExtra("Address"))
        binding.edHouse.setText(intent.getStringExtra("houseNumber"))
        binding. edApartment.setText(intent.getStringExtra("area"))
        addressType = intent.getStringExtra("addressType").toString()
        when (addressType) {
            "1" -> {
                binding.btnHome.background = ResourcesCompat.getDrawable(resources,R.drawable.black_rect2,null)
                binding.btnoffice.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)
                binding. btnother.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)

                binding. btnHome.setTextColor(getColor(R.color.white))
                binding. btnoffice.setTextColor(getColor(R.color.black))
                binding. btnother.setTextColor(getColor(R.color.black))
                saveAS = "1"
            }
            "2" -> {
                binding. btnHome.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)
                binding.btnoffice.background = ResourcesCompat.getDrawable(resources,R.drawable.black_rect2,null)
                binding.btnother.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)
                binding. btnHome.setTextColor(getColor(R.color.black))
                binding. btnoffice.setTextColor(getColor(R.color.white))
                binding. btnother.setTextColor(getColor(R.color.black))
                saveAS = "2"
            }
            "3" -> {
                binding. btnHome.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)
                binding. btnoffice.background = ResourcesCompat.getDrawable(resources,R.drawable.white_rect2,null)
                binding. btnother.background = ResourcesCompat.getDrawable(resources,R.drawable.black_rect2,null)
                binding. btnHome.setTextColor(getColor(R.color.black))
                binding. btnoffice.setTextColor(getColor(R.color.black))
                binding. btnother.setTextColor(getColor(R.color.white))
                saveAS = "3"
            }
        }
    }





    //TODO API ADD ADDRESS CALL
    private fun callApiAddAddress(addadrress: HashMap<String, String>) {
        showLoadingProgress(this@ActConfirmAddress)
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().addAddress(addadrress)) {
                is NetworkResponse.Success -> {
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status==1) {
                        dismissLoadingProgress()
                        Common.isAddOrUpdated = true

                        finish()
                    } else if (restResponse.status==0) {
                        dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActConfirmAddress,
                            restResponse.message.toString()
                        )
                    } else {
                        dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@ActConfirmAddress,
                            restResponse.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }




    //TODO API UPDATE ADDRESS CALL
    private fun callApiUpdateAddress(addressRequest: HashMap<String, String>) {
        showLoadingProgress(this@ActConfirmAddress)
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().updateAddress(addressRequest)) {
                is NetworkResponse.Success -> {
                    Common.isAddOrUpdated = true
                    if (response.body.status==1) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showErrorFullMsg(
                            this@ActConfirmAddress,
                            response.body.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@ActConfirmAddress,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }




    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this, false)
    }
}