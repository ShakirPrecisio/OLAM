package com.kemar.olam.retrofit

import android.annotation.SuppressLint
import com.google.gson.GsonBuilder
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.login.model.responce.LoginRes
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.WS_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiClientMultiPart {
    private lateinit var retrofit: Retrofit
    private const val cacheSize = 5 * 1024 * 1024 // 10 MB
    /*var cache =
        Cache(MyApplication.getAppContext().cacheDir, cacheSize.toLong())*/

    val authorizationInterceptor = Interceptor { chain ->
        val mainResponse = chain.proceed(chain.request())
        if (mainResponse.code() == 305) {
            try {
                val newRequest = arrayOfNulls<Request>(1)

                val apiService: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request : CommonRequest =   CommonRequest()
                request.userName = SharedPref.read(Constants.user_login_id)

                val call: Call<LoginRes> = apiService.regenerateToken(request)
                // MyUtils.logE("Retrofit Request -> ", " URL: " + call.request().url() + " request: " + new Gson().toJson(loginRequest));
                val resp: LoginRes? = call.execute().body()
                if (resp!!.getSeverity() == 200) {

                    SharedPref.write(
                            Constants.user_token,
                            resp.getTokenKey()
                    )

                    newRequest[0] =
                            chain.request().newBuilder().removeHeader("tokenKey")
                                    .addHeader("tokenKey",  resp.getTokenKey().toString()).build()
                    return@Interceptor chain.proceed(newRequest[0])
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mainResponse
    }

    val client: Retrofit
        get() {
            /*   val decryptionInterceptor =
                DecryptionInterceptor(EncrypterDecrypter(MyApplication.getAppContext()))
            val encryptionInterceptor =
                EncryptionInterceptor(EncrypterDecrypter(MyApplication.getAppContext()))*/
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            //this for UAT Build
            //  val client = getUnsafeOkHttpClient()
            //this for QA Build
            val gson = GsonBuilder()
                .setLenient()
                .create()

            val client =
                getUnsafeOkHttpClient()//getUnsafeOkHttpClient()//getUnsafeOkHttpClient() //OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    //.cache(cache)
                    /*  .addInterceptor(encryptionInterceptor)
                .addInterceptor(decryptionInterceptor)*/
                     .addInterceptor(authorizationInterceptor)
                    .connectTimeout(500000, TimeUnit.SECONDS)
                    //.addNetworkInterceptor(MyOkHttpInterceptor())
                    .readTimeout(30000, TimeUnit.SECONDS)
                    .writeTimeout(30000, TimeUnit.SECONDS).build()

            retrofit = Retrofit.Builder()
                .baseUrl(WS_URL.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()

            return retrofit
        }

    fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

