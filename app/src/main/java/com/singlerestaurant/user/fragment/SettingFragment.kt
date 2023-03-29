package com.singlerestaurant.user.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.*
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.DlgLogoutBinding
import com.singlerestaurant.user.databinding.FragmentSettingBinding
import com.singlerestaurant.user.model.Admin
import com.singlerestaurant.user.model.GetProfileResponse
import com.singlerestaurant.user.model.getprofiledata
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.getCurrentLanguage
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getBooleanPref
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.singlerestaurant.user.utils.SharePreference.Companion.sharedPreferences

import kotlinx.coroutines.launch


class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    private lateinit var binding:FragmentSettingBinding


    override fun Init(view: View) {
        getCurrentLanguage(requireActivity(), false)

        if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
            val loginType = getStringPref(requireActivity(), SharePreference.loginType)
            if (loginType != "email" || getStringPref(requireActivity(), SharePreference.otpAddon) == "1") {
                binding.linearChangePassword.visibility = View.GONE
            }
        }


        if (!getBooleanPref(requireActivity(), SharePreference.isLogin)) {
            binding.tvLogout.text=resources.getString(R.string.login)
        }
        binding.editProfile.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                openActivity(EditProfileActivity::class.java)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }

        binding.linearManageAddress.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                val intent = Intent(requireContext(), GetAddressActivity::class.java)
                intent.putExtra("isComeFromSelectAddress", false)
                startActivity(intent)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }

        binding.linearTestiMonial.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                val intent = Intent(requireContext(),ActRattings::class.java)
                startActivity(intent)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }

        binding.linearChangePassword.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                    val intent = Intent(requireContext(),ChangePasswordActivity::class.java)
                    startActivity(intent)
            }
            else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }

        binding.linearNotification.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                val intent = Intent(requireContext(), ActNotificationSetting::class.java)
                startActivity(intent)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }


        binding.linearMYWallet.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                openActivity(ActMyWallet::class.java)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }



        binding.linearReferandEarn.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                openActivity(ReferAndEarnActivity::class.java)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }
        binding.linearChangeLayout.setOnClickListener {
            val dialog = BottomSheetDialog(requireActivity())
            val mView = layoutInflater.inflate(R.layout.row_bottomsheetlayout, null)
            val btnLTREng = mView.findViewById<TextView>(R.id.tvLTR)
            val btnRTLHindi = mView.findViewById<TextView>(R.id.tvRTL)
            val btnCancel = mView.findViewById<TextView>(R.id.tvCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            btnLTREng.setOnClickListener {
                SharePreference.setStringPref(
                    requireActivity(),
                    SharePreference.SELECTED_LANGUAGE,
                    requireActivity().resources.getString(R.string.language_english)
                )
                getCurrentLanguage(requireActivity(), true)
            }
            btnRTLHindi.setOnClickListener {
                SharePreference.setStringPref(
                    requireActivity(),
                    SharePreference.SELECTED_LANGUAGE,
                    requireActivity().resources.getString(R.string.language_hindi)
                )
                getCurrentLanguage(requireActivity(), true)
            }
            dialog.setCancelable(true)
            dialog.setContentView(mView)
            dialog.show()

        }
        binding.linearHelpContactus.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                openActivity(ContactUsActivity::class.java)
            } else {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }
        }

        binding.linearPrivacyPolicy.setOnClickListener {
            startActivity(Intent(activity, ActCmsPages::class.java).putExtra("title","privacypolicy"))
        }

        binding.linearAbout.setOnClickListener {
            startActivity(Intent(activity, ActCmsPages::class.java).putExtra("title", resources.getString(R.string.about_us)))
        }
        binding.linearLogout.setOnClickListener {
            if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                alertLogOutDialog()
            }else
            {
                openActivity(LoginActivity::class.java)
                requireActivity().finish()
                requireActivity().finishAffinity()
            }

        }

        binding.linearBookTable.setOnClickListener {
            startActivity(Intent(requireActivity(),ActBookTable::class.java))
        }

        binding.linearGallery.setOnClickListener {
            startActivity(Intent(requireActivity(),ActGallery::class.java))

        }

        binding.linearFaqs.setOnClickListener {
            startActivity(Intent(requireActivity(),ActFaq::class.java))

        }

        binding.linearBlogs.setOnClickListener {
            startActivity(Intent(requireActivity(),ActBlogs::class.java))
        }
        binding.linearTeam.setOnClickListener {
            startActivity(Intent(requireActivity(),ActOurTeam::class.java))

        }
    }

    private fun alertLogOutDialog() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(requireActivity(), R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mView =DlgLogoutBinding.inflate(layoutInflater)
            val finalDialog: Dialog = dialog
            mView.tvLogout.setOnClickListener {
                finalDialog.dismiss()
                Common.setLogout(requireActivity())
            }
            mView.tvCancel.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(mView.root)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }








    private fun setProfileData() {
        if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
            binding.tvUserName.text = getStringPref(requireContext(),SharePreference.userName)
            binding.tvEmail.text = getStringPref(requireActivity(), SharePreference.userEmail)
            Glide.with(requireActivity()).load(getStringPref(requireActivity(), SharePreference.userProfile)).apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(binding.ivProfile)

        } else {
            binding.tvUserName.text = resources.getString(R.string.menu_name)
            binding.ivProfile.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.ic_launcher,
                    null
                )
            )
        }
    }


    override fun onResume() {
        super.onResume()
        getCurrentLanguage(requireActivity(), false)
        setProfileData()
        /*if (getBooleanPref(requireActivity(), SharePreference.isLogin)) {
            if (Common.isCheckNetwork(requireActivity())) {
                val hasmap = HashMap<String, String>()
                hasmap["user_id"] = getStringPref(
                    requireActivity(),
                    SharePreference.userId
                ) ?: ""
                callApiProfile(hasmap)
            } else {
                Common.alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.no_internet)
                )
            }
        }
        else
        {
            binding.clSettings.visibility=View.VISIBLE

        }*/
    }

    override fun getBinding(): FragmentSettingBinding {
        binding= FragmentSettingBinding.inflate(layoutInflater)
        return binding
    }
}