package com.singlerestaurant.user.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.singlerestaurant.user.R
import com.singlerestaurant.user.utils.Common
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Common.getCurrentLanguage(this@BaseActivity, false)
        InitView()
        setContentView(setLayout())
    }

    protected abstract fun setLayout(): View
    protected abstract fun InitView()

    open fun openActivity(destinationClass: Class<*>) {
        startActivity(Intent(this@BaseActivity, destinationClass))
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fad_in, R.anim.fad_out)
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.fad_in, R.anim.fad_out)
        Common.getCurrentLanguage(this@BaseActivity, false)
    }
}