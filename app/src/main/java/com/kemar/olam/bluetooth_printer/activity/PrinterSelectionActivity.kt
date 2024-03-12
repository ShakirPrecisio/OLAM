package com.kemar.olam.bluetooth_printer.activity

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.kemar.olam.R
import com.kemar.olam.utility.UIHelper
import com.zebra.sdk.btleComm.BluetoothLeConnection
import com.zebra.sdk.btleComm.BluetoothLeDiscoverer
import com.zebra.sdk.btleComm.DiscoveredPrinterBluetoothLe
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.PrinterLanguage
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveryHandler

class PrinterSelectionActivity : AppCompatActivity() {
    var filePath: String? = null
    private val helper: UIHelper = UIHelper(this)
    var printerConnection: Connection? = null
    var discoverPrintersButton: Button? = null
    private var context: Context? = null
    private var mArrayAdapter: ArrayAdapter<String>? = null
    var discoverPrintersList: ListView? = null
    var macAddress: String? = null
    var discoveredPrintersClickListener = OnItemClickListener { parent, view, position, id ->
        println("Clicked on $position position\n")
        Thread {
            discoverPrintersList!!.isClickable = false
            val printerInformation = discoverPrintersList!!.getItemAtPosition(position).toString()
            macAddress = printerInformation.split("\n".toRegex()).toTypedArray()[1]
            Looper.prepare()
            connectAndSendLabel()
            Looper.loop()
            Looper.myLooper()!!.quit()
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_selection)

        filePath = getIntent().getStringExtra("EXTRA_FULL_PATH");

        context = this
        mArrayAdapter = ArrayAdapter(context!!, R.layout.discovery_results_for_demos)

        discoverPrintersButton = findViewById<View>(R.id.discoverPrintersButton) as Button
        discoverPrintersButton!!.setOnClickListener {
            discoverPrintersButton!!.isClickable = false
            mArrayAdapter!!.clear()
            mArrayAdapter!!.notifyDataSetChanged()
            try {
                BluetoothLeDiscoverer.findPrinters(context, object : DiscoveryHandler {
                    override fun foundPrinter(discoveredPrinter: DiscoveredPrinter) {
                        if (null != (discoveredPrinter as DiscoveredPrinterBluetoothLe).friendlyName) {
                            print("""Found ${discoveredPrinter.friendlyName}""".trimIndent())
                            runOnUiThread {
                                mArrayAdapter!!.add("""${discoveredPrinter.friendlyName}${discoveredPrinter.address}""".trimIndent())
                                mArrayAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun discoveryFinished() {
                        if (discoverPrintersList!!.isClickable) {
                            enableDiscoveredPrinterButtonClick()
                        }
                    }

                    override fun discoveryError(message: String) {
                        Log.d("btError",message.toString())
                        if (discoverPrintersList!!.isClickable) {
                            enableDiscoveredPrinterButtonClick()
                        }
                    }
                })
            } catch (e: ConnectionException) {
                e.printStackTrace()
            }
        }

        discoverPrintersList = findViewById<View>(R.id.discoveredPrintersList) as ListView
        discoverPrintersList!!.adapter = mArrayAdapter
        discoverPrintersList!!.onItemClickListener = discoveredPrintersClickListener


    }


    fun enableDiscoveredPrintersListClick() {
        discoverPrintersList!!.isClickable = true
    }

    fun enableDiscoveredPrinterButtonClick() {
        discoverPrintersButton!!.isClickable = true
    }

    private fun connectAndSendLabel() {
        printerConnection = BluetoothLeConnection(macAddress, this)
        if (printerConnection != null) {
            try {
                helper.showLoadingDialog("Connecting...")
                printerConnection!!.open()
                var printer: ZebraPrinter? = null
                if (printerConnection!!.isConnected()) {
                    printer = ZebraPrinterFactory.getInstance(printerConnection)
                    if (printer != null) {
                        val pl = printer.printerControlLanguage
                        if (pl == PrinterLanguage.CPCL) {
                            helper.showErrorDialogOnGuiThread("This demo will not work for CPCL printers!")
                        } else {
                            // [self.connectivityViewController setStatus:@"Building receipt in ZPL..." withColor:[UIColor
                            // cyanColor]];
                            sendZplReceipt(printerConnection!!)

                        }
                        printerConnection!!.close()
                    }
                }
            } catch (e: ConnectionException) {
                helper.showErrorDialogOnGuiThread(e.message)
            } catch (e: ZebraPrinterLanguageUnknownException) {
                helper.showErrorDialogOnGuiThread("Could not detect printer language")
            } finally {
                helper.dismissLoadingDialog()
            }
        } else {
            helper.showErrorDialogOnGuiThread("Could not detect printer")
        }
    }

    @Throws(ConnectionException::class)
    private fun sendZplReceipt(printerConnection: Connection) {

//        val tmpHeader =  """
//            ^XA^POI^PW400^MNN^LL325^LH0,0
//            ^FO50,50
//            ^A0,N,70,70
//            ^FD Shipping^FS
//            ^FO50,130
//            ^A0,N,35,35
//            ^FDPurchase Confirmation^FS
//            ^FO50,180
//            ^A0,N,25,25
//            ^FDCustomer:^FS
//            ^FO225,180
//            ^A0,N,25,25
//            ^FDAcme Industries^FS
//            ^FO50,220
//            ^A0,N,25,25
//            ^FDDelivery Date:^FS
//            ^FO225,220
//            ^A0,N,25,25
//            ^FD%s^FS
//            ^FO50,273
//            ^A0,N,30,30
//            ^FDItem^FS
//            ^FO280,273
//            ^A0,N,25,25
//            ^FDPrice^FS
//            ^FO50,300
//            ^GB350,5,5,B,0^FS^XZ
//            """.trimIndent()
//        val headerHeight = 325
//        val date = Date()
//        val sdf = SimpleDateFormat("yyyy/MM/dd")
//        val dateString = sdf.format(date)
//        val header = String.format(tmpHeader, dateString)
//        printerConnection.write(header.toByteArray())
//        val heightOfOneLine = 40
//        var totalPrice = 0f
//        val itemsToPrint: Map<String, String> = createListOfItems()
//        val i = 0
//        for (productName in itemsToPrint.keys) {
//            val price = itemsToPrint[productName]
//            val lineItem = """
//            ^XA^POI^LL40^FO50,10
//            ^A0,N,28,28
//            ^FD%s^FS
//            ^FO280,10
//            ^A0,N,28,28
//            ^FD$%s^FS^XZ
//            """.trimIndent()
//            totalPrice += price!!.toFloat()
//            val oneLineLabel = String.format(lineItem, productName, price)
//            printerConnection.write(oneLineLabel.toByteArray())
//        }
//        val totalBodyHeight = ((itemsToPrint.size + 1) * heightOfOneLine).toLong()
//        val footerStartPosition = headerHeight + totalBodyHeight

        val footer = String.format("^XA~TA000~JSN^LT0^MNN^MTD^PON^PMN^LH0,0^JMA^PR4,4~SD25^JUS^LRN^CI0^XZ\n" +
                "^XA\n" +
                "^MMT\n" +
                "^PW719\n" +
                "^LL0607\n" +
                "^LS0\n" +
                "^BY4,3,160^FT627,389^BCI,,Y,N\n" +
                "^FD>:barcodeId^FS\n" +
                "^FT560,251^A0I,28,28^FH\\^FDtxtBCLog^FS\n" +
                "^PQ1,0,1,Y^XZ".trimIndent())
        printerConnection.write(footer.toByteArray())
    }

}