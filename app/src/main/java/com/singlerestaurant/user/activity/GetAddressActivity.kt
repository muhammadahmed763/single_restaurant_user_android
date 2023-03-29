package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.databinding.ActivityGetAddressBinding
import com.singlerestaurant.user.model.AddressResponse
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.model.ListResponse
import com.singlerestaurant.user.utils.Constants
import kotlinx.coroutines.launch


class GetAddressActivity : BaseActivity() {
    private lateinit var binding: ActivityGetAddressBinding
    var addressList = ArrayList<AddressResponse>()
    var addressAdapter: BaseAdaptor<AddressResponse>? = null
    override fun setLayout(): View = binding.root
    override fun InitView() {

        binding = ActivityGetAddressBinding.inflate(layoutInflater)
        setupGetAddressAdapter(addressList)
        callApiGetAddressList()


        binding.ivBack.setOnClickListener {
            finish()
        }

        if (SharePreference.getStringPref(
                this@GetAddressActivity,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {

            binding.ivBack.rotation = 180f
        } else {
            binding.ivBack.rotation = 0f

        }

        binding.btnAddNewAddress.setOnClickListener {
            if (ApiClient.System_environment == Constants.SendBox) {
                val intent = Intent(this@GetAddressActivity, ActConfirmAddress::class.java)
                intent.putExtra("Type", 0)
                startActivity(intent)
            } else {
                val intent = Intent(this@GetAddressActivity, ActAddAddress::class.java)
                intent.putExtra("Type", 0)
                startActivity(intent)
            }


        }
    }


    private fun checkDeliveryZone(data: AddressResponse) {
        Common.showLoadingProgress(this@GetAddressActivity)
        val request = HashMap<String, String>()
        request["lat"] = data.lat.toString()
        request["lang"] = data.lang.toString()
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().checkDeliveryZone(request)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {


                        val intent =
                            Intent(this@GetAddressActivity, OrderSummeryActivity::class.java)
                        intent.putExtra("houseNumber", data.building.toString())
                        intent.putExtra("Address", data.address.toString())
                        intent.putExtra("addressType", data.addressType.toString())
                        intent.putExtra("address_id", data.id.toString())
                        intent.putExtra("lat", data.lat.toString())
                        intent.putExtra("long", data.lang.toString())
                        intent.putExtra("area", data.area.toString())
                        intent.putExtra("Type", 1)
                        setResult(500, intent)
                        finish()

                    } else if (response.body.status == 2) {
                        Common.alertErrorOrValidationDialog(
                            this@GetAddressActivity,
                            response.body.message.toString()
                        )
                    }
                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    private fun callApiGetAddressList() {
        Common.showLoadingProgress(this@GetAddressActivity)
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            this@GetAddressActivity,
            SharePreference.userId
        )!!

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getAddress(map)) {
                is NetworkResponse.Success -> {
                    val restResponse: ListResponse<AddressResponse> = response.body
                    if (restResponse.status == 1) {
                        Common.dismissLoadingProgress()
                        restResponse.data?.let { addressList.addAll(it) }
                        if (addressList.size > 0) {
                            binding.tvNoDataFound.visibility = View.GONE
                            binding.rvAddressList.visibility = View.VISIBLE
                            addressAdapter?.notifyDataSetChanged()
                        } else {
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.rvAddressList.visibility = View.GONE
                        }

                        binding.linearAddresses.visibility = View.VISIBLE
                    } else if (restResponse.status == 0) {
                        binding.tvNoDataFound.visibility = View.GONE
                        binding.rvAddressList.visibility = View.VISIBLE
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@GetAddressActivity,
                            restResponse.message
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    override fun onResume() {
        super.onResume()
        if (Common.isAddOrUpdated) {
            Common.isAddOrUpdated = false
            addressList.clear()
            callApiGetAddressList()
        }
    }

    private fun setupGetAddressAdapter(addressList: ArrayList<AddressResponse>) {
        addressAdapter =
            object : BaseAdaptor<AddressResponse>(this@GetAddressActivity, addressList) {
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: AddressResponse,
                    position: Int
                ) {
                    val tvAddressType: TextView = holder!!.itemView.findViewById(R.id.tvType)
                    val tvAddress: TextView = holder.itemView.findViewById(R.id.tvAddress)
                    val ivAddressType: ImageView = holder.itemView.findViewById(R.id.ivAddressType)

                    val ivEdit: ImageView = holder.itemView.findViewById(R.id.ivEdit)
                    val ivDelete: ImageView = holder.itemView.findViewById(R.id.ivDelete)

                    when (addressList[position].addressType) {
                        1 -> {
                            tvAddressType.text = resources.getString(R.string.home)
                            ivAddressType.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_home_black,
                                    null
                                )
                            )
                        }
                        2 -> {
                            tvAddressType.text = resources.getString(R.string.office)
                            ivAddressType.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_office,
                                    null
                                )
                            )
                        }
                        3 -> {
                            tvAddressType.text = resources.getString(R.string.other)
                            ivAddressType.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_locationn,
                                    null
                                )
                            )
                        }
                    }

                    /*          tvAddress.text = addressList[position].address.plus(",")
                                  .plus("${addressList[position].building},").plus(addressList[position].area)*/

                    tvAddress.text = addressList[position].address.toString()
                    ivDelete.setOnClickListener {

                        if (Common.isCheckNetwork(this@GetAddressActivity)) {
                            dlgDeleteConformationDialog(
                                this@GetAddressActivity,
                                resources.getString(R.string.delete_address_alert),
                                addressList[position].id.toString(),
                                position
                            )
                        } else {
                            Common.alertErrorOrValidationDialog(
                                this@GetAddressActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }



                    ivEdit.setOnClickListener {
                        if (ApiClient.System_environment == Constants.SendBox) {
                            Common.showErrorFullMsg(
                                this@GetAddressActivity,
                                resources.getString(R.string.send_box_error_alert)
                            )
                        } else {
                            val intent = Intent(this@GetAddressActivity, ActAddAddress::class.java)
                            intent.putExtra(
                                "houseNumber",
                                addressList[position].building.toString()
                            )
                            intent.putExtra("Address", addressList[position].address.toString())
                            intent.putExtra(
                                "addressType",
                                addressList[position].addressType.toString()
                            )
                            intent.putExtra("address_id", addressList[position].id.toString())
                            intent.putExtra("lat", addressList[position].lat.toString())
                            intent.putExtra("lang", addressList[position].lang.toString())
                            intent.putExtra("area", addressList[position].area.toString())
                            intent.putExtra("Type", "1")
                            startActivity(intent)

                        }


                    }
                    val isComeFromSelectAddress =
                        intent.getBooleanExtra("isComeFromSelectAddress", false)
                    holder.itemView.setOnClickListener {
                        if (isComeFromSelectAddress) {
                            if (ApiClient.System_environment == Constants.SendBox) {

                                val data = addressList[position]
                                val intent = Intent(
                                    this@GetAddressActivity,
                                    OrderSummeryActivity::class.java
                                )
                                intent.putExtra("houseNumber", data.building.toString())
                                intent.putExtra("Address", data.address.toString())
                                intent.putExtra("addressType", data.addressType.toString())
                                intent.putExtra("address_id", data.id.toString())
                                intent.putExtra("lat", data.lat.toString())
                                intent.putExtra("long", data.lang.toString())
                                intent.putExtra("area", data.area.toString())
                                intent.putExtra("Type", 1)
                                setResult(500, intent)
                                finish()
                            } else {
                                checkDeliveryZone(addressList[position])
                            }


                        }
                    }

                }

                override fun setItemLayout(): Int = R.layout.cell_address_list

            }
        binding.rvAddressList.apply {
            adapter = addressAdapter
            layoutManager = LinearLayoutManager(this@GetAddressActivity)
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = true
            /* val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
             itemTouchHelper.attachToRecyclerView(this)*/
        }
    }


    @SuppressLint("InflateParams")
    fun dlgDeleteConformationDialog(act: Activity, msg: String?, strCartId: String, pos: Int) {
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
            val mView = mInflater.inflate(R.layout.dlg_confomation, null, false)
            val textDesc: TextView = mView.findViewById(R.id.tvDesc)
            textDesc.text = msg
            val tvOk: TextView = mView.findViewById(R.id.tvYes)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                if (Common.isCheckNetwork(this@GetAddressActivity)) {
                    finalDialog.dismiss()
                    val hashMap = HashMap<String, String>()
                    hashMap["user_id"] = SharePreference.getStringPref(
                        this@GetAddressActivity, SharePreference.userId
                    ).toString()
                    hashMap["address_id"] = strCartId
                    callDeleteApi(hashMap, pos)
                } else {
                    Common.alertErrorOrValidationDialog(
                        this@GetAddressActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
            }
            val tvCancle: TextView = mView.findViewById(R.id.tvNo)
            tvCancle.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(mView)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun callDeleteApi(deleteRequest: HashMap<String, String>, pos: Int) {
        Common.showLoadingProgress(this@GetAddressActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().deleteAddress(deleteRequest)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {

                        Common.showSuccessFullMsg(
                            this@GetAddressActivity,
                            response.body.message.toString()
                        )
                        addressList.removeAt(pos)

                        addressAdapter?.notifyDataSetChanged()

                        if (addressList.size > 0) {
                            binding.tvNoDataFound.visibility = View.GONE
                            binding.rvAddressList.visibility = View.VISIBLE
                        } else {
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.rvAddressList.visibility = View.GONE
                        }
                    } else {
                        Common.showErrorFullMsg(
                            this@GetAddressActivity,
                            response.body.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@GetAddressActivity,
                        resources.getString(R.string.error_msg)
                    )
                }

            }
        }
    }
}