package com.example.mpesaapp

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mpesaapp.Constants.BUSINESS_SHORT_CODE
import com.example.mpesaapp.Constants.CALLBACKURL
import com.example.mpesaapp.Constants.PARTYB
import com.example.mpesaapp.Constants.PASSKEY
import com.example.mpesaapp.Constants.TRANSACTION_TYPE
import com.example.mpesaapp.Utils.getPassword
import com.example.mpesaapp.Utils.sanitizePhoneNumber
import com.example.mpesaapp.Utils.timestamp
import com.example.mpesaapp.databinding.ActivityMainBinding
import com.example.mpesaapp.model.AccessToken
import com.example.mpesaapp.model.STKPush
import com.example.mpesaapp.services.DarajaApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class MainActivity : AppCompatActivity() {


    private lateinit var binding : ActivityMainBinding
    private lateinit var mApiClient: DarajaApiClient
    private lateinit var mProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mProgressDialog = ProgressDialog(this)
        mApiClient = DarajaApiClient()
        mApiClient.setIsDebug(true) //Set True to enable logging, false to disable.
        binding.mPay.setOnClickListener(::onClick)
        getAccessToken()
    }

    private fun getAccessToken() {
        mApiClient.setGetAccessToken(true)
        mApiClient.mpesaService().accessToken!!.enqueue(object : Callback<AccessToken?> {
            override fun onResponse(call: Call<AccessToken?>, response: Response<AccessToken?>) {
                if (response.isSuccessful) {
                    mApiClient.setAuthToken(response.body()?.accessToken)
                }
            }

            override fun onFailure(call: Call<AccessToken?>, t: Throwable) {}
        })
    }


    private fun onClick(view: View) {
        if (view === binding.mPay) {
            val phoneNumber = binding.mPhone.text.toString()
            val amount = binding.mAmount.text.toString()
            performSTKPush(phoneNumber, amount)
        }
    }


    private fun performSTKPush(phoneNumber: String?, amount: String) {
        mProgressDialog.setMessage("Processing your request")
        mProgressDialog.setTitle("Please Wait...")
        mProgressDialog.isIndeterminate = true
        mProgressDialog.show()
        val timestamp = timestamp
        val stkPush = STKPush(
            BUSINESS_SHORT_CODE,
            getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
            timestamp,
            TRANSACTION_TYPE, amount,
            sanitizePhoneNumber(phoneNumber!!),
            PARTYB,
            sanitizePhoneNumber(phoneNumber),
            CALLBACKURL,
            "SmartLoan Ltd",  //Account reference
            "SmartLoan STK PUSH by TDBSoft" //Transaction description
        )
        mApiClient.setGetAccessToken(false)

        //Sending the data to the Mpesa API, remember to remove the logging when in production.
        mApiClient.mpesaService().sendPush(stkPush)!!.enqueue(object : Callback<STKPush?> {
            override fun onResponse(call: Call<STKPush?>, response: Response<STKPush?>) {
                mProgressDialog.dismiss()
                try {
                    if (response.isSuccessful) {
                        Timber.d("post submitted to API. %s", response.body())
                    } else {
                        Timber.e("Response %s", response.errorBody()?.string())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<STKPush?>, t: Throwable) {
                mProgressDialog.dismiss()
                Timber.e(t)
            }
        })
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
}