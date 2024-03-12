package com.kemar.olam.bluetooth_printer.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.kemar.olam.R;
import com.kemar.olam.bordereau.model.request.BodereuLogListing;
import com.kemar.olam.forestry_management.model.BarcodeGenerationRequest;
import com.kemar.olam.utility.Constants;
import com.kemar.olam.utility.DemoSleeper;
import com.kemar.olam.utility.SharedPref;
import com.kemar.olam.utility.UIHelper;
import com.kemar.olam.utility.Utility;
import com.zebra.sdk.btleComm.BluetoothLeConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class ReceiptDemo extends ConnectionScreen {

    private UIHelper helper = new UIHelper(this);
    Connection printerConnection = null;
    String supplierName = "";
    String FSC_NO_FSC = "";
    String NONFSC_NO_FSC = "";
    String barcodeValue = "";
    String moduleName = "";
    BodereuLogListing logModel;
    BarcodeGenerationRequest barcodeGenerationModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra("moduleName")){
            moduleName = getIntent().getStringExtra("moduleName");
        }

        if(Objects.equals(moduleName, getString(R.string.forestry_management))){
            barcodeGenerationModel = new Gson().fromJson(getIntent().getStringExtra("BARCODE_DATA"), BarcodeGenerationRequest.class);
        } else {
            supplierName = getIntent().getStringExtra("Supplier_Name");
            logModel = new Gson().fromJson(getIntent().getStringExtra("LOG_DATA"), BodereuLogListing.class);
            FSC_NO_FSC = getIntent().getStringExtra("FSC_NO_FSC");
            barcodeValue = getIntent().getStringExtra("BARCODE_Value");
        }

    }

    @Override
    public void performTest() {
        executeTest();
    }

    public void executeTest() {
        new Thread(new Runnable() {
            public void run() {
                if (Looper.myLooper() == null)
                {
                    Looper.prepare();
                }
                connectAndSendLabel();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

    }

    private void connectAndSendLabel() {
        printerConnection = new BluetoothLeConnection(macAddress, this);
        try {
            helper.showLoadingDialog("Connecting...");
            printerConnection.open();

            ZebraPrinter printer = null;

            if (printerConnection.isConnected()) {
                printer = ZebraPrinterFactory.getInstance(printerConnection);

                if (printer != null) {
                    PrinterLanguage pl = printer.getPrinterControlLanguage();
                    if (pl == PrinterLanguage.CPCL) {
                        helper.showErrorDialogOnGuiThread("This app will not work for CPCL printers!");
                    } else {
//                        if(Objects.equals(moduleName, getString(R.string.forestry_management))){
//                            for(int i = 0; i < Integer.parseInt(barcodeGenerationModel.getTotalBarcodeNos()); i++) {
//                                sendForestryManagementBarcodes(System.currentTimeMillis());
//                            }
//                        }else
                            sendTestLabel();
                    }
                    printerConnection.close();
                }
            }
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread("Could not detect printer language");
        } finally {
            helper.dismissLoadingDialog();
            finish();
        }
    }

//    private void sendForestryManagementBarcodes(long treeId) {
////        try {
////            byte[] configLabel = createZplReceipt().getBytes();
//            for(int j = 0; j < Integer.parseInt(barcodeGenerationModel.getCopies()); j++) {
//                Log.d("ForestBarcode", "barcode copy" + j + " = " + treeId);
////                printerConnection.write(configLabel);
//            }
////            DemoSleeper.sleep(1500);
////            if (printerConnection instanceof BluetoothLeConnection) {
////                DemoSleeper.sleep(1000);
////            }
////        } catch (ConnectionException e) {
////            e.printStackTrace();
////        }
//    }

    private void sendTestLabel() {
        try {
            byte[] configLabel = createZplReceipt().getBytes();
            printerConnection.write(configLabel);
            DemoSleeper.sleep(1500);
            if (printerConnection instanceof BluetoothLeConnection) {
                DemoSleeper.sleep(1000);
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

//    private void sendTestLabelWithMStringJobs(Connection printerConnection) {
//        try {
//            sendZplReceipt(printerConnection);
//        } catch (ConnectionException e) {
//            helper.showErrorDialogOnGuiThread(e.getMessage());
//        }
//
//    }

    private String createZplReceipt() {



        Integer totalDia12=logModel.getDiamBdx1()  + logModel.getDiamBdx2();
        Integer totalDia34=logModel.getDiamBdx3()  + logModel.getDiamBdx4();


        Integer avgDia12= Integer.parseInt(String.valueOf((totalDia12/2)));
        Integer avgDia34= Integer.parseInt(String.valueOf((totalDia34/2)));




        if(FSC_NO_FSC.equals("FSC")){
            NONFSC_NO_FSC="NON FSC";
        }else{
            NONFSC_NO_FSC="FSC";
        }
        /*String tmpHeader="^XA\n" +
                        "^MMT\n" +
                        "^PW830\n" +
                        "^LL0609\n" +
                        "^LS0\n" +
                        "^FO288,416^GFA,00768,00768,00012,:Z64:\n" +
                        "eJxjYKAxYPwPBv/wsj8YgIAdBvuEBwj4gdlHWkCgD8w+zAwCfBhsZDXIekfNp7/5RMT7IAAA5GqFVA==:BD66\n" +
                        "^FT148,284^A0N,28,28^FH\\^FD"+logModel.getLogNo()+"^FS\n" +
                        "^FT288,284^A0N,28,28^FH\\^FD"+logModel.getLogSpeciesName()+"^FS\n" +
                        "^FT483,284^A0N,28,28^FH\\^FD"+supplierName+"^FS\n" +
                        "^FT148,337^A0N,28,28^FH\\^FD"+barcodeValue+"^FS\n" +
                        "^FT409,402^A0N,31,31^FH\\^FDAD:"+logModel.getDiamBdx().toString()+"  L:"+logModel.getLongBdx().toString()+" "+logModel.getCbm().toString()+" m3^FS\n" +
                        "^FO148,424^GB56,45,3^FS\n" +
                        "^FT231,461^A0N,28,28^FH\\^FD"+NONFSC_NO_FSC+"^FS\n" +
                        "^FT388,461^A0N,28,28^FH\\^FD"+FSC_NO_FSC+"^FS\n" +
                        "^FT148,402^A0N,31,31^FH\\^FDD1:"+avgDia12+"     D2:"+avgDia34+" ^FS\n" +
                        "^BY4,3,160^FT165,208^B3N,N,,Y,N\n" +
                        "^FD"+logModel.getBarcodeNumber()+"^FS\n" +
                        "^PQ1,0,1,Y^XZ\n";*/

        String tmpHeader=
                "\u0010CT~~CD,~CC^~CT~\n" +
                        "^XA\n" +
                        "^PW769\n" +
                        "^FO352,352^GFA,00768,00768,00008,:Z64:\n" +
                        "eJxjYBjeoP4/GDSg0zU8IMAPp4uEQEC4oVARBJQbCg6AwGE4DROHqRup+nGF50DHM7kAAFEhiqw=:E30D\n" +
                        "^BY3,3,106^FT60,142^B3N,N,,Y,N\n" +
                        "^FD"+logModel.getBarcodeNumber()+"^FS\n" +
                        "^FT97,235^A0N,31,31^FH\\^FD"+logModel.getLogNo()+"^FS\n" +
                        "^FT241,235^A0N,31,31^FH\\^FD"+logModel.getLogSpeciesName()+"^FS\n" +
                        "^FT457,235^A0N,31,31^FH\\^FD"+supplierName+"^FS\n" +
                        "^FT97,291^A0N,31,31^FH\\^FD"+barcodeValue+"/"+logModel.getAACYear().toString()+"^FS\n" +
                        "^FT97,347^A0N,31,31^FH\\^FDD1:"+avgDia12+"    D2:"+avgDia34+"^FS\n" +
                        "^FT361,347^A0N,31,31^FH\\^FDAD:"+logModel.getDiamBdx().toString()+"    L:"+logModel.getLongBdx().toString()+"  "+logModel.getCbm().toString()+" m3^FS\n" +
                        "^FT169,411^A0N,31,31^FH\\^FD"+NONFSC_NO_FSC+"^FS\n" +
                        "^FT425,411^A0N,31,31^FH\\^FD"+FSC_NO_FSC+"^FS\n" +
                        "^FO97,380^GB56,45,3^FS\n" +
                        "^PQ2,0,1,Y^XZ\n";

        /* String tmpHeader=
                "\u0010CT~~CD,~CC^~CT~\n" +
                        "^XA\n" +
                        "^PW769\n" +
                        "^FO352,352^GFA,00768,00768,00008,:Z64:\n" +
                        "eJxjYBjeoP4/GDSg0zU8IMAPp4uEQEC4oVARBJQbCg6AwGE4DROHqRup+nGF50DHM7kAAFEhiqw=:E30D\n" +
                        "^BY3,3,106^FT61,142^B3N,N,,Y,N\n" +
                        "^FD"+logModel.getBarcodeNumber()+"^FS\n" +
                        "^FT97,235^A0N,31,31^FH\\^FD"+logModel.getLogNo()+"^FS\n" +
                        "^FT241,235^A0N,31,31^FH\\^FD"+logModel.getLogSpeciesName()+"^FS\n" +
                        "^FT457,235^A0N,31,31^FH\\^FD"+supplierName+"^FS\n" +
                        "^FT97,291^A0N,31,31^FH\\^FD"+barcodeValue+"/"+logModel.getAACYear().toString()+"^FS\n" +
                        "^FT97,347^A0N,31,31^FH\\^FDD1:"+avgDia12+"    D2:"+avgDia34+"^FS\n" +
                        "^FT361,347^A0N,31,31^FH\\^FDAD:"+logModel.getDiamBdx().toString()+"    L:"+logModel.getLongBdx().toString()+"  "+logModel.getCbm().toString()+" m3^FS\n" +
                        "^FT169,411^A0N,31,31^FH\\^FD"+NONFSC_NO_FSC+"^FS\n" +
                        "^FT425,411^A0N,31,31^FH\\^FD"+FSC_NO_FSC+"^FS\n" +
                        "^FO97,380^GB56,45,3^FS\n" +
                        "^PQ1,0,1,Y^XZ\n";*/



       /* String tmpHeader=
                "\u0010CT~~CD,~CC^~CT~\n" +
                        "^XA\n" +
                        "^PW769\n" +
                        "^FO352,352^GFA,00768,00768,00008,:Z64:\n" +
                        "eJxjYBjeoP4/GDSg0zU8IMAPp4uEQEC4oVARBJQbCg6AwGE4DROHqRup+nGF50DHM7kAAFEhiqw=:E30D\n" +
                        "^BY4,3,107^FT105,143^BAN,,Y,N\n" +
                        "^FD"+logModel.getBarcodeNumber()+"^FS\n" +
                        "^FT97,235^A0N,31,31^FH\\^FD"+logModel.getLogNo()+"^FS\n" +
                        "^FT241,235^A0N,31,31^FH\\^FD"+logModel.getLogSpeciesName()+"^FS\n" +
                        "^FT457,235^A0N,31,31^FH\\^FD"+supplierName+"^FS\n" +
                        "^FT97,291^A0N,31,31^FH\\^FD"+barcodeValue+"/"+logModel.getAACYear().toString()+"^FS\n" +
                        "^FT97,347^A0N,31,31^FH\\^FDD1:"+avgDia12+"    D2:"+avgDia34+"^FS\n" +
                        "^FT361,347^A0N,31,31^FH\\^FDAD:"+logModel.getDiamBdx().toString()+"    L:"+logModel.getLongBdx().toString()+"  "+logModel.getCbm().toString()+" m3^FS\n" +
                        "^FT169,411^A0N,31,31^FH\\^FD"+NONFSC_NO_FSC+"^FS\n" +
                        "^FT425,411^A0N,31,31^FH\\^FD"+FSC_NO_FSC+"^FS\n" +
                        "^FO97,380^GB56,45,3^FS\n" +
                        "^PQ1,0,1,Y^XZ\n";*/






        int headerHeight = 325;
//        String body = String.format("^LH0,%d", headerHeight);
//
//        int heightOfOneLine = 40;
//
//        float totalPrice = 0;
//
//        Map<String, String> itemsToPrint = createListOfItems();
//
//        int i = 0;
//        for (String productName : itemsToPrint.keySet()) {
//            String price = itemsToPrint.get(productName);
//
//            String lineItem = "^FO50,%d" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD%s^FS" + "\r\n" + "^FO280,%d" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD$%s^FS";
//            totalPrice += Float.parseFloat(price);
//            int totalHeight = i++ * heightOfOneLine;
//            body += String.format(lineItem, totalHeight, productName, totalHeight, price);
//
//        }
//
        long totalBodyHeight = 0;
//
//        long footerStartPosition = headerHeight + totalBodyHeight;

//        String footer = String.format("^LH0,%d" + "\r\n" +
//
//        "^FO50,1" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +
//
//        "^FO50,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FDTotal^FS" + "\r\n" +
//
//        "^FO175,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FD$%.2f^FS" + "\r\n" +
//
//        "^FO50,130" + "\r\n" + "^A0,N,45,45" + "\r\n" + "^FDPlease Sign Below^FS" + "\r\n" +
//
//        "^FO50,190" + "\r\n" + "^GB350,200,2,B^FS" + "\r\n" +
//
//        "^FO50,400" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +
//
//        "^FO50,420" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDThanks for choosing us!^FS" + "\r\n" +
//
//        "^FO50,470" + "\r\n" + "^B3N,N,45,Y,N" + "\r\n" + "^FD0123456^FS" + "\r\n" + "^XZ", footerStartPosition, totalPrice);

        long footerHeight = 600;
        long labelLength = headerHeight + totalBodyHeight + footerHeight;

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = sdf.format(date);

        String header = String.format(tmpHeader, labelLength, dateString);

        String wholeZplLabel = tmpHeader;

        return wholeZplLabel;
    }

//    private void sendZplReceipt(Connection printerConnection) throws ConnectionException {
//        /*
//         This routine is provided to you as an example of how to create a variable length label with user specified data.
//         The basic flow of the example is as follows
//
//            Header of the label with some variable data
//            Body of the label
//                Loops thru user content and creates small line items of printed material
//            Footer of the label
//
//         As you can see, there are some variables that the user provides in the header, body and footer, and this routine uses that to build up a proper ZPL string for printing.
//         Using this same concept, you can create one label for your receipt header, one for the body and one for the footer. The body receipt will be duplicated as mString items as there are in your variable data
//
//         */
//
//        String tmpHeader =
//        /*
//         Some basics of ZPL. Find more information here : http://www.zebra.com
//
//         ^XA indicates the beginning of a label
//         ^PW sets the width of the label (in dots)
//         ^MNN sets the printer in continuous mode (variable length receipts only make sense with variably sized labels)
//         ^LL sets the length of the label (we calculate this value at the end of the routine)
//         ^LH sets the reference axis for printing.
//            You will notice we change this positioning of the 'Y' axis (length) as we build up the label. Once the positioning is changed, all new fields drawn on the label are rendered as if '0' is the new home position
//         ^FO sets the origin of the field relative to Label Home ^LH
//         ^A sets font information
//         ^FD is a field description
//         ^GB is graphic boxes (or lines)
//         ^B sets barcode information
//         ^XZ indicates the end of a label
//         */
//
//                "^XA" +
//
//                        "^POI^PW400^MNN^LL325^LH0,0" + "\r\n" +
//
//                        "^FO50,50" + "\r\n" + "^A0,N,70,70" + "\r\n" + "^FD Shipping^FS" + "\r\n" +
//
//                        "^FO50,130" + "\r\n" + "^A0,N,35,35" + "\r\n" + "^FDPurchase Confirmation^FS" + "\r\n" +
//
//                        "^FO50,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDCustomer:^FS" + "\r\n" +
//
//                        "^FO225,180" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDAcme Industries^FS" + "\r\n" +
//
//                        "^FO50,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDDelivery Date:^FS" + "\r\n" +
//
//                        "^FO225,220" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FD%s^FS" + "\r\n" +
//
//                        "^FO50,273" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDItem^FS" + "\r\n" +
//
//                        "^FO280,273" + "\r\n" + "^A0,N,25,25" + "\r\n" + "^FDPrice^FS" + "\r\n" +
//
//                        "^FO50,300" + "\r\n" + "^GB350,5,5,B,0^FS" + "^XZ";
//
//        int headerHeight = 325;
//
//        Date date = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//        String dateString = sdf.format(date);
//
//        String header = String.format(tmpHeader, dateString);
//
//        printerConnection.write(header.getBytes());
//
//        int heightOfOneLine = 40;
//
//        float totalPrice = 0;
//
//        Map<String, String> itemsToPrint = createListOfItems();
//
//        int i = 0;
//        for (String productName : itemsToPrint.keySet()) {
//            String price = itemsToPrint.get(productName);
//
//            String lineItem = "^XA^POI^LL40" + "^FO50,10" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD%s^FS" + "\r\n" + "^FO280,10" + "\r\n" + "^A0,N,28,28" + "\r\n" + "^FD$%s^FS" + "^XZ";
//            totalPrice += Float.parseFloat(price);
//            String oneLineLabel = String.format(lineItem, productName, price);
//
//            printerConnection.write(oneLineLabel.getBytes());
//
//        }
//
//        long totalBodyHeight = (itemsToPrint.size() + 1) * heightOfOneLine;
//
//        long footerStartPosition = headerHeight + totalBodyHeight;
//
//        String footer = String.format("^XA^POI^LL600" + "\r\n" +
//
//                "^FO50,1" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +
//
//                "^FO50,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FDTotal^FS" + "\r\n" +
//
//                "^FO175,15" + "\r\n" + "^A0,N,40,40" + "\r\n" + "^FD$%.2f^FS" + "\r\n" +
//
//                "^FO50,130" + "\r\n" + "^A0,N,45,45" + "\r\n" + "^FDPlease Sign Below^FS" + "\r\n" +
//
//                "^FO50,190" + "\r\n" + "^GB350,200,2,B^FS" + "\r\n" +
//
//                "^FO50,400" + "\r\n" + "^GB350,5,5,B,0^FS" + "\r\n" +
//
//                "^FO50,420" + "\r\n" + "^A0,N,30,30" + "\r\n" + "^FDThanks for choosing us!^FS" + "\r\n" +
//
//                "^FO50,470" + "\r\n" + "^B3N,N,45,Y,N" + "\r\n" + "^FD0123456^FS" + "\r\n" + "^XZ", totalPrice);
//
//        printerConnection.write(footer.getBytes());
//
//    }
}

