package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityEditprofileBinding
import com.singlerestaurant.user.model.EditProfileResponse
import com.singlerestaurant.user.model.GetProfileResponse
import com.singlerestaurant.user.model.getprofiledata
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Common.isProfileEdit
import com.singlerestaurant.user.utils.Common.isProfileMainEdit
import com.singlerestaurant.user.utils.Common.setImageUpload
import com.singlerestaurant.user.utils.Common.setRequestBody
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.SharePreference
import com.github.dhaval2404.imagepicker.ImagePicker
import com.singlerestaurant.user.utils.Constants
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

@Suppress("DEPRECATION")
class EditProfileActivity : BaseActivity() {
    private val SELECT_FILE = 201
    private val REQUEST_CAMERA = 202
    private var mSelectedFileImg: File? = null
    private lateinit var binding: ActivityEditprofileBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding = ActivityEditprofileBinding.inflate(layoutInflater)

        setProfileData()
        /*    if (isCheckNetwork(this@EditProfileActivity)) {
                if (SharePreference.getBooleanPref(this@EditProfileActivity,SharePreference.isLogin)) {
                    val hasmap = HashMap<String, String>()
                    hasmap["user_id"] = SharePreference.getStringPref(this@EditProfileActivity, SharePreference.userId)!!
                    callApiProfile(hasmap)
                }else
                {
                    openActivity(LoginActivity::class.java)
                    finish()
                    finishAffinity()
                }




            } else {
                alertErrorOrValidationDialog(
                        this@EditProfileActivity,
                        resources.getString(R.string.no_internet)
                )
            }*/

        if (SharePreference.getStringPref(
                this@EditProfileActivity,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F

        }
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvUpdate -> {

                if(ApiClient.System_environment== Constants.SendBox)
                {
                    Common.showErrorFullMsg(this@EditProfileActivity, resources.getString(R.string.send_box_error_alert))
                }else {

                    if (binding.edUserName.text.toString() == "") {
                        Common.showErrorFullMsg(
                            this@EditProfileActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else {
                        if (isCheckNetwork(this@EditProfileActivity)) {
                            callEditProfileApi()

                        } else {
                            alertErrorOrValidationDialog(
                                this@EditProfileActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                }
            }
            R.id.ivGellary -> {

                ImagePicker.with(this)
                    .cropSquare()
                    .compress(1024)
                    .saveDir(
                        File(
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            resources.getString(R.string.app_name)
                        )
                    )
                    .maxResultSize(1080, 1080)
                    .createIntent { intent ->
                        startActivityForResult(intent, SELECT_FILE)
                    }
            }

        }
    }

    private fun callApiProfile(hasmap: HashMap<String, String>) {
        showLoadingProgress(this@EditProfileActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getProfile(hasmap)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()

                    val restResponse: GetProfileResponse = response.body
                    if (restResponse.status == 1) {
                        val dataResponse: getprofiledata = restResponse.data!!
                        setProfileData()
                    } else if (restResponse.data!!.equals("0")) {
                        alertErrorOrValidationDialog(
                            this@EditProfileActivity,
                            restResponse.message
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@EditProfileActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@EditProfileActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@EditProfileActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    private fun setProfileData() {
        binding.edEmailAddress.setText(
            SharePreference.getStringPref(
                this@EditProfileActivity,
                SharePreference.userEmail
            )
        )
        binding.edUserName.setText(
            SharePreference.getStringPref(
                this@EditProfileActivity,
                SharePreference.userName
            )
        )
        binding.tvMobileNumber.text =
            SharePreference.getStringPref(this@EditProfileActivity, SharePreference.userMobile)
        val profileImage =
            SharePreference.getStringPref(this@EditProfileActivity, SharePreference.userProfile)
        Glide.with(this@EditProfileActivity).load(profileImage)
            .apply(RequestOptions.circleCropTransform()).transition(
            DrawableTransitionOptions.withCrossFade(500)
        ).into(binding.ivProfile)
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                val fileUri = data?.data!!
                fileUri.path.let { mSelectedFileImg = File(it) }
                Glide.with(this@EditProfileActivity).load(fileUri.path)
                    .apply(RequestOptions.circleCropTransform())
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(binding.ivProfile)
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data!!)
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this@EditProfileActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!["data"] as Bitmap?
        val bytes = ByteArrayOutputStream()
        thumbnail!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

        mSelectedFileImg = File(
            Environment.getExternalStorageDirectory(),
            System.currentTimeMillis().toString() + ".jpeg"
        )
        val fo: FileOutputStream
        try {
            mSelectedFileImg!!.createNewFile()
            fo = FileOutputStream(mSelectedFileImg)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Glide.with(this@EditProfileActivity)
            .load(Uri.parse("file://" + mSelectedFileImg!!.path))
            .apply(RequestOptions.circleCropTransform())
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .into(binding.ivProfile)
    }


    private fun callEditProfileApi() {
        showLoadingProgress(this@EditProfileActivity)

        val call = if (mSelectedFileImg != null) {
            ApiClient.getClient.setProfile(
                setRequestBody(
                    SharePreference.getStringPref(
                        this@EditProfileActivity,
                        SharePreference.userId
                    )!!
                ),
                setRequestBody(binding.edUserName.text.toString()),
                setImageUpload("image", mSelectedFileImg!!)
            )
        } else {
            ApiClient.getClient.setProfile(
                setRequestBody(
                    SharePreference.getStringPref(
                        this@EditProfileActivity,
                        SharePreference.userId
                    ).toString()
                ), setRequestBody(binding.edUserName.text.toString()), null
            )
        }
        call.enqueue(object : Callback<EditProfileResponse> {


            override fun onResponse(
                call: Call<EditProfileResponse>,
                response: Response<EditProfileResponse>
            ) {
                dismissLoadingProgress()

                val editProfileResponse = response.body()
                if (editProfileResponse?.status == 1) {
                    dismissLoadingProgress()
                    isProfileEdit = true
                    isProfileMainEdit = true
                    Common.setProfileData(this@EditProfileActivity, editProfileResponse.data)
                    successfulDialog(this@EditProfileActivity, editProfileResponse.message)
                } else if (editProfileResponse?.status == 0) {
                    dismissLoadingProgress()
                    alertErrorOrValidationDialog(
                        this@EditProfileActivity,
                        editProfileResponse.message
                    )
                }

            }

            override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@EditProfileActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    private fun successfulDialog(act: Activity, msg: String?) {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mInflater = LayoutInflater.from(act)
            val mView = mInflater.inflate(R.layout.dlg_validation, null, false)
            val textDesc: TextView = mView.findViewById(R.id.tvMessage)
            textDesc.text = msg
            val tvOk: TextView = mView.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()
                finish()
            }
            dialog.setContentView(mView)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("error", e.message.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@EditProfileActivity, false)
    }
}