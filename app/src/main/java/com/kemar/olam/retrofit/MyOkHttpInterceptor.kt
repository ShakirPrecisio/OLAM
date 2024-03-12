package com.kemar.olam.retrofit

import android.util.Log
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class MyOkHttpInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request()
            .newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("tokenKey", SharedPref.read(Constants.user_token))
            .header("userId",SharedPref.getUserId(Constants.user_id).toString())
            .header("userLocationId",SharedPref.readInt(Constants.user_location_id).toString())
          /*  .header("Content-Type", "application/x-www-form-urlencoded")
            .header("authorization", "bearer  vz7jploec5vCVSMyvABFB8ty9zI-eQvizbINGPFFDybZP6x1wgSSiqSqGXPkw9AYYy2HtsMfw9rGIxQIzySkI6gd4aRAdBc4NJoIiegJnGi7hNw7BB3y7mxLBK_E22q0BkM_Val5bUUvRCfXRiXpIy4B4TUOtDOZiZGc6GJ_gNEn2FswSgXBxMrUZ9kHDcyWpZy0Zb-dsIBWNfQCWMVjaHK-iG1Vdn3NFQCoCEALjpo")*/
            .build()
        Log.d("token", SharedPref.read(Constants.user_token))
        Log.d("userId", SharedPref.getUserId(Constants.user_id).toString())
        return chain.proceed(newRequest)
    }
}