package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityDashboardBinding
import com.singlerestaurant.user.databinding.DlgConfomationBinding
import com.singlerestaurant.user.fragment.*
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getBooleanPref


open class DashboardActivity : BaseActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        if (intent.getStringExtra("pos") != null) {

            setFragment(intent.getStringExtra("pos")!!.toInt())

        } else {
            setFragment(1)

        }
        setCartCount()


    }

    fun setCartCount() {
        val count =
            SharePreference.getStringPref(this@DashboardActivity, SharePreference.BadgesCount)
        binding.tvBadgeCount.text =
            SharePreference.getStringPref(this@DashboardActivity, SharePreference.BadgesCount)
        if (count.isNullOrEmpty() || count == "0") {
            binding.tvBadgeCount.visibility = View.GONE
        } else {
            binding.tvBadgeCount.visibility = View.VISIBLE
        }
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.menu_home -> {


                setFragment(1)
            }
            R.id.menu_fav -> {
                if (getBooleanPref(this, SharePreference.isLogin)) {

                    setFragment(2)
                } else {
                    openActivity(LoginActivity::class.java)
                    finish()
                    finishAffinity()
                }
            }
            R.id.menu_cart -> {
                if (getBooleanPref(this, SharePreference.isLogin)) {

                    setFragment(3)

                } else {
                    openActivity(LoginActivity::class.java)
                    finish()
                    finishAffinity()
                }

            }
            R.id.menu_doce -> {
                if (getBooleanPref(this@DashboardActivity, SharePreference.isLogin)) {

                    setFragment(4)

                } else {
                    openActivity(LoginActivity::class.java)
                    finish()
                    finishAffinity()
                }
            }

            R.id.menu_profile -> {
                setFragment(5)

            }
        }
    }

    @SuppressLint("NewApi")
    fun setFragment(pos: Int) {
        binding.ivHome.imageTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.gray, null))
        binding.ivFav.imageTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.gray, null))
        binding.ivCart.imageTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.gray, null))
        binding.ivDoce.imageTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.gray, null))
        binding.ivProfile.imageTintList =
            ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.gray, null))
        val fragment = supportFragmentManager.findFragmentById(R.id.FramFragment)

        setState(pos)

        when (pos) {
            1 -> {

                binding.ivHome.imageTintList =
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.black,
                            null
                        )
                    )

                if (fragment !is HomeFragment) {
                    replaceFragment(HomeFragment())

                }
            }
            2 -> {

                binding.ivFav.imageTintList =
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.black,
                            null
                        )
                    )

                if (fragment !is FavouriteFragment) {
                    replaceFragment(FavouriteFragment())
                }
            }
            3 -> {

                binding.ivCart.imageTintList =
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.black,
                            null
                        )
                    )

                if (fragment !is CartFragment) {
                    replaceFragment(CartFragment())

                }
            }
            4 -> {

                binding.ivDoce.imageTintList =
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.black,
                            null
                        )
                    )
                if (fragment !is OrderHistoryFragment) {
                    replaceFragment(OrderHistoryFragment())
                }
            }
            5 -> {

                binding.ivProfile.imageTintList =
                    ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.black,
                            null
                        )
                    )

                if (fragment !is SettingFragment) {

                    replaceFragment(SettingFragment())
                }
            }
        }
    }

    private fun setState(pos: Int) {
        when (pos) {
            1 -> {
                binding.ivHome.isSelected = true
                binding.ivFav.isSelected = false
                binding.ivCart.isSelected = false
                binding.ivDoce.isSelected = false
                binding.ivProfile.isSelected = false

            }
            2 -> {
                binding.ivHome.isSelected = false
                binding.ivFav.isSelected = true
                binding.ivCart.isSelected = false
                binding.ivDoce.isSelected = false
                binding.ivProfile.isSelected = false
            }
            3 -> {
                binding.ivHome.isSelected = false
                binding.ivFav.isSelected = false
                binding.ivCart.isSelected = true
                binding.ivDoce.isSelected = false
                binding.ivProfile.isSelected = false
            }
            4 -> {
                binding.ivHome.isSelected = false
                binding.ivFav.isSelected = false
                binding.ivCart.isSelected = false
                binding.ivDoce.isSelected = true
                binding.ivProfile.isSelected = false
            }
            5 -> {
                binding.ivHome.isSelected = false
                binding.ivFav.isSelected = false
                binding.ivCart.isSelected = false
                binding.ivDoce.isSelected = false
                binding.ivProfile.isSelected = true
            }
        }

    }

    @SuppressLint("WrongConstant")
    fun replaceFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.FramFragment, fragment)
            commit()
        }


    override fun onBackPressed() {
        mExitDialog()
    }


    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@DashboardActivity, false)

    }

    private fun mExitDialog() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(this@DashboardActivity, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mView = DlgConfomationBinding.inflate(layoutInflater)
            val finalDialog: Dialog = dialog
            mView.tvYes.setOnClickListener {
                finalDialog.dismiss()
                ActivityCompat.finishAfterTransition(this@DashboardActivity)
                ActivityCompat.finishAffinity(this@DashboardActivity);
                finish()
            }
            mView.tvNo.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(mView.root)

            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}