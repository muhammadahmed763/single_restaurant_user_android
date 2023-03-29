package com.singlerestaurant.user.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActBookTableBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ActBookTable : AppCompatActivity() {

    private lateinit var binding: ActBookTableBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActBookTableBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (SharePreference.getStringPref(
                this@ActBookTable,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnSubmit.setOnClickListener {
            validation()
        }
        binding.edDate.setOnClickListener {

            getDate(binding.edDate)
        }
        binding.edTime.setOnClickListener {
            getTime(binding.edTime)
        }

    }
    private fun getDate(textView: AppCompatTextView) {
        val c: Calendar = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar[year, monthOfYear] = dayOfMonth
                val format = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
                val dateString = format.format(calendar.time)
                textView.text = dateString
            },
            mYear,
            mMonth,
            mDay)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }

    private fun getTime(textView: AppCompatTextView) {
        val calender = Calendar.getInstance()

        val mHour = calender[Calendar.HOUR_OF_DAY]
        val mMinute = calender[Calendar.MINUTE]



        val timePickerDialog = TimePickerDialog(
            this@ActBookTable,
            { view, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute).plus(":00")
                textView.text = Common.getTimeFormat(time)
            },
            mHour,
            mMinute,

            false
        )



        timePickerDialog.show()

    }


    private fun validation() {

        if (binding.edFullName.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )
        } else if (binding.edEmail.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else if (binding.edMobile.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else if (binding.edNoGuest.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else if (binding.edDate.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else if (binding.edTime.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else if (binding.edReservationType.text?.isEmpty() == true) {
            Common.alertErrorOrValidationDialog(
                this@ActBookTable,
                resources.getString(R.string.validation_all)
            )

        } else {
            callBookTableApi()
        }

    }

    private fun callBookTableApi() {
        Common.showLoadingProgress(this@ActBookTable)
        val request = HashMap<String, String>()
        request["name"] = binding.edFullName.text.toString()
        request["email"] = binding.edEmail.text.toString()
        request["mobile"] = binding.edMobile.text.toString()
        request["guests"] = binding.edNoGuest.text.toString()
        request["date"] = binding.edDate.text.toString()
        request["time"] = binding.edTime.text.toString()
        request["reservation_type"] = binding.edReservationType.text.toString()
        request["special_request"] = binding.edSpecialRequest.text.toString()

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().bookTable(request)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    Common.showSuccessFullMsg(this@ActBookTable, response.body.message.toString())
                    finish()
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(this@ActBookTable, response.body.message.toString().replace("\\n", System.lineSeparator()))
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(this@ActBookTable,resources.getString(R.string.no_internet))
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(this@ActBookTable,resources.getString(R.string.error_msg))
                }


            }
        }

    }
}