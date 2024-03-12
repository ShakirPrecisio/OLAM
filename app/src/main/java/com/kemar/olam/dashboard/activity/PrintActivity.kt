package com.kemar.olam.dashboard.activity

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_print.*
import kotlinx.android.synthetic.main.row_print_reciept.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class PrintActivity : AppCompatActivity() {
    var  FULL_PATH  : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        ivBarcode.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.bg_splash))
        btnGeneratNPrint.setOnClickListener({
            acessRuntimPermission()
        })

    }



    fun acessRuntimPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                savePdf()
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
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun savePdf() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        //pdf file path
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName +".pdf"
        FULL_PATH = mFilePath
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()

            //get text from EditText i.e. textEt
            val mText ="Test PDF"

         parentView.setDrawingCacheEnabled(true);
         parentView.buildDrawingCache();
            var bitmap = parentView.getDrawingCache();
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
           var  image = Image.getInstance(imageInByte);

            //add author of the document (metadata)
            mDoc.addAuthor("OLAM")

                 var  scaler = ((mDoc.getPageSize().getWidth() - mDoc.leftMargin()
                    - mDoc.rightMargin() - 0) / image.getWidth()) * 100;
            image.scalePercent(scaler);
           image.setAlignment(Image.ALIGN_CENTER)/* | Image.ALIGN_TOP);*/

            //add paragraph to the document
          //  mDoc.add(Paragraph(mText))
           //add Image to the document
            mDoc.add(image)
            //close document
            mDoc.close()

            //show file saved message with file name and path
            Toast.makeText(this, "$mFileName.pdf\nis saved to\n$mFilePath", Toast.LENGTH_SHORT).show()
            printPDF(FULL_PATH)
        }
        catch (e: Exception){
            //if Stringthing goes wrong causing exception, get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }



    fun printPDF(pdfPath:String){
        val printManager =
            getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, pdfPath)
        printManager.print("Document",printAdapter,PrintAttributes.Builder().build())
    }

}