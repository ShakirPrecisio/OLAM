package com.kemar.olam.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.Writer
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import okhttp3.OkHttpClient
import java.io.File
import java.io.FileOutputStream
import java.math.RoundingMode
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Utility {

//    private var progressDialog: ProgressDialog? = null
    private var progressAnimate: AlertDialog? = null


    fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

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

    fun convertDoubleToRoundedValue(value:Double):String{
        if(value==null){
            return "0"
        }
        val format: NumberFormat = DecimalFormat.getInstance()
        format.roundingMode = RoundingMode.FLOOR
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 2
        return format.format(value).toString()
    }

    fun convertNumberIntoDecimalFormat(number: Int):String{
       return DecimalFormat("00").format(number)
    }

    /**
     * This method is used for checking internet connection if connection
     * available return true else return false
     */
    fun checkInternetConnection(context: Context): Boolean {
        val conMgr =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo.isAvailable
    }

   /* fun calculationForCBM(long:Char,):Double{

        val result =
            (s.toString().toInt().toDouble()) / 100 * (tempDia) / 100 * (tempDia) / 100 * 0.7854
        val df = DecimalFormat("###.###")
        val finalResult = df.format(result)

    }*/

    fun setupLocalization(context: Context){
        val myLocale = Locale(getLangSettingFromSharePre(context, "fr"))
        val res: Resources = context.getResources()
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
    }

    fun setLangSettingFromSharePre(context: Context, value: String){
        var langValue = SharedPrefLang.write("lang", value)

    }

    fun writeDataIntoFileAndSavePDF(mView: View, fileName: String, bmScreenShot: String): File? {
        var file: File? = null
        var filOutputStrem : FileOutputStream? = null
        try {
            val imageBytes = Base64.decode(bmScreenShot, Base64.DEFAULT)

            val d = Date()
            val s =
                android.text.format.DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

            val cw = ContextWrapper(mView.context)
            val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
            deletePreviousFileDataAndRecret(directory)
            file = File(directory, fileName + s + ".pdf")

            filOutputStrem = FileOutputStream(file, false)
            filOutputStrem.write(imageBytes)
            filOutputStrem.flush()
            filOutputStrem.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
        return file


    }

    fun deletePreviousFileDataAndRecret(directory: File){
        try{
            // Create imageDir
            if (directory.exists()) {
            var isContentDeleted =  directory.deleteRecursively()
                var test  = isContentDeleted
            }
            if (!directory.exists()) {
                directory.mkdir()
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun getLangSettingFromSharePre(context: Context, defaultValue: String):String{
        var langValue = SharedPrefLang.read("lang", defaultValue)
        if(langValue.isNullOrEmpty()){
            langValue ="fr"
        }else{
            if(langValue.equals("english", ignoreCase = true)){
                langValue = "en"
            }else{
                langValue = "fr"
            }
        }
        return langValue

    }


    fun isvalidTruckNumber(password: String):Boolean {

        val pattern:Pattern;
        val matcher:Matcher;

        val PASSWORD_PATTERN:String = "[0-9]{1,}[-]{1,}[0-9]{1}"

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        var condition=matcher.matches()
        return condition

    }



    /**
     * Global progress dialog show
     */
    fun showProgressDialog(context: Context?) {
//        progressDialog =
//            ProgressDialog(context, R.style.AlertDialogCustomColor)
//        progressDialog!!.setMessage("Please wait ...")
//        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//        progressDialog!!.setCanceledOnTouchOutside(false)
//        progressDialog!!.show()


        val dialogBuilder = AlertDialog.Builder(context as Activity)
        val inflater = (context as Activity).layoutInflater
        val dialogView: View = inflater.inflate(R.layout.progressdialog, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        if (progressAnimate == null) progressAnimate = dialogBuilder.create()

        progressAnimate!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (progressAnimate?.isShowing()==false) {
            progressAnimate?.show()
        }

    }

    fun alertDialgSession(
        context: Context?,
        activity: Activity?
    ) {
        val cardSesnOkLogoutBtn: CardView
        val dialog: AlertDialog
        val dilaog =
            AlertDialog.Builder(context!!, R.style.CustomDialogs)
        val layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.session_dilaog, null)
        dilaog.setView(view)
        dialog = dilaog.create()
        cardSesnOkLogoutBtn =
            view.findViewById<View>(R.id.cardSesnOkLogoutBtn) as CardView
        cardSesnOkLogoutBtn.setOnClickListener {
            if (activity != null) {
                SharedPref.logoutWithLoginRedirection(context)
                dialog.dismiss()
                activity.finish()
            }
        }
        dilaog.setCancelable(true)
        dialog.setCancelable(false)
        dialog.show()
    }


    fun createLogRecordDocsNumber(boNumber: String?, logNumber: String, diaBXx: String):String{
 var logRecordDocsNumber=""
        logRecordDocsNumber =  boNumber+"-"+logNumber+"-"+diaBXx
        return logRecordDocsNumber

    }

    fun intToDouble(value: Int?):Double{
        var returnValue = 0.0
        returnValue = value?.toDouble()!!
        return returnValue
    }


    fun generatebarcodeBitmap(productId: String): Bitmap {
        val hintMap: Hashtable<EncodeHintType, ErrorCorrectionLevel> =
            Hashtable<EncodeHintType, ErrorCorrectionLevel>()
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
        val codeWriter: Writer
        codeWriter = Code128Writer()
        val byteMatrix: BitMatrix =
            codeWriter.encode(productId, BarcodeFormat.CODE_128, 600, 200, hintMap)
        val width = byteMatrix.width
        val height = byteMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (i in 0 until width) {
            for (j in 0 until height) {
                bitmap.setPixel(i, j, if (byteMatrix[i, j]) Color.BLACK else Color.WHITE)
            }
        }
        return  bitmap

    }


    fun printHashKey(pContext: Context) {
        try {
            val info = pContext.packageManager
                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("HashKey", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("HashKey", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("HashKey", "printHashKey()", e)
        }
    }


    fun doubleToInt(value: Double):Int{
        var returnValue = 0
        returnValue = value.toInt()
        return returnValue
    }

    fun showAlert(activity: Activity?, message: String?){
        android.app.AlertDialog.Builder(activity)
            //.setTitle(activity?.getString(R.string.app_name))
            .setMessage(message)
            .setPositiveButton("Ok",
                DialogInterface.OnClickListener { dialog, which ->

                })
            //.setIcon(R.drawable.logo)
            .show()
    }

    /**
     * Global progress dialog dismiss
     */
    fun dismissProgressDialog() {
//        if (progressDialog != null) if (progressDialog!!.isShowing()) progressDialog!!.dismiss()

        try {
            if(progressAnimate!=null) {
                if (progressAnimate?.isShowing!!)
                    progressAnimate?.dismiss()
            }

        } catch (e: Exception) {
        }
    }


    fun  logout(context: Context?){

    }
    fun hideKeyBoard(activity: Activity) {
        val inputManager: InputMethodManager = activity.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (activity.currentFocus != null) {
            assert(inputManager != null)
            inputManager.hideSoftInputFromWindow(
                Objects.requireNonNull(activity.currentFocus)!!.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun openKeyBoard(activity: Activity){
        val inputManager: InputMethodManager = activity.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputManager.toggleSoftInputFromWindow(
            Objects.requireNonNull(activity.currentFocus)!!.applicationWindowToken,
            InputMethodManager.SHOW_FORCED, 0
        )
    }

    fun openKeyBoard(activity: Activity, mEditText: EditText){
        val imm = activity.getSystemService(
            Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT)
    }


    fun createBarcodeBitmap(data: String, width: Int, height: Int): Bitmap? {
        val writer = MultiFormatWriter()
        val finalData = Uri.encode(data)

        // Use 1 as the height of the matrix as this is a 1D Barcode.
        val bm = writer.encode(finalData, BarcodeFormat.CODE_128, width, 1)
        val bmWidth = bm.width
        val imageBitmap = Bitmap.createBitmap(bmWidth, height, Bitmap.Config.ARGB_8888)
        for (i in 0 until bmWidth) {
            // Paint columns of width 1
            val column = IntArray(height)
            Arrays.fill(column, if (bm[i, 0]) Color.BLACK else Color.WHITE)
            imageBitmap.setPixels(column, 0, 1, i, 0, 1, height)
        }
        return imageBitmap
    }


    /**
     * common method to show toast message
     *
     * @param context :- Class instance
     * @param message :- Message which to be displayed.
     */
    fun showToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * @param emailId
     * @return true - if emailId is valid
     */

    fun isValidEmailID(emailId: String?): Boolean {
        return !TextUtils.isEmpty(emailId) && Patterns.EMAIL_ADDRESS.matcher(emailId).matches()
    }

    fun isZipValid(zip: String?):Boolean{
        val regex = "^[0-9]{5}(?:-[0-9]{4})?$"
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(zip)
        return matcher.matches()
    }

 fun getRandomNumberString(bodreuNumber: String):String {
    // It will generate 6 digit random Number.
    // from 0 to 999999
    var rnd =  Random();
    var number = rnd.nextInt(99)
     val currentTimestamp = System.currentTimeMillis()
     var uniquebarcode=currentTimestamp.toString()

     //val uniqueBarcode = uniquebarcode.substring(0, 12)
     //var uniquebarcode=bodreuNumber+String.format("%04d", number)

     Log.e("uniqueBarcode", "is" + uniquebarcode)
     val last12: String
     if (uniquebarcode == null || uniquebarcode.length < 12) {
         val uniqueBarcode = uniquebarcode+number
         last12 = uniqueBarcode.substring(uniqueBarcode.length -12 );
     } else {
         last12 = uniquebarcode.substring(uniquebarcode.length -12 );
     }
    // this will convert String number sequence into 6 character.
     Log.e("last12", "is" + last12)
    return last12
}

    fun generateRandomNummber(bodreuNumber: String):String{
        val rnds = (0..10).random()

return  rnds.toString()
    }
    fun getYear(strdate: String?) :Int{
        var age :Int=0
        try {
            val originalFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            var date: Date? = null
            try {
                date = originalFormat.parse(strdate)
                System.out.println(calculateAge(date))
                age=calculateAge(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            return age
        }
        return  age
    }

    fun calculateAge(birthdate: Date?): Int {
        val birth = Calendar.getInstance()
        birth.time = birthdate
        val today = Calendar.getInstance()
        var yearDifference = (today[Calendar.YEAR]
                - birth[Calendar.YEAR])

        return yearDifference
    }




    fun startPickImageDialog(
        GALLERY_PICTURE: Int,
        CAMERA_REQUEST: Int,
        cameraPath: String?,
        activity: Activity
    ) {
        val myAlertDialog: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(
            activity
        )
        myAlertDialog.setTitle("Upload Pictures ")
        //myAlertDialog.setMessage("How do you want to set your picture?");
        myAlertDialog.setPositiveButton(
            "Gallery"
        ) { arg0, arg1 ->
            val pictureActionIntent: Intent
            pictureActionIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            activity.startActivityForResult(
                pictureActionIntent,
                GALLERY_PICTURE
            )
        }
        myAlertDialog.setNegativeButton(
            "Camera"
        ) { arg0, arg1 -> //                        Intent i =new Intent(fragment.activity,MainCameraActivity.class);
            //                        i.putExtra(Keys.Arguments.camera_path,cameraPath);
            //                        fragment.startActivityForResult(i,CAMERA_REQUEST);
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(
                    activity.applicationContext,
                    activity.packageName + ".provider",
                    File(cameraPath)
                )
            )
            activity.startActivityForResult(
                intent,
                CAMERA_REQUEST, Bundle()
            )
        }
        myAlertDialog.show()
    }


    fun dateFormater(inputFormat: String?, outputFormat: String?, strdate: String?): String? {
        var formattedDate = ""
        try {
            val originalFormat: DateFormat =
                SimpleDateFormat(inputFormat, Locale.ENGLISH)
            @SuppressLint("SimpleDateFormat") val targetFormat: DateFormat =
                SimpleDateFormat(outputFormat)
            var date: Date? = null
            try {
                date = originalFormat.parse(strdate)
                formattedDate = targetFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            return formattedDate
        }
        return formattedDate
    }

     fun getCurrentDate(): String {
         var currenDate = ""
         try {
             val sdf = SimpleDateFormat("dd.MM.yyyy")
             currenDate = sdf.format(Date())
         }catch (e: Exception){
             e.toString()
         }
         return  currenDate
    }

    fun getCurrentTime():String{
        var currentTime = ""
        try {
            val sdf = SimpleDateFormat("hh:mm a")
            currentTime =  sdf.format(Date())
        }catch (e: Exception){
            e.toString()
        }
            return currentTime
    }


    //Print PDF
    fun printPDF(mContext: Context, pdfPath: String) {
        val printManager =
            mContext.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(mContext, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }


    fun downloadPDFFromURL(mContext: Context, pdfPath: String) {
        showProgressDialog(mContext)
        try {
            val mFileName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            var destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
            var fileName = "$mFileName.pdf";
            destination += fileName;
            var uri = Uri.parse("file://" + destination);
            val pdfURL = pdfPath;

            var request = DownloadManager.Request(Uri.parse(pdfURL));
            request.setDescription("Downloading....");
            request.setTitle(" OLAM ");
            request.setDestinationUri(uri);
            val manager =
                mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager;
            manager.enqueue(request);
            val finalDestination = destination

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    mContext.unregisterReceiver(this)
                     dismissProgressDialog()
                    printPDF(mContext, finalDestination)

                }
            }
            mContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        } catch (e: Exception) {
            dismissProgressDialog()
            e.printStackTrace()
        }

    }


    fun setProgressDialog(context:Context, message:String): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 15.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        dialog.setCancelable(false)
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }



  /*  @Throws(java.lang.Exception::class)
    fun dateFormat(inputFormat: String?, outFormat: String?, strDate: String?): String? {

        try {
            if (strDate != null && !strDate.isEmpty()) {
                val locale = Locale("en_US_POSIX")

                val formats = arrayOf(
                    SimpleDateFormat("dd/MM/yyyy, hh:mm a", locale),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("dd-MM-yy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("MM-dd-yy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("MM.dd.yyyy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("yyyy.MM.dd'T'HH:mm:ss.SSSZ", locale),
                    SimpleDateFormat("yyyy-MM-dd", locale),
                    SimpleDateFormat("yyyy-dd-MM", locale),
                    SimpleDateFormat("MMMM dd, yyyy", locale),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale),
                    SimpleDateFormat("MM-dd-yyyy", locale),
                    SimpleDateFormat("dd-MM-yyyy", locale),
                    SimpleDateFormat("yyyy-MM-dd hh:mm a", locale),
                    SimpleDateFormat("dd-MM-yyyy hh:mm a", locale),
                    SimpleDateFormat("MM-dd-yyyy hh:mm a", locale),
                    SimpleDateFormat("MM/dd/yyyy, hh:mm a", locale)

                )
                var parsedDate: Date? = null
                if (!inputFormat.isNullOrEmpty()) {

                    var formattedDate = ""
                    val originalFormat: DateFormat =
                        SimpleDateFormat(inputFormat, Locale.ENGLISH)
                    @SuppressLint("SimpleDateFormat") val targetFormat: DateFormat =
                        SimpleDateFormat(outFormat)
                    var date: Date? = null
                    date = originalFormat.parse(strDate)
                    targetFormat.setTimeZone(TimeZone.getDefault());
                    formattedDate = targetFormat.format(date)
                    return formattedDate


                } else {
                    for (i in formats.indices) {
                        return try {
                            parsedDate = formats[i].parse(strDate)
                            parsedDateIntoGivenFormat(parsedDate, outFormat)
                        } catch (e: ParseException) {
                            continue
                        }
                    }
                }
            }
            throw java.lang.Exception("Unknown date format: '$strDate'")

        }catch (e:Exception){
            e.printStackTrace()

            return ""
        }

        return ""

    }

    fun parsedDateIntoGivenFormat(
        parsedDate: Date?,
        outFormat: String?
    ): String {
        val DateFor = SimpleDateFormat(outFormat, Locale.getDefault())
        return DateFor.format(parsedDate!!)
    }
*/

}