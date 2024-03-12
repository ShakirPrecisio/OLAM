package com.kemar.olam.utility;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PrinterClass {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public  IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

    private PendingIntent mPermissionIntent;
    private boolean hasPermissionToCommunicate = true;

    private UsbManager mUsbManager;
    private DiscoveredPrinterUsb discoveredPrinterUsb;

    public PrinterClass(Context context) {
        // Register broadcast receiver that catches USB permission intent
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        requestPersimmion(context);
    }

    // Catches intent indicating if the user grants permission to use the USB device
    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            hasPermissionToCommunicate = true;
                        }
                    }
                }
            }
        }
    };

    private void requestPersimmion(Context context){

        new Thread(new Runnable() {

            public void run() {
                // Find connected printers
                UsbDiscoveryHandler handler = new UsbDiscoveryHandler();
                UsbDiscoverer.findPrinters(context, handler);

                try {
                    while (!handler.discoveryComplete) {
                        Thread.sleep(100);
                    }

                    if (handler.printers != null && handler.printers.size() > 0) {
                        discoveredPrinterUsb = handler.printers.get(0);

                        if (!mUsbManager.hasPermission(discoveredPrinterUsb.device)) {
                            mUsbManager.requestPermission(discoveredPrinterUsb.device, mPermissionIntent);
                        } else {
                            hasPermissionToCommunicate = true;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage() + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();

    }

    public void printData(Context context, Bitmap bitmap){
        if (hasPermissionToCommunicate) {
            Connection conn = null;
            try {
                conn = discoveredPrinterUsb.getConnection();
                conn.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(conn);

                conn.write("! U1 JOURNAL\r\n! U1 SETFF 100 2\r\n".getBytes());

                ZebraImageAndroid zebraImageToPrint = new ZebraImageAndroid(bitmap);
                printer.printImage(zebraImageToPrint, 0, 0, -1, -1, false);

            } catch (ConnectionException | ZebraPrinterLanguageUnknownException e) {
                Toast.makeText(context, e.getMessage() + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Toast.makeText(context, "No permission to communicate", Toast.LENGTH_LONG).show();
        }
    }

    // Handles USB device discovery
    class UsbDiscoveryHandler implements DiscoveryHandler {
        public List<DiscoveredPrinterUsb> printers;
        public boolean discoveryComplete = false;

        public UsbDiscoveryHandler() {
            printers = new LinkedList<DiscoveredPrinterUsb>();
        }

        public void foundPrinter(final DiscoveredPrinter printer) {
            printers.add((DiscoveredPrinterUsb) printer);
        }

        public void discoveryFinished() {
            discoveryComplete = true;
        }

        public void discoveryError(String message) {
            discoveryComplete = true;
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public ArrayList<Bitmap> pdfToBitmap(Context context, File pdfFile) {
//        ArrayList<Bitmap> bitmaps = new ArrayList<>();
//
//        try {
//            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
//
//            Bitmap bitmap;
//            final int pageCount = renderer.getPageCount();
//            for (int i = 0; i < pageCount; i++) {
//                PdfRenderer.Page page = renderer.openPage(i);
//
//                int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
//                int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
//                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//
//                bitmaps.add(bitmap);
//
//                // close the page
//                page.close();
//
//            }
//
//            // close the renderer
//            renderer.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return bitmaps;
//
//    }

}
