package com.kemar.olam.login.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.DashboardActivity
import com.kemar.olam.login.model.request.LoginReq
import com.kemar.olam.login.model.responce.LoginRes
import com.kemar.olam.offlineData.loginModel.LoginUser
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class LoginActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var realm: Realm


    private val SCOPES = arrayOf("https://graph.windows.net/User.Read")
    /* Azure AD v2 Configs */
    val AUTHORITY = "https://login.microsoftonline.com/common"
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var mAccount: IAccount? = null

    lateinit var mUtils : Utility
    var TAG = LoginActivity::class.java.simpleName
    //for google captcha
    var SITE_KEY = "6LcJ1f0ZAAAAAN4oYjoeJLe_SNxcD2hQa7pDg4X6"
    var SECRET_KEY = "6LcJ1f0ZAAAAAMbQW7iGCPdPg3yKTDiBswzXH_eO"
    var queue: RequestQueue? = null
      var  splitedName : String  = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mUtils = Utility()

        realm = RealmHelper.getRealmInstance()

        btnLogin.setOnClickListener(this)
      //  loadGoogleCaptcha()
        PublicClientApplication.createSingleAccountPublicClientApplication(
            this,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
//                    loadAccount()
                    signOutPreviousUser()

                }

                override fun onError(exception: MsalException) {
                    displayError(exception)
                }
            })
        Log.e("Timestamp","is"+ System.currentTimeMillis().toString())
//        verifyRedirectUriWithAppSignature()


    }



    // Verifies broker redirect URI against the app's signature, to make sure that this is legit.
//    @Throws(MsalClientException::class)
//    private fun verifyRedirectUriWithAppSignature() {
//        val packageName: String = this.getPackageName()
//        try {
//            val info: PackageInfo = this.getPackageManager()
//                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures) {
//                val messageDigest: MessageDigest = MessageDigest.getInstance("SHA")
//                messageDigest.update(signature.toByteArray())
//                val signatureHash: String =
//                    android.util.Base64.encodeToString(
//                        messageDigest.digest(),
//                        android.util.Base64.NO_WRAP
//                    )
//
//                edtUserName.setText(signatureHash)
//
//                val builder: Uri.Builder = Uri.Builder()
//                val uri: Uri = builder.scheme("msauth")
//                    .authority(packageName)
//                    .appendPath(signatureHash)
//                    .build()
//                txtTest.setText(uri.toString())
////                if (mRedirectUri.equalsIgnoreCase(uri.toString())) {
////                    // Life is good.
//                    return
////                }
//            }
//        } catch (e: PackageManager.NameNotFoundException) {
////            Logger.error(TAG, "Unexpected error in verifyRedirectUriWithAppSignature()", e)
//        } catch (e: NoSuchAlgorithmException) {
////            Logger.error(TAG, "Unexpected error in verifyRedirectUriWithAppSignature()", e)
//        }
//        throw MsalClientException(
//            MsalClientException.REDIRECT_URI_VALIDATION_ERROR,
//            "The redirect URI in the configuration file doesn't match with the one " +
//                    "generated with package name and signature hash. Please verify the uri in the config file and your app registration in Azure portal."
//        )
//    }

    fun signOutPreviousUser(){
        if (mSingleAccountApp != null) {

            mSingleAccountApp!!.signOut(object :
                ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    updateUI(null)
                    performOperationOnSignOut()
                }

                override fun onError(exception: MsalException) {
                    displayError(exception)
                }
            })
        }
    }

//    fun loadGoogleCaptcha() {
//        queue = Volley.newRequestQueue(getApplicationContext())
//        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
//            .addOnSuccessListener(
//                this
//            ) { response ->
//                if (!response.tokenResult.isEmpty()) {
//                    handleSiteVerify(response.tokenResult)
//                }
//            }
//            .addOnFailureListener(this) { e ->
//                if (e is ApiException) {
//                    Log.d(
//                        TAG, "Error message: " +
//                                CommonStatusCodes.getStatusCodeString(e.statusCode)
//                    )
//                } else {
//                    Log.d(TAG, "Unknown type of error: " + e.message)
//                }
//
//            }
//    }

         fun handleSiteVerify(responseToken: String) { //it is google recaptcha siteverify server
            //you can place your server url
            val url = "https://www.google.com/recaptcha/api/siteverify"
            val request: StringRequest = object : StringRequest(
                Request.Method.POST, url,
                com.android.volley.Response.Listener<String?> { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getBoolean("success")) { //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(
                                applicationContext,
                                jsonObject.getString("error-codes").toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (ex: Exception) {
                        Log.d(TAG, "JSON exception: " + ex.message)
                    }
                },
                com.android.volley.Response.ErrorListener { error ->
                    Log.d(
                        TAG,
                        "Error message: " + error.message
                    )
                }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["secret"] = SECRET_KEY
                    params["response"] = responseToken
                    return params
                }
            }
            request.retryPolicy = DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            queue!!.add(request)
        }

    fun validate(): Boolean {
        if(!isAdLoginSwitch.isChecked) {
            if (TextUtils.isEmpty(edtUserName.getText().toString())) {
                edtUserName.requestFocus()
                edtUserName.setError(AlertMessage.Empty__UserName)
                return false
            } else {
                edtUserName.clearFocus()
                edtUserName.setError(null)
            }
            if (TextUtils.isEmpty(edtPassword.text.toString())) {
                edtPassword.requestFocus()
                edtPassword.error = AlertMessage.Empty_Password
                return false
            } else {
                edtPassword.clearFocus()
                edtPassword.setError(null)
            }
        }
        return true
        }

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.btnLogin -> {
                try {
                    acessRuntimPermission()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun goToHomePage(){
        var i = Intent(this, DashboardActivity::class.java)
        startActivity(i)
        finish()
    }

    fun acessRuntimPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                if(validate()){
                    try {
                        if (isAdLoginSwitch.isChecked) {

                            if (mSingleAccountApp == null) {
                                return
                            }

//                            mSingleAccountApp?.acquireTokenSilent(SCOPES, "")

                            getAuthInteractiveCallback()?.let {
                                mSingleAccountApp?.signIn(
                                    this@LoginActivity,
                                    null,
                                    SCOPES,
                                    it
                                )
                            }

                        } else {
                            loginApi()
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                // goToHomePage()
                }
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {

            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage(getString(R.string.Please_give_permission_for_app_functionality))
            .setDeniedMessage(
                getString(R.string.If_you_reject_permission_you_can_not_use_this_service) + "\n\n" + getString(
                    R.string.Please_turn_on_permissions_at
                )
            )
            .setGotoSettingButtonText("setting")
            .setPermissions(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
            .check()
    }

    private fun loginApi() {
        if (mUtils.checkInternetConnection(this)) {
            try {
                mUtils.showProgressDialog(this)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var loginRequest : LoginReq  =  LoginReq()

                if(!isAdLoginSwitch.isChecked) {
                    loginRequest.setPassword(edtPassword.text.toString())
                    loginRequest.setIsADLogin(false)
                    loginRequest.setUsername(edtUserName.text.toString())

                }else{
                    loginRequest.setUsername(splitedName)
                    loginRequest.setIsADLogin(true)
                    loginRequest.setPassword("")

                }

                /*  if(isAdLoginSwitch.isChecked){
                    loginRequest.setIsADLogin(true)
                }else{
                    loginRequest.setIsADLogin(false)
                }*/

                val call_login: Call<LoginRes> =
                    apiInterface.login(loginRequest)
                call_login.enqueue(object :
                    Callback<LoginRes> {
                    override fun onResponse(
                        call: Call<LoginRes>,
                        response: Response<LoginRes>
                    ) {
                        mUtils.dismissProgressDialog()
                        try {
                            if (response.code() == 200) {
                                val loginresponse: LoginRes =
                                    response.body()!!
                                if (loginresponse != null) {
                                    if (loginresponse.getSeverity() == 200) {

                                        val i: Intent =
                                            Intent(
                                                this@LoginActivity,
                                                DashboardActivity::class.java
                                            )
                                        SharedPref.write(
                                            Constants.user_name,
                                            loginresponse.getUserProfile()?.firstName + " " + loginresponse.getUserProfile()?.lastName
                                        )
                                        SharedPref.write(
                                            Constants.profile_path,
                                            loginresponse.getUserProfile()?.image
                                        )
                                        SharedPref.write(
                                            Constants.user_id,
                                            loginresponse.getUserProfile()?.userId.toString()
                                        )
                                        SharedPref.write(
                                            Constants.user_location_id,
                                            loginresponse.getUserLocationID().toString()
                                        )
                                        SharedPref.write(
                                            Constants.user_role,
                                            loginresponse.getRole()?.roleName
                                        )
                                        SharedPref.write(
                                            Constants.user_token,
                                            loginresponse.getTokenKey()
                                        )
                                        SharedPref.write(
                                            Constants.user_login_id,
                                            loginresponse.getUserProfile()?.loginId
                                        )

                                        val connectionsJSONString: String = Gson().toJson(
                                            loginresponse.getAppModuleAccess()
                                        )

                                        realm.executeTransaction { realm ->
                                            var loginUser=LoginUser()
                                            loginUser.uniqueId=System.currentTimeMillis().toString()
                                            loginUser.user_name=edtUserName.text.toString()
                                            loginUser.username=edtUserName.text.toString()
                                            loginUser.password=edtPassword.text.toString()
                                            loginUser.profile_path= loginresponse.getUserProfile()?.image
                                            loginUser.token= loginresponse.getTokenKey()
                                            loginUser.user_login_id=  loginresponse.getUserProfile()?.loginId
                                            loginUser.user_token=  loginresponse.getTokenKey()
                                            loginUser.user_role= loginresponse.getRole()?.roleName
                                            loginUser.user_location_id=  loginresponse.getUserLocationID().toString()
                                            loginUser.user_id=  loginresponse.getUserProfile()?.userId.toString()
                                            loginUser.appmodule=connectionsJSONString
                                            realm.copyToRealmOrUpdate(loginUser)
                                        }

                                        SharedPref.write(Constants.APPMODULE, connectionsJSONString)
                                        SharedPref.write(Constants.LOGIN, true)

                                        startActivity(i)
                                        finish()

                                    } else {
                                        mUtils.showAlert(
                                            this@LoginActivity,
                                            loginresponse.getMessage()
                                        )
                                    }
                                }
                            } else {
                                mUtils.showToast(this@LoginActivity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<LoginRes>,
                        t: Throwable
                    ) {
                        mUtils.showAlert(this@LoginActivity, Constants.SERVERTIMEOUT)
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{

            realm.executeTransaction { bgrealm ->
                var username =edtUserName.text.toString()
                var password =edtPassword.text.toString()

                val realmResults: LoginUser = bgrealm.where(LoginUser::class.java)
                        .equalTo("username", username)
                        .equalTo("password", password)
                        .findFirst()!!

                if(realmResults.isValid || realmResults.isLoaded) {

                    if(realmResults.uniqueId!=null){

                        val i: Intent = Intent(this@LoginActivity,DashboardActivity::class.java)

                        SharedPref.write(
                                Constants.user_name,
                                realmResults.username
                        )

                        SharedPref.write(
                                Constants.profile_path,
                                realmResults.profile_path
                        )

                        SharedPref.write(
                                Constants.user_id,
                                realmResults.user_id
                        )

                        SharedPref.write(
                                Constants.user_location_id,
                                realmResults.user_location_id
                        )

                        SharedPref.write(
                                Constants.user_role,
                                realmResults.user_role
                        )

                        SharedPref.write(
                                Constants.user_token,
                                realmResults.token
                        )

                        SharedPref.write(
                                Constants.user_login_id,
                                realmResults.user_login_id
                        )

                        SharedPref.write(Constants.APPMODULE,  realmResults.appmodule)
                        SharedPref.write(Constants.LOGIN, true)

                        startActivity(i)
                        finish()

                    }
                }

            }
        }
    }


    //azure login integration
     open fun loadAccount(): Unit {
        if (mSingleAccountApp == null) {
            return
        }
          mSingleAccountApp?.getCurrentAccountAsync(object :
              ISingleAccountPublicClientApplication.CurrentAccountCallback {
              override fun onAccountLoaded(activeAccount: IAccount?) { // You can use the account data to update your UI or your app database.

                  // You can use the account data to update your UI or your app database.
                  mAccount = activeAccount
                  // updateUI(activeAccount)
              }

              override fun onAccountChanged(
                  priorAccount: IAccount?,
                  currentAccount: IAccount?
              ) {
                  if (currentAccount == null) { // Perform a cleanup task as the signed-in account changed.
                      performOperationOnSignOut()
                  }
              }

              override fun onError(exception: MsalException) {
                  displayError(exception)
              }
          })
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback? {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) { /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(
                    TAG,
                    "Successfully authenticated"
                )
                val accountName  = authenticationResult?.account?.username
                     splitedName = accountName.substring(0, accountName.indexOf("@"))
                    loginApi()
                /* Update UI *//*updateUI(authenticationResult.account)
                *//* call graph *//*callGraphAPI(authenticationResult)*/
            }

            override fun onError(exception: MsalException) { /* Failed to acquireToken */
                Log.d(
                    TAG,
                    "Authentication failed: $exception"
                )
                displayError(exception)
            }

            override fun onCancel() { /* User canceled the authentication */
                Log.d(
                    TAG,
                    "User cancelled login."
                )
            }
        }
    }


//    private fun getAuthSilentCallback(): SilentAuthenticationCallback? {
//        return object : SilentAuthenticationCallback {
//            override fun onSuccess(authenticationResult: IAuthenticationResult) {
//                Log.d(
//                    TAG,
//                    "Successfully authenticated"
//                )
//                callGraphAPI(authenticationResult)
//            }
//
//            override fun onError(exception: MsalException) {
//                Log.d(
//                    TAG,
//                    "Authentication failed: $exception"
//                )
//                displayError(exception)
//            }
//        }
//    }
//
//
//    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
//        val accessToken = authenticationResult.accessToken
//        val graphClient = GraphServiceClient
//            .builder()
//            .authenticationProvider { request ->
//                Log.d(
//                    TAG,
//                    "Authenticating request," + request.requestUrl
//                )
//                request.addHeader("Authorization", "Bearer $accessToken")
//            }
//            .buildClient()
//        graphClient
//            .me()
//            .drive()
//            .buildRequest()[object : ICallback<Drive> {
//            override fun success(drive: Drive) {
//                Log.d(
//                    TAG,
//                    "Found Drive " + drive.id
//                )
//                displayGraphResult(drive.rawObject)
//            }
//
//            override fun failure(ex: ClientException) {
//                displayError(ex)
//            }
//        }]
//    }
//
    private fun updateUI(account: IAccount?) {
        if (account != null) {
            /*signInButton.setEnabled(false)
            signOutButton.setEnabled(true)
            callGraphApiInteractiveButton.setEnabled(true)
            callGraphApiSilentButton.setEnabled(true)
            currentUserTextView.setText(account.username)*/
        } else {
            /*signInButton.setEnabled(true)
            signOutButton.setEnabled(false)
            callGraphApiInteractiveButton.setEnabled(false)
            callGraphApiSilentButton.setEnabled(false)
            currentUserTextView.setText("")
            logTextView.setText("")*/
        }
    }

    private fun displayError(exception: java.lang.Exception) {
        //logTextView.setText(exception.toString())
    }
//
//    private fun displayGraphResult(graphResponse: JsonObject) {
//      //  logTextView.setText(graphResponse.toString())
//    }

    private fun performOperationOnSignOut() {
        /*val signOutText = "Signed Out."
      //  currentUserTextView.setText("")
        Toast.makeText(applicationContext, signOutText, Toast.LENGTH_SHORT)
            .show()*/
    }


}