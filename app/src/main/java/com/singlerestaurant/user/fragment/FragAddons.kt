package com.singlerestaurant.user.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singlerestaurant.user.adaptor.AddOnsViewAdapter
import com.singlerestaurant.user.databinding.FragAddonsBinding
import com.singlerestaurant.user.model.AddOnsDataModel
import java.util.*
import kotlin.collections.ArrayList


class FragAddons : BottomSheetDialogFragment() {

    private lateinit var binding:FragAddonsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragAddonsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val name=arguments?.getString("addOnsName")
        val price=arguments?.getString("addOnsPrice")

        val nameList=name?.split(',')?.map { it }?.toTypedArray()
        val priceList=price?.split(',')?.map { it }?.toTypedArray()
        Log.e("nameList",nameList.toString())
        Log.e("price",price.toString())

        val addOnList=ArrayList<AddOnsDataModel>()

        for(i in 0 until nameList?.size!!)
        {
            addOnList.add(AddOnsDataModel(0,nameList.get(i),priceList?.get(i),false))
        }
        binding.rvAddOns.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = AddOnsViewAdapter(requireActivity(), addOnList)
            isNestedScrollingEnabled=true
        }
/*
     val   addonsList = arguments?.getParcelableArrayList<AddOnsDataModel>("addons") as ArrayList<AddOnsDataModel>

        binding.rvAddOns.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = AddOnsListAdapter(requireActivity(), addonsList, true)
            isNestedScrollingEnabled=true
        }*/

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

}