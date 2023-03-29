package com.singlerestaurant.user.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.utils.Common

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return getBinding().root
    }

    abstract fun getBinding(): T
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Init(view)
    }


    protected abstract fun Init(view: View)

    open fun openActivity(destinationClass: Class<*>?) {
        startActivity(Intent(requireActivity(), destinationClass))
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(requireActivity(), false)
        if (requireActivity() is DashboardActivity) {
            (requireActivity() as DashboardActivity).setCartCount()
        }
    }
}



