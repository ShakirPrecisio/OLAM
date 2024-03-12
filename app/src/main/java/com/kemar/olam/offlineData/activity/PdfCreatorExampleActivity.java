package com.kemar.olam.offlineData.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.kemar.olam.R;
import com.kemar.olam.loading_wagons.model.request.LoadingRequest;
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest;
import com.kemar.olam.utility.Constants;
import com.kemar.olam.utility.RealmHelper;
import com.kemar.olam.utility.Utility;
import com.tejpratapsingh.pdfcreator.activity.PDFCreatorActivity;
import com.tejpratapsingh.pdfcreator.utils.PDFUtil;
import com.tejpratapsingh.pdfcreator.views.PDFBody;
import com.tejpratapsingh.pdfcreator.views.PDFFooterView;
import com.tejpratapsingh.pdfcreator.views.PDFHeaderView;
import com.tejpratapsingh.pdfcreator.views.PDFTableView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFHorizontalView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFImageView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFLineSeparatorView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFTextView;
import com.tejpratapsingh.pdfcreator.views.basic.PDFVerticalView;

import java.io.File;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class PdfCreatorExampleActivity extends PDFCreatorActivity {

    Realm realm;
    LoadingRequest loadingRequests;

    RealmResults<WagonLogRequest> wagonLogRequests;

    int OkumeCount=0;
    int otherCount=0;

    double cubantOkumeSum=0.0;
    double othercubantSum=0.0;
    double totalTonnes=0.0;

    String  OKOUME="OKOUME";

    Boolean isGSEBOrForestDeclaration  = false;

    Utility mUtils= new Utility();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = RealmHelper.getRealmInstance();

        String uniqueId = getIntent().getStringExtra(Constants.UNIQUEID);
        isGSEBOrForestDeclaration=getIntent().getBooleanExtra(Constants.isGESZ,false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                loadingRequests= realm.where(LoadingRequest.class).equalTo("uniqueId", uniqueId).findFirst();
                wagonLogRequests= realm.where(WagonLogRequest.class).equalTo("forestuniqueId", uniqueId).findAll();
            }
        });


        createPDF("test", new PDFUtil.PDFUtilListener() {
            @Override
            public void pdfGenerationSuccess(File savedPDFFile) {
                Toast.makeText(PdfCreatorExampleActivity.this, "PDF Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void pdfGenerationFailure(Exception exception) {
                Toast.makeText(PdfCreatorExampleActivity.this, "PDF NOT Created", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    protected PDFHeaderView getHeaderView(int pageIndex) {
        PDFHeaderView headerView = new PDFHeaderView(getApplicationContext());

        PDFHorizontalView horizontalView = new PDFHorizontalView(getApplicationContext());

        PDFTextView pdfTextView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString feuili = new SpannableString("FEUILLE DE ROUTE");
        feuili.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, feuili.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pdfTextView.setText(feuili);
        pdfTextView.getView().setGravity(Gravity.CENTER);
        pdfTextView.getView().setTypeface(pdfTextView.getView().getTypeface(), Typeface.BOLD);
        PDFImageView imageView = new PDFImageView(getApplicationContext());
        LinearLayout.LayoutParams txtLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        txtLayoutParam.setMargins(80, 0, 10, 0);
        pdfTextView.setLayout(txtLayoutParam);

        LinearLayout.LayoutParams imageLayoutParam = new LinearLayout.LayoutParams(
                60,
                60, 0);
        imageView.setImageScale(ImageView.ScaleType.CENTER_INSIDE);
        if(isGSEBOrForestDeclaration) {
            imageView.setImageResource(R.drawable.gsez_logo_new);
        }else{
            if(loadingRequests.getForestName().equals("GAW")){
                imageView.setImageResource(R.drawable.gaw_logo);
            }else{
                imageView.setImageResource(R.drawable.gsez_logo_new);
            }
        }
        imageLayoutParam.setMargins(0, 0, 10, 0);
        imageView.setLayout(imageLayoutParam);
        horizontalView.addView(imageView);

        // left side text Visa des Eaux et Forets
        PDFTextView pdfTextView1= new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString feuili1 = new SpannableString("Visa des Eaux et Forets");
        feuili1.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, feuili1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pdfTextView1.setText(feuili1);

        LinearLayout.LayoutParams txtLayoutParam1 = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        txtLayoutParam1.setMargins(0, 20, 10, 0);

        pdfTextView1.setLayout(txtLayoutParam1);
        pdfTextView1.getView().setGravity(Gravity.END);
        pdfTextView1.getView().setTypeface(pdfTextView1.getView().getTypeface(), Typeface.BOLD);

        horizontalView.addView(pdfTextView);
        horizontalView.addView(pdfTextView1);
        headerView.addView(horizontalView);
        PDFLineSeparatorView lineSeparatorView1 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        headerView.addView(lineSeparatorView1);


        return headerView;
    }

    @Override
    protected PDFBody getBodyViews() {
        PDFBody pdfBody = new PDFBody();

        // data hearder and trasnpoter name
        PDFHorizontalView headerContent = new PDFHorizontalView(getApplicationContext());

        PDFTextView transPortsNameView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString transsportTxt = new SpannableString("");
        transsportTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, transsportTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        transPortsNameView.setText(transsportTxt);
        transPortsNameView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        transPortsNameView.getView().setTypeface(transPortsNameView.getView().getTypeface(), Typeface.BOLD);
        headerContent.addView(transPortsNameView);


        PDFTextView trucksView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString Trusck = new SpannableString("");
        Trusck.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, Trusck.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        trucksView.setText(Trusck);
        trucksView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        trucksView.getView().setTypeface(trucksView.getView().getTypeface(), Typeface.NORMAL);

        headerContent.addView(trucksView);


        PDFTextView ORIGINEView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
//        SpannableString ORIGINE = new SpannableString("ORIGINE");
        SpannableString ORIGINE = new SpannableString("");
        ORIGINE.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, ORIGINE.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ORIGINEView.setText(ORIGINE);
        ORIGINEView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        ORIGINEView.getView().setTypeface(ORIGINEView.getView().getTypeface(), Typeface.BOLD);

        headerContent.addView(ORIGINEView);

        PDFTextView originText = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString originValue = new SpannableString("");//loadingRequests.getOriginName());
        originValue.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, originValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        originText.setText(originValue);
        originText.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        originText.getView().setTypeface(originText.getView().getTypeface(), Typeface.NORMAL);

        headerContent.addView(originText);
        pdfBody.addView(headerContent);

        ////second


        // data hearder and trasnpoter name
        PDFHorizontalView secondContent = new PDFHorizontalView(getApplicationContext());


        PDFTextView transPortNameView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString transportTxt = new SpannableString("TRANSPORT PAR");
        transportTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, transportTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        transPortNameView.setText(transportTxt);
        transPortNameView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        transPortNameView.getView().setTypeface(transPortNameView.getView().getTypeface(), Typeface.BOLD);
        secondContent.addView(transPortNameView);


        PDFTextView truckView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        String transportPar="";
        if(loadingRequests.getModeOfTransport()==3){
            transportPar="Truck";
        }else if(loadingRequests.getModeOfTransport()==1){
            transportPar="Wagon";
        }else if(loadingRequests.getModeOfTransport()==17){
            transportPar="Barge";

        }
        SpannableString Truck = new SpannableString(transportPar);
        Truck.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, Truck.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        truckView.setText(Truck);
        truckView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        truckView.getView().setTypeface(truckView.getView().getTypeface(), Typeface.NORMAL);

        secondContent.addView(truckView);






        PDFTextView destinationView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString destinationTxt = new SpannableString("DESTINATION");
        destinationTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, destinationTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        destinationView.setText(destinationTxt);
        destinationView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        destinationView.getView().setTypeface(destinationView.getView().getTypeface(), Typeface.BOLD);

        secondContent.addView(destinationView);

        try {
            PDFTextView destContentView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);

            String destination = "";
            if (loadingRequests.getDestination() != null) {
                if (!loadingRequests.getDestination().isEmpty()) {
                    destination = loadingRequests.getDestination();
                } else {
                    destination = loadingRequests.getDestination();
                }
            } else {
                destination = loadingRequests.getDestination();
            }
            //String destination=(loadingRequests.getDestinationName()==null )?"":loadingRequests.getDestination();
            SpannableString destinationContentTxt = new SpannableString(destination);
            destinationContentTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, destinationContentTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            destContentView.setText(destinationContentTxt);

            destContentView.setLayout(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            destContentView.getView().setTypeface(destContentView.getView().getTypeface(), Typeface.NORMAL);
            secondContent.addView(destContentView);

            pdfBody.addView(secondContent);
        }catch (Exception e){
            e.printStackTrace();
        }



        // data hearder and BORDEREAU NO. name
        PDFHorizontalView thirdContent = new PDFHorizontalView(getApplicationContext());

        PDFTextView forestNameView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
//        SpannableString forestTxt = new SpannableString("FOREST NAME ");
        SpannableString forestTxt = new SpannableString("");
        forestTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, forestTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forestNameView.setText(forestTxt);
        forestNameView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        forestNameView.getView().setTypeface(forestNameView.getView().getTypeface(), Typeface.BOLD);
        thirdContent.addView(forestNameView);



        PDFTextView rougierView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString rougierTxt = new SpannableString("");//loadingRequests.getForestName());
        rougierTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, rougierTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rougierView.setText(rougierTxt);
        rougierView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        rougierView.getView().setTypeface(rougierView.getView().getTypeface(), Typeface.NORMAL);

        thirdContent.addView(rougierView);




        PDFTextView loadingPointView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString loadingPointText = new SpannableString("LOADING POINT");
        loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, loadingPointText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        loadingPointView.setText(loadingPointText);
        loadingPointView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        loadingPointView.getView().setTypeface(loadingPointView.getView().getTypeface(), Typeface.BOLD);

        thirdContent.addView(loadingPointView);

        PDFTextView loadingPointContentView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString loadingPointContentText = new SpannableString(loadingRequests.getChargementName());
        loadingPointContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, loadingPointContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        loadingPointContentView.setText(loadingPointContentText);
        loadingPointContentView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        loadingPointContentView.getView().setTypeface(loadingPointContentView.getView().getTypeface(), Typeface.NORMAL);
        thirdContent.addView(loadingPointContentView);

        pdfBody.addView(thirdContent);


        // data hearder and BORDEREAU DU. name
        PDFHorizontalView fourthContent = new PDFHorizontalView(getApplicationContext());


        PDFTextView bordereuNoView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString bordereuTxt = new SpannableString("BORDEREAU NO.");
        bordereuTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, bordereuTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        bordereuNoView.setText(bordereuTxt);
        bordereuNoView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        bordereuNoView.getView().setTypeface(bordereuNoView.getView().getTypeface(), Typeface.BOLD);
        fourthContent.addView(bordereuNoView);


        PDFTextView bordereuNoContentView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        if(!loadingRequests.getBordereauNo().isEmpty()) {
            SpannableString bordereuNoContentText = new SpannableString(loadingRequests.getBordereauNo());
            bordereuNoContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, bordereuNoContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            bordereuNoContentView.setText(bordereuNoContentText);
        }else{
            SpannableString bordereuNoContentText = new SpannableString(loadingRequests.getEbordereauNo());
            bordereuNoContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, bordereuNoContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            bordereuNoContentView.setText(bordereuNoContentText);
        }

        bordereuNoContentView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        bordereuNoContentView.getView().setTypeface(bordereuNoContentView.getView().getTypeface(), Typeface.NORMAL);

        fourthContent.addView(bordereuNoContentView);



        PDFTextView transporterView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString transporterText = new SpannableString("TRANSPORTEUR");
        transporterText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, transporterText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        transporterView.setText(transporterText);
        transporterView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        transporterView.getView().setTypeface(transporterView.getView().getTypeface(), Typeface.BOLD);

        fourthContent.addView(transporterView);

        PDFTextView borderauDUView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString borderauDUContentText = new SpannableString(loadingRequests.getTranspoterName());
        borderauDUContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, borderauDUContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        borderauDUView.setText(borderauDUContentText);
        borderauDUView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        borderauDUView.getView().setTypeface(borderauDUView.getView().getTypeface(), Typeface.NORMAL);
        fourthContent.addView(borderauDUView);

        pdfBody.addView(fourthContent);


        // data hearder and WAGON NO.
        PDFHorizontalView fifthContent = new PDFHorizontalView(getApplicationContext());

        PDFTextView bordereuDUView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString bordereuDuTxt = new SpannableString("BORDEREAU DU");
        bordereuDuTxt.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, bordereuDuTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        bordereuDUView.setText(bordereuDuTxt);
        bordereuDUView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        bordereuDUView.getView().setTypeface(bordereuDUView.getView().getTypeface(), Typeface.BOLD);
        fifthContent.addView(bordereuDUView);


        PDFTextView bordereuDUContentView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString bordereuDUContentText = new SpannableString(loadingRequests.getCurrentDate());
        bordereuDUContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, bordereuDUContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        bordereuDUContentView.setText(bordereuDUContentText);
        bordereuDUContentView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        bordereuDUContentView.getView().setTypeface(bordereuDUContentView.getView().getTypeface(), Typeface.NORMAL);

        fifthContent.addView(bordereuDUContentView);


        PDFTextView wagonView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString wagonText = new SpannableString("CAMION/WAGON NO.");
        wagonText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, wagonText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wagonView.setText(wagonText);
        wagonView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        wagonView.getView().setTypeface(wagonView.getView().getTypeface(), Typeface.BOLD);

        fifthContent.addView(wagonView);

        PDFTextView wagonContentView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
        SpannableString wagonContentText = new SpannableString(loadingRequests.getWagonNo());
        wagonContentText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, wagonContentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wagonContentView.setText(wagonContentText);
        wagonContentView.setLayout(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        wagonContentView.getView().setTypeface(wagonContentView.getView().getTypeface(), Typeface.NORMAL);
        fifthContent.addView(wagonContentView);

        pdfBody.addView(fifthContent);
        pdfBody.addView(new PDFLineSeparatorView(this).setBackgroundColor(Color.BLACK));



        PDFVerticalView verticaview = new PDFVerticalView(getApplicationContext());
        PDFTextView pdfTextview8 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");
        verticaview.addView(pdfTextview8);
        PDFTextView pdf = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        PDFTextView c = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        PDFTextView d = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        verticaview.addView(pdf);
        verticaview.addView(c);
        verticaview.addView(d);
        pdfBody.addView(verticaview);

        int[] widthPercent = {6,8,8,8,8,8,7,9,8,8,8,7,7};
        //int[] widthPercent = {5, 9, 9, 15, 10, 9, 8, 6, 6, 6, 6, 6}; // Sum should be equal to 100%
        String[] textInTable = {"Sr.No", "AAC", "FOREST", "CERT", "GSEZ NO","ESSENECE","PLAQ NO","LOG NO","D1","D2","DIA","LONG","CBM"};


        PDFTableView.PDFTableRowView tableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());

            for (int i = 0; i < textInTable.length; i++) {
            PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
            SpannableString headerText = new SpannableString(textInTable[i]);
            loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, headerText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            headerView.setText(headerText);
            headerView.setLayout(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 0));
            headerView.getView().setTypeface(headerView.getView().getTypeface(), Typeface.BOLD);
            if(i==11){
                headerView.getView().setGravity(Gravity.CENTER);
            }else if(i==10){
                headerView.getView().setGravity(Gravity.CENTER);
            }else if(i==9){
                headerView.getView().setGravity(Gravity.CENTER);
            }else if(i==8){
                headerView.getView().setGravity(Gravity.CENTER);
            }else if(i==7){
                headerView.getView().setGravity(Gravity.CENTER);
            }else{
                headerView.getView().setGravity(Gravity.CENTER);
            }
            tableHeader.addToHeaderRow(headerView);
        }

        PDFTableView.PDFTableRowView tableRowView1 = new PDFTableView.PDFTableRowView(getApplicationContext());

        String[] textInROW = {"","", "", "", "","","PLAQ NO","LOG NO","D1","D2","DIA","LONG","CBM"};

        for (String s : textInROW) {
            PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            pdfTextView1.setText(s);
            PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
////            SpannableString headerText = new SpannableString(s);
////            loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, headerText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            headerView.setText(s);
//            headerView.setLayout(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
//            headerView.getView().setTypeface(headerView.getView().getTypeface(), Typeface.BOLD);
//            //tableRowView1.addToRow(headerView);
        }

        PDFTableView tableView = new PDFTableView(getApplicationContext(), tableHeader, tableRowView1);
        OkumeCount=0;
        otherCount=0;
        cubantOkumeSum=0;
        othercubantSum=0;
        for (int i = 0; i < 20; i++) {
            // Create 10 rows
            PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (int j=0;j<13;j++) {
                PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);

//                LinearLayout.LayoutParams txtLayoutParam1 = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT, 0);
//
//
//                pdfTextView1.setLayout(txtLayoutParam1);

                if(wagonLogRequests.size()>i) {
                    if (j == 0) {

                        Integer index = i + 1;
                        String zero ="  0";
                        if(i<9){
                            pdfTextView1.setText(zero+index.toString());
                        }else{
                            pdfTextView1.setText(index.toString());
                        }
                        pdfTextView1.getView().setGravity(Gravity.START);
                    } else if (j == 1) {
                        if(wagonLogRequests.get(i).getAACName()!=null) {
                            pdfTextView1.setText(wagonLogRequests.get(i).getAACName() +" - "+wagonLogRequests.get(i).getAACYear());
                        }else{
                            //pdfTextView1.setText("");

                        }
                        pdfTextView1.getView().setGravity(Gravity.START);
                    } else if (j == 2) {
                        if(wagonLogRequests.get(i).getSupplierName()!=null) {
                            pdfTextView1.setText(wagonLogRequests.get(i).getSupplierName());
                        }else{
                            pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.START);
                    } else if (j == 3) {
                        if(wagonLogRequests.get(i).getFSCMode()!=null) {

                            if(!wagonLogRequests.get(i).getFSCMode().equals("NON FSC")) {
                                pdfTextView1.setText(wagonLogRequests.get(i).getFSCMode() + " 100 %");
                            }else{
                                pdfTextView1.setText("");
                            }
                        }else{
                            //pdfTextView1.setText("");

                        }
                        pdfTextView1.getView().setGravity(Gravity.START);
                    } else if (j == 4) {
                        if(wagonLogRequests.get(i).getBarcodeNumber()!=null) {
                            pdfTextView1.setText(wagonLogRequests.get(i).getBarcodeNumber());
                        }else{
                            //pdfTextView1.setText("");
                        }
                        //pdfTextView1.setText(wagonLogRequests.get(i).getBarcodeNumber());

                       /* if(!loadingRequests.getBordereauNo().isEmpty()) {
                            pdfTextView1.setText(loadingRequests.getBordereauNo());
                        }else{
                            pdfTextView1.setText(loadingRequests.getEbordereauNo());
                        }*/
                        pdfTextView1.getView().setGravity(Gravity.START);
                    } else if (j == 5) {

                        if(wagonLogRequests.get(i).getLogSpeciesName()!=null) {
                            pdfTextView1.setText(wagonLogRequests.get(i).getLogSpeciesName());
                        }else{
                            //pdfTextView1.setText("");
                        }

                        pdfTextView1.getView().setGravity(Gravity.CENTER);

                        if(OKOUME.equals(wagonLogRequests.get(i).getLogSpeciesName())){
                            OkumeCount++;
                            cubantOkumeSum+=wagonLogRequests.get(i).getCbm();
                        }else{
                            othercubantSum+=wagonLogRequests.get(i).getCbm();
                            otherCount++;
                        }
                    } else if (j == 6) {

                        if(wagonLogRequests.get(i).getPlaqNo()!=null) {
                            pdfTextView1.setText(wagonLogRequests.get(i).getPlaqNo());
                        }else{
                            //pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    }else if (j == 7) {
                        if(wagonLogRequests.get(i).getLogNo()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getLogNo()));
                        }else{
                            //pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    }  else if (j == 8) {
                        if(wagonLogRequests.get(i).getDiamBdx1()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getDiamBdx1()));
                        }else{
                            //pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    } else if (j == 9) {
                        if(wagonLogRequests.get(i).getDiamBdx2()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getDiamBdx2()));
                        }else{
                            //pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    } else if (j == 10) {
                        if(wagonLogRequests.get(i).getDiamBdx()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getDiamBdx()));
                        }else{
                           // pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    }else if (j == 11) {
                        if(wagonLogRequests.get(i).getLongBdx()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getLongBdx()));
                        }else{
                           // pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    }else if (j == 12) {
                        if(wagonLogRequests.get(i).getLongBdx()!=null) {
                            pdfTextView1.setText(String.valueOf(wagonLogRequests.get(i).getCbm()));
                        }else{
                           // pdfTextView1.setText("");
                        }
                        pdfTextView1.getView().setGravity(Gravity.CENTER);
                    }else if (j == 13) {
                       // pdfTextView1.setText("");
                    }else{
                        //pdfTextView1.setText("");
                    }

                     }else{
                    if (j == 0) {Integer index = i + 1;
                        String zero ="  0";
                        if(i<9){
                            pdfTextView1.setText(zero+index.toString());
                        }else{
                            pdfTextView1.setText(index.toString());
                        }
                        pdfTextView1.getView().setGravity(Gravity.START);
                    }else{
                        //pdfTextView1.setText("");
                    }
                }
                GradientDrawable gd = new GradientDrawable();
                //gd.setColor(0xFF00FF00); // Changes this drawbale to use a single color instead of a gradient
                gd.setStroke(1, 0xFF000000);
                //pdfTextView1.getView().setBackground(gd);
                tableRowView.addToRow(pdfTextView1);
            }

            tableView.addRow(this,tableRowView,true);
        }
        tableView.setColumnWidth(widthPercent);
        pdfBody.addView(tableView);




        PDFVerticalView verticalview = new PDFVerticalView(getApplicationContext());
        PDFTextView pdfTextView48 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");


        PDFTextView pdfTextView78 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        PDFTextView a = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        PDFTextView v = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        verticalview.addView(pdfTextView48);
        verticalview.addView(pdfTextView78);
        verticalview.addView(a);
        verticalview.addView(v);
        pdfBody.addView(verticalview);

        ///This Is For Nature grume
        PDFLineSeparatorView lineSeparatorView4 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView4);

        int[] widthnaturePercent = {25,25,25,25};

        String[] textInNatureTable = {"NATURE", "GRUMES", "CUBANT","TONNES"};
        PDFTableView.PDFTableRowView natureTableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());
        for (String s : textInNatureTable) {
            PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
            SpannableString headerText = new SpannableString(s);
            loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, headerText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            headerView.setText(headerText);
            headerView.setLayout(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            headerView.getView().setTypeface(headerView.getView().getTypeface(), Typeface.BOLD);
            natureTableHeader.addToRow(headerView);
        }
        PDFTableView.PDFTableRowView tableRowViewNature = new PDFTableView.PDFTableRowView(getApplicationContext());

        String[] textInROWNature = {" ", " ", " ", " "," "," "};

        for (String s : textInROWNature) {
            PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            /* pdfTextView1.setText(s);*/
            //tableRowViewNature.addToRow(pdfTextView1);
        }



        totalTonnes=0.6*cubantOkumeSum+0.8*othercubantSum;
        PDFTableView tableViewNature = new PDFTableView(getApplicationContext(), natureTableHeader, tableRowViewNature);
        String[] textInTableOKUME = {"BILLES OKOUME", String.valueOf(OkumeCount), mUtils.convertDoubleToRoundedValue(cubantOkumeSum), mUtils.convertDoubleToRoundedValue(cubantOkumeSum*0.6)};
        String[] textInTableHARDWOOD = {"BILLES HARDWOOD",  String.valueOf(otherCount), mUtils.convertDoubleToRoundedValue(othercubantSum), mUtils.convertDoubleToRoundedValue(othercubantSum*0.8)};
        String[] totalNo = {"Total",  String.valueOf(OkumeCount+otherCount), mUtils.convertDoubleToRoundedValue(cubantOkumeSum+othercubantSum), mUtils.convertDoubleToRoundedValue(totalTonnes)};
        for (int i = 0; i < 3; i++) {
            // Create 3 rows
            PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
            for (int j=0;j<4;j++) {
                if(i==2){
                    PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
                    SpannableString headerText = new SpannableString(totalNo[j]);
                    loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, headerText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    headerView.setText(headerText);
                    headerView.setLayout(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    headerView.getView().setTypeface(headerView.getView().getTypeface(), Typeface.BOLD);

                    tableRowView.addToRow(headerView);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(1, 0xFF000000);
                    //headerView.getView().setBackground(gd);
                }
                else if(i==0){
//                    PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
//                    pdfTextView1.setText(textInTableOKUME[j]);
//                    //pdfTextView1.getView().setGravity(Gravity.CENTER);
//                    tableRowView.addToRow(pdfTextView1);


                    PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
                    headerView.setText(textInTableOKUME[j]);
                    headerView.setLayout(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    tableRowView.addToRow(headerView);
                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(1, 0xFF000000);
                    //headerView.getView().setBackground(gd);
                }else {

                    PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
                    headerView.setText(textInTableHARDWOOD[j]);
                    headerView.setLayout(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    tableRowView.addToRow(headerView);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(1, 0xFF000000);
                    //headerView.getView().setBackground(gd);

                    /*PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                    pdfTextView1.setText(textInTableHARDWOOD[j]);
                    //pdfTextView1.getView().setGravity(Gravity.CENTER);
                    tableRowView.addToRow(pdfTextView1);*/
                }

                GradientDrawable gd = new GradientDrawable();
                gd.setStroke(1, 0xFF000000);
                //tableRowView.getView().setBackground(gd);
            }
            tableViewNature.addRow(tableRowView);
        }


        tableViewNature.setColumnWidth(widthnaturePercent);
        pdfBody.addView(tableViewNature);
        PDFLineSeparatorView lineSeparatorView5 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView5);

        //Observation

        PDFVerticalView verticalView = new PDFVerticalView(getApplicationContext());
        PDFTextView pdfTextView4 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("Observation");
        verticalView.addView(pdfTextView4);
        PDFTextView pdfTextView3 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        verticalView.addView(pdfTextView3);

        PDFTextView pdfTextView5 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        verticalView.addView(pdfTextView5);

        PDFTextView pdfTextView6 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P)
                .setText("");

        verticalView.addView(pdfTextView6);

        pdfBody.addView(verticalView);


        ///This Is For Final Content
        String[] textFinalContentText = {"Le pointeur", "Le chauffeur", "Visa du Receptionnaire","Visa des Eaux et Forets"};
        PDFTableView.PDFTableRowView finalTableHeader = new PDFTableView.PDFTableRowView(getApplicationContext());
        for (String s : textFinalContentText) {
            PDFTextView headerView = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.HEADER);
            SpannableString headerText = new SpannableString(s);
           // loadingPointText.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, headerText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            headerView.setText(headerText);
            headerView.setLayout(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            headerView.getView().setTypeface(headerView.getView().getTypeface(), Typeface.BOLD);
            finalTableHeader.addToRow(headerView);
        }
        PDFTableView.PDFTableRowView tableRowViewFinal = new PDFTableView.PDFTableRowView(getApplicationContext());

        String[] textInROWFinal = {" ", " ", " ", " "," "," "};

        for (String s : textInROWFinal) {
            PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
            /* pdfTextView1.setText(s);*/
            tableRowViewFinal.addToRow(pdfTextView1);
        }

        PDFTableView tableViewFinal = new PDFTableView(getApplicationContext(), finalTableHeader, tableRowViewFinal);

              for (int i=0;i<4;i++) {
                  PDFTableView.PDFTableRowView tableRowView = new PDFTableView.PDFTableRowView(getApplicationContext());
               for (int j=0;j<4;j++) {
                   PDFTextView pdfTextView1 = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.P);
                   tableRowView.addToRow(pdfTextView1);
               }
               tableViewFinal.addRow(tableRowView);
            }

        PDFLineSeparatorView lineSeparatorView6 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView6);
        pdfBody.addView(tableViewFinal);
        PDFLineSeparatorView lineSeparatorView7 = new PDFLineSeparatorView(getApplicationContext()).setBackgroundColor(Color.BLACK);
        pdfBody.addView(lineSeparatorView7);




        return pdfBody;
    }




    @Override
    protected PDFFooterView getFooterView(int pageIndex) {
        PDFFooterView footerView = new PDFFooterView(getApplicationContext());

        PDFTextView pdfTextViewPage = new PDFTextView(getApplicationContext(), PDFTextView.PDF_TEXT_SIZE.SMALL);
        pdfTextViewPage.setText(String.format(Locale.getDefault(), "Page: %d", pageIndex + 1));
        pdfTextViewPage.setLayout(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 0));
        pdfTextViewPage.getView().setGravity(Gravity.CENTER_HORIZONTAL);

        footerView.addView(pdfTextViewPage);

        return footerView;
    }

    @Nullable
    @Override
    protected PDFImageView getWatermarkView(int forPage) {
        PDFImageView pdfImageView = new PDFImageView(getApplicationContext());
/*
        FrameLayout.LayoutParams childLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200, Gravity.CENTER);
        pdfImageView.setLayout(childLayoutParams);

        pdfImageView.setImageResource(R.drawable.ic_pdf);
        pdfImageView.setImageScale(ImageView.ScaleType.FIT_CENTER);
        pdfImageView.getView().setAlpha(0.3F);
*/

        return pdfImageView;
    }

    @Override
    protected void onNextClicked(final File savedPDFFile) {
        Uri pdfUri = Uri.fromFile(savedPDFFile);

        Intent intentPdfViewer = new Intent(PdfCreatorExampleActivity.this, PdfViewerExampleActivity.class);
        intentPdfViewer.putExtra(PdfViewerExampleActivity.PDF_FILE_URI, pdfUri);

        startActivity(intentPdfViewer);
    }
}
