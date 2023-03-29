@file:Suppress("DEPRECATION")

package com.singlerestaurant.user.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.activity.LoginActivity
import com.singlerestaurant.user.model.getprofiledata
import com.singlerestaurant.user.utils.SharePreference.Companion.SELECTED_LANGUAGE
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.isCurrancy
import com.singlerestaurant.user.utils.SharePreference.Companion.setBooleanPref
import com.singlerestaurant.user.utils.SharePreference.Companion.setStringPref
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object Common {
    var isProfileEdit: Boolean = false
    var isProfileMainEdit: Boolean = false
    var isCartTrue: Boolean = false
    var isCartTrueOut: Boolean = false
    var isCancelledOrder: Boolean = false
    var isAddOrUpdated: Boolean = false


    fun getToast(activity: Activity, strTxtToast: String) {
        Toast.makeText(activity, strTxtToast, Toast.LENGTH_SHORT).show()
    }

    fun getLog(strKey: String, strValue: String) {
        Log.e(">>>---  $strKey  ---<<<", strValue)
    }

    fun isValidEmail(strPattern: String): Boolean {
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(strPattern).matches();
    }

    fun isValidAmount(strPattern: String): Boolean {
        return Pattern.compile(
            "^[0-9]+([.][0-9]{2})?\$"
        ).matcher(strPattern).matches();
    }

    fun isValidNumber(strPattern: String): Boolean {
        return Pattern.compile(
            "0123456789."
        ).matcher(strPattern).matches()
    }

    @SuppressLint("MissingPermission")
    fun isCheckNetwork(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun openActivity(activity: Activity, destinationClass: Class<*>?) {
        activity.startActivity(Intent(activity, destinationClass))
        activity.overridePendingTransition(R.anim.fad_in, R.anim.fad_out)
    }

    var dialog: Dialog? = null

    fun dismissLoadingProgress() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    fun showLoadingProgress(context: Activity) {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(R.layout.dlg_progress)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    fun alertErrorOrValidationDialog(act: Activity, msg: String?) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
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
            }
            dialog.setContentView(mView)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun settingDialog(act: Activity) {
        var dialog: Dialog? = null
        try {
            if (dialog != null) {
                dialog.dismiss()
                dialog = null
            }
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mInflater = LayoutInflater.from(act)
            val mView = mInflater.inflate(R.layout.dlg_setting, null, false)
            val tvOkSetting = mView.findViewById<AppCompatTextView>(R.id.tvOkSetting)
            val finalDialog: Dialog = dialog
            tvOkSetting.setOnClickListener {
                var i = Intent()
                i.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.data = android.net.Uri.parse("package:" + act.getPackageName())
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                act.startActivity(i)
                dialog.dismiss()
                finalDialog.dismiss()
            }
            dialog.setContentView(mView)
            if (!act.isFinishing) dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun setLogout(activity: Activity) {
        val getLanguage = getStringPref(activity, SELECTED_LANGUAGE)
        val isTutorialsActivity: Boolean =
            SharePreference.getBooleanPref(activity, SharePreference.isTutorial)
        val loginUserType =
            SharePreference.getStringPref(activity, SharePreference.otpAddon) ?: ""
        val preference = SharePreference(activity)
        preference.mLogout()
        setBooleanPref(activity, SharePreference.isTutorial, isTutorialsActivity)
        setStringPref(activity, SharePreference.SELECTED_LANGUAGE, getLanguage!!)
        setStringPref(activity, SharePreference.otpAddon, loginUserType)
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    @SuppressLint("NewApi", "SimpleDateFormat")
    fun getDayAndMonth(strDate: String): String {
        val sd = SimpleDateFormat("dd-MM-yyyy")
        val sdout = SimpleDateFormat("dd-MMMM-yyyy")
        val sdday = SimpleDateFormat("EEEE")
        val date: Date = sd.parse(strDate)!!
        val getDay = sdday.format(date)
        val getDate = sdout.format(date)
        val stringArray = getDate.split("-").toTypedArray()
        val strDay = stringArray.get(0).plus("th")
        return getDay.plus(" ".plus(stringArray.get(1))).plus(" ".plus(strDay))
    }

    fun setImageUpload(strParameter: String, mSelectedFileImg: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            strParameter,
            mSelectedFileImg.name,
            RequestBody.create("image/*".toMediaType(), mSelectedFileImg)
        )
    }

    fun setRequestBody(bodyData: String): RequestBody {
        return bodyData.toRequestBody("text/plain".toMediaType())
    }


    fun getCurrancy(act: Activity): String {
        return getStringPref(act, isCurrancy)!!
    }


    fun getCurrentLanguage(context: Activity, isChangeLanguage: Boolean) {
        if (getStringPref(context, SELECTED_LANGUAGE) == null || getStringPref(
                context,
                SELECTED_LANGUAGE
            ).equals("", true)
        ) {
            setStringPref(
                context,
                SELECTED_LANGUAGE,
                context.resources.getString(R.string.language_english)
            )
        }
        val locale = if (getStringPref(
                context,
                SELECTED_LANGUAGE
            ).equals(context.resources.getString(R.string.language_english), true)
        ) {
            Locale("en-us")
        } else {
            Locale("ar")
        }
        //start
        val activityRes = context.resources
        val activityConf = activityRes.configuration
        val newLocale = locale
        if (getStringPref(
                context,
                SELECTED_LANGUAGE
            ).equals(context.resources.getString(R.string.language_english), true)
        ) {
            activityConf.setLocale(Locale("en-us")) // API 17+ only.
        } else {
            activityConf.setLocale(Locale("ar"))
        }
        activityRes.updateConfiguration(activityConf, activityRes.displayMetrics)
        val applicationRes = context.applicationContext.resources
        val applicationConf = applicationRes.configuration
        applicationConf.setLocale(newLocale)
        applicationRes.updateConfiguration(applicationConf, applicationRes.displayMetrics)

        if (isChangeLanguage) {
            val intent = Intent(context, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            context.finish()
            context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }


    fun getTimeFormat(time: String): String? {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = timeFormat.parse(time)

        val userTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return userTimeFormat.format(date)
    }


    fun showSuccessFullMsg(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showErrorFullMsg(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun getDate(strDate: String): String {
        val curFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val dateObj = curFormatter.parse(strDate)
        val postFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return postFormatter.format(dateObj!!)
    }

    fun closeKeyBoard(activity: Activity) {
        val view: View? = activity.currentFocus
        if (view != null) {
            try {
                val imm: InputMethodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    fun getPrices(currencyPos: String, currency: String, price: String): String {
        return if (currencyPos == "1") {
            currency.plus(String.format(Locale.US, "%,.02f", price.toDouble()))
        } else {
            String.format(Locale.US, "%,.02f", price.toDouble()).plus(currency)
        }
    }

     fun setProfileData(context: Activity, getProfile: getprofiledata?) {


        SharePreference.setStringPref(
            context,
            SharePreference.notificationStatus,
            getProfile?.isNotification.toString()
        )
        SharePreference.setStringPref(
            context,
            SharePreference.userProfile,
            getProfile?.profileImage.toString()
        )
         SharePreference.setStringPref(
             context,
             SharePreference.userRefralCode,
             getProfile?.referral_code.toString()
         )
        SharePreference.setStringPref(
            context,
            SharePreference.userEmail,
            getProfile?.email.toString()
        )
        SharePreference.setStringPref(
            context,
            SharePreference.userName,
            getProfile?.name.toString()
        )
        SharePreference.setStringPref(
            context,
            SharePreference.userMobile,
            getProfile?.mobile.toString()
        )
        SharePreference.setStringPref(
            context,
            SharePreference.userId,
            getProfile?.id.toString()
        )

        SharePreference.setStringPref(
            context,
            SharePreference.wallet,
            getProfile?.wallet ?: "00"
        )

        SharePreference.setStringPref(
            context,
            SharePreference.emailStatus,
            getProfile?.isMail.toString()
        )
         SharePreference.setStringPref(context, SharePreference.loginType,getProfile?.loginType.toString())
    }


    @SuppressLint("PackageManagerGetSignatures")
    fun printKeyHash(context: Activity): String? {
        val packageInfo: PackageInfo
        var key: String? = null
        try {
            //getting application package name, as defined in manifest
            val packageName = context.applicationContext.packageName

            //Retriving package info
            packageInfo = context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            Log.e("Package Name=", context.applicationContext.packageName)
            for (signature in packageInfo.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                key = String(Base64.encode(md.digest(), 0))

                // String key = new String(Base64.encodeBytes(md.digest()));
                //getLog("Key Hash", key)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("Name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("No such an algorithm", e.toString())
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
        return key
    }

}