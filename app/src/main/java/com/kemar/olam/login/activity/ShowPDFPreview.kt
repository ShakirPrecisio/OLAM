package com.kemar.olam.login.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.kemar.olam.R
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.activity_show_p_d_f_preview.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


class ShowPDFPreview : AppCompatActivity(), View.OnClickListener
     {

         private val FILENAME = "report.pdf"

         private var pageIndex = 0
         private var pdfRenderer: PdfRenderer? = null
         private var currentPage: PdfRenderer.Page? = null
         private var parcelFileDescriptor: ParcelFileDescriptor? = null


    lateinit var mUtils: Utility
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_p_d_f_preview)
        txtConfirm.setOnClickListener(this)
        mUtils = Utility()
     /*   val progressDialog =  ProgressDialog(this)
        progressDialog.setMessage("Loading Data...")
        webView.requestFocus()
        webView.getSettings().setJavaScriptEnabled(true)*/
        val myPdfUrl = intent?.getStringExtra(Constants.PDF_PATH)
        SaveBitmapToInternalStorageAsyncTask().execute(myPdfUrl)
      /*  val url = "https://docs.google.com/viewer?embedded = true&url = $myPdfUrl"
        webView.loadUrl(url)
        progressDialog.setCancelable(false)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
*/

       /* webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100) {
                    progressDialog.show();
                }
                if (progress == 100) {
                    progressDialog.dismiss();
                }
            }
        }*/



    }

    inner class  SaveBitmapToInternalStorageAsyncTask internal constructor():
        AsyncTask<String?, String?, File?>() {

        override fun onPreExecute() {
            super.onPreExecute()

        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            if (file != null) {
                try {
                    openRenderer(applicationContext,file)
                    showPage(pageIndex)
                   /* pdfView.fromFile(file)
                        .defaultPage(0)
                        .enableAnnotationRendering(true)
                        .spacing(10) // in dp
                        .load()*/
                } catch (t: Throwable) {
                }
            }
        }



        override fun doInBackground(vararg p0: String?): File? {
            var file: File? = null
            try {

                val bmScreenShot = p0[0]
                if (bmScreenShot != null) {
                    file =   writeDataIntoFileAndSavePDF(applicationContext,"Invoice",bmScreenShot)
                }

                /*  val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                  file = bitmapImage?.let { saveBitmapIntoStorageNew(it) }*/
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }

    fun deletePreviousFileDataAndRecret(directory:File){
        try{
            // Create imageDir
            if (directory.exists()) {
                var isContentDeleted =  directory.deleteRecursively()
                var test  = isContentDeleted
            }
            if (!directory.exists()) {
                directory.mkdir()
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun writeDataIntoFileAndSavePDF(context:Context,fileName:String,bmScreenShot:String): File? {
        var file: File? = null
        var filOutputStrem : FileOutputStream? = null
        try {
            val imageBytes = Base64.decode(bmScreenShot, Base64.DEFAULT)

            val d = Date()
            val s =
                android.text.format.DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

            val cw = ContextWrapper(context)
            val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
            deletePreviousFileDataAndRecret(directory)
            file = File(directory, fileName + s + ".pdf")

            filOutputStrem = FileOutputStream(file, false)
            filOutputStrem.write(imageBytes)
            filOutputStrem.flush()
            filOutputStrem.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
        return file


    }

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.txtConfirm->{
                var resultIntent : Intent = Intent();
                setResult(Activity.RESULT_OK,resultIntent)
                finish()
            }
        }
    }




         @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
         override fun onStart() {
             super.onStart()
             try {

             } catch (e: IOException) {
                 e.printStackTrace()
             }
         }

         override fun onStop() {
             try {
                 closeRenderer()
             } catch (e: IOException) {
                 e.printStackTrace()
             }
             super.onStop()
         }

         @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
         @Throws(IOException::class)
         private fun openRenderer(context: Context,file : File) { // In this sample, we read a PDF from the assets directory.
             val file = file
             if (!file.exists()) { // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
                 // the cache directory.
                 val asset: InputStream = context.getAssets().open(FILENAME)
                 val output = FileOutputStream(file)
                 val buffer = ByteArray(1024)
                 var size: Int = 0
                 while (asset.read(buffer).also({ size = it }) != -1) {
                     output.write(buffer, 0, size)
                 }
                 asset.close()
                 output.close()
             }
             parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
             // This is the PdfRenderer we use to render the PDF.
             if (parcelFileDescriptor != null) {
                 pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
             }
         }

         @Throws(IOException::class)
         private fun closeRenderer() {
             currentPage?.close()
             pdfRenderer!!.close()
             parcelFileDescriptor!!.close()
         }


         @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
         private fun showPage(index: Int) {
             if (pdfRenderer!!.pageCount <= index) {
                 return
             }
             // Make sure to close the current page before opening another one.
             if (null != currentPage) {
                 currentPage?.close()
             }
             // Use `openPage` to open a specific page in PDF.
             currentPage = pdfRenderer!!.openPage(index)
             // Important: the destination bitmap must be ARGB (not RGB).
             val bitmap = currentPage?.getHeight()?.let {
                 Bitmap.createBitmap(
                     currentPage?.getWidth()!!, it,
                     Bitmap.Config.ARGB_8888
                 )
             }
             // Here, we render the page onto the Bitmap.
// To render a portion of the page, use the second and third parameter. Pass nulls to get
// the default result.
// Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
             if (bitmap != null) {
                 currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
             }
             // We are ready to show the Bitmap to user.
             pdf_image.setImageBitmap(bitmap)
             updateUi()
         }

         @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
         private fun updateUi() {
             val index = currentPage!!.index
             val pageCount = pdfRenderer!!.pageCount
             /*button_pre_doc.setEnabled(0 != index)
             button_next_doc.setEnabled(index + 1 < pageCount)*/
         }

         @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
         fun getPageCount(): Int {
             return pdfRenderer!!.pageCount
         }

}