package com.kavindu.farmshare.farmer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.kavindu.farmshare.MainActivity;
import com.kavindu.farmshare.NotificationActivity;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.ChartEntruDto;
import com.kavindu.farmshare.dto.FarmerHomeDto;
import com.kavindu.farmshare.dto.NameIdDto;
import com.kavindu.farmshare.dto.StockAllocationTableItemDto;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.model.SQLiteHelper;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import taimoor.sultani.sweetalert2.Sweetalert;


public class FarmerHomeFragment extends Fragment {

    private static class SoilReportDto {
        private String ph;
        private String moisture;
        private String organic;
        private String nutrient;

        public String getPh() { return ph; }
        public void setPh(String ph) { this.ph = ph; }
        public String getMoisture() { return moisture; }
        public void setMoisture(String moisture) { this.moisture = moisture; }
        public String getOrganic() { return organic; }
        public void setOrganic(String organic) { this.organic = organic; }
        public String getNutrient() { return nutrient; }
        public void setNutrient(String nutrient) { this.nutrient = nutrient; }
    }

    ColorfulRingProgressView crpv1;
    ColorfulRingProgressView crpv2;

    TextView riskScoreText;
    TextView riskScoreTitle;
    TextView riskScore;

    int farmId;
    Uri soilReportFileUri;

    ImageView soilReportBtn;

    TextView phText ;
    TextView moistureText;
    TextView organicText;
    TextView nutrientText;

    String startDate = null;
    String endDate = null;

    String farmName = "";

    DatePickerDialog datePickerDialog;

    TextView farmProgressText;
    LinearLayout farmProgressButton;

    private ListenerRegistration investorListener;
    private ListenerRegistration commenListener;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    AtomicBoolean isInitialLoad = new AtomicBoolean(true);
    AtomicBoolean isInitialLoadInvestor = new AtomicBoolean(true);



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_farmer_home, container, false);



        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user",null);

        Gson gson = new Gson();
        UserDto userDto = gson.fromJson(userJson, UserDto.class);



        //firebase snapshot listener for commen
        commenListener = firestore.collection("commen")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FarmShareLog", "Listen failed.", error);
                            return;
                        }

                        firebaseNotification(snapshots,view);

                    }
                });



        //firebase snapshot listener for investor
        investorListener = firestore.collection("farmer")
                .whereEqualTo("userId", String.valueOf(userDto.getId()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FarmShareLog", "Listen failed.", error);
                            return;
                        }

                        firebaseNotificationInvestor(snapshots,view);

                    }
                });


        ImageView notificationButton = view.findViewById(R.id.imageView7);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClick) {

                Intent intent = new Intent(view.getContext(), NotificationActivity.class);
                startActivity(intent);

            }
        });

        crpv1 = (ColorfulRingProgressView) view.findViewById(R.id.crpv);
        crpv1.setPercent(75);

        crpv2 = (ColorfulRingProgressView) view.findViewById(R.id.crpv2);
        crpv2.setPercent(35);

        riskScoreText = view.findViewById(R.id.riskScoreText);
        riskScoreTitle = view.findViewById(R.id.riskScoreTitle);
        riskScore = view.findViewById(R.id.riskScore);
        LineChart lineChart = view.findViewById(R.id.lineChart);

        farmProgressButton = view.findViewById(R.id.farmProgressButton);
        farmProgressText = view.findViewById(R.id.farmProgressText);

// Sample Data
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1990, 60));
        entries.add(new Entry(1994, 30));
        entries.add(new Entry(1998, 90));
        entries.add(new Entry(2002, 60));
        entries.add(new Entry(2006, 100));
        entries.add(new Entry(2010, 70));
        entries.add(new Entry(2014, 30));
        entries.add(new Entry(2018, 80));
        entries.add(new Entry(2022, 120));

// Line DataSet
        LineDataSet dataSet = new LineDataSet(entries, "Stock Price ($)");  // Fill color
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Enable smooth cubic lines
        dataSet.setColor(Color.parseColor("#7AF27B"));
        dataSet.setCircleColor(Color.parseColor("#7AF27B"));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawFilled(true); // Enable filled area
        dataSet.setFillDrawable(ContextCompat.getDrawable(view.getContext(), R.drawable.gradient_farmer_chart)); // Set the gradient
        dataSet.setDrawValues(false);

// Line Data
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh chart

// Customization
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setGranularity(1f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);


        //soil repost document
        soilReportBtn = view.findViewById(R.id.homeSoilReportimageView);

        ActivityResultLauncher<String> soilReportfilePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        soilReportFileUri = uri;
                        Toast.makeText(view.getContext(), "Soil report file Selected: " + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                        soilReportBtn.setImageResource(R.drawable.check);
                    }
                });

        soilReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soilReportfilePickerLauncher.launch("image/*");
            }
        });

        //submit soil report
        Button button = view.findViewById(R.id.buttonSoilSubmit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClick) {
                phText = view.findViewById(R.id.soileditTextText9);
                moistureText = view.findViewById(R.id.soileditTextText10);
                organicText = view.findViewById(R.id.soileditTextText11);
                nutrientText = view.findViewById(R.id.soileditTextText12);

                if (phText.getText().toString().isBlank()){
                    showErrorDialog("Please enter ph level",view);
                }else if (moistureText.getText().toString().isBlank()){
                    showErrorDialog("Please enter moisture level",view);
                }else if (organicText.getText().toString().isBlank()){
                    showErrorDialog("Please enter organic level",view);
                }else if (nutrientText.getText().toString().isBlank()){
                    showErrorDialog("Please enter nutrient level",view);
                }else if (soilReportFileUri == null) {
                    showErrorDialog("Please select soil report document", view);
                } else {
                    SoilReportDto soilReportDto = new SoilReportDto();

                    soilReportDto.setPh(phText.getText().toString());
                    soilReportDto.setMoisture(moistureText.getText().toString());
                    soilReportDto.setOrganic(organicText.getText().toString());
                    soilReportDto.setNutrient(nutrientText.getText().toString());

                    uploadImageToFirebase(soilReportFileUri,view,soilReportDto);
                }

            }
        });


        Button riskReviewButton = view.findViewById(R.id.riskReviewButton);
        riskReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),FarmerRiskReviewActivity.class);
                intent.putExtra("id",farmId);
                view.getContext().startActivity(intent);
            }
        });

        //stock relese
        Button stockReleseButton = view.findViewById(R.id.stockReleaseButton);
        EditText stockReleseEditText = view.findViewById(R.id.editTextNumber2);
        stockReleseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClick) {

                if (!stockReleseEditText.getText().toString().isBlank()){

                    Sweetalert pDialog2 = new Sweetalert(view.getContext(), Sweetalert.PROGRESS_TYPE);
                    pDialog2.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog2.setTitleText("Processing");
                    pDialog2.setCancelable(false);
                    pDialog2.show();

                    DocumentReference farmRef = firestore.collection("farms").document(String.valueOf(farmId));
                    farmRef.update("releasedStock", stockReleseEditText.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    new Sweetalert(view.getContext(), Sweetalert.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Stock released")
                                            .show();
                                    stockReleseEditText.setText("");
                                    loadFarmData(farmId, view);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                            .setTitleText("Error")
                                            .setContentText("Failed to release stock")
                                            .show();
                                }
                            });



                }else{
                    showErrorDialog("Enter stock amount",view);
                }


            }
        });




        //load data
        Sweetalert pDialog = new Sweetalert(view.getContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing");
        pDialog.setCancelable(false);
        pDialog.show();

        SharedPreferences sp = getActivity().getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
        String user = sp.getString("user",null);

        if (user != null) {
            // Change variable name to avoid redefinition
            UserDto parsedUserDto = new Gson().fromJson(user, UserDto.class);
            db.collection("farms").whereEqualTo("farmerId", parsedUserDto.getId()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                pDialog.cancel();
                                new Sweetalert(view.getContext(), Sweetalert.WARNING_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Add new farm")
                                        .show();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                Intent intent = new Intent(view.getContext(), FarmerAddFarmActivity.class);
                                startActivity(intent);
                            } else {
                                ArrayList<NameIdDto> chipArrayList = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    chipArrayList.add(new NameIdDto(document.getString("farmName"), Integer.parseInt(document.getId())));
                                }

                                ChipGroup chipGroup = view.findViewById(R.id.homeChipgroup);
                                int heightInPixels = (int) TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP, 55, view.getContext().getResources().getDisplayMetrics());

                                for (int i = 0; i < chipArrayList.size(); i++) {
                                    NameIdDto nameIdDto = chipArrayList.get(i);
                                    Chip chip = new Chip(view.getContext());
                                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT, heightInPixels);
                                    chip.setLayoutParams(params);
                                    chip.setCheckable(true);
                                    chip.setChecked(i == 0);
                                    chip.setText(nameIdDto.getName());
                                    ColorStateList textColor = ContextCompat.getColorStateList(view.getContext(), R.color.chip_text_color);
                                    chip.setTextColor(textColor);
                                    chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                    ColorStateList chipBackgroundColor = ContextCompat.getColorStateList(view.getContext(), R.color.chip_selector);
                                    chip.setChipBackgroundColor(chipBackgroundColor);
                                    chip.setChipStrokeWidth(0);
                                    chip.setOnClickListener(clickCiew -> loadFarmData(nameIdDto.getId(), view));
                                    chipGroup.addView(chip);
                                }
                                chipGroup.invalidate();
                                if (!chipArrayList.isEmpty()) {
                                    loadFarmData(chipArrayList.get(0).getId(), view);
                                }
                                pDialog.cancel();
                            }
                        } else {
                            pDialog.cancel();
                            new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText("Something went wrong!")
                                    .show();
                        }
                    });
        }



        return view;
    }

    private void loadFarmData(int id, View parent) {
        Sweetalert pDialog = new Sweetalert(requireContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading farm data");
        pDialog.setCancelable(false);
        pDialog.show();

        if (!com.kavindu.farmshare.utils.NetworkUtils.isNetworkConnected(getContext())) {
            pDialog.dismissWithAnimation();
            Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("farms").document(String.valueOf(id))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    pDialog.dismissWithAnimation();
                    if (documentSnapshot.exists()) {
                        // Assuming FarmerHomeDto can be directly mapped from the Firestore document
                        FarmerHomeDto farmerHomeDto = documentSnapshot.toObject(FarmerHomeDto.class);
                        if (farmerHomeDto != null) {
                            updateData(farmerHomeDto, parent);
                        }
                    } else {
                        Toast.makeText(getContext(), "Farm not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    pDialog.dismissWithAnimation();
                    Toast.makeText(getContext(), "Error loading farm data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FarmerHomeFragment", "Error loading farm data", e);
                });
    }

    private void updateData(FarmerHomeDto farmerHomeDto,View parent){

        farmName = farmerHomeDto.getFarmName();

        int percentage = (int) ((farmerHomeDto.getRiskScore() * 100) / 200);

        if (farmerHomeDto.getRiskScore() > 120){
            crpv2.setVisibility(View.INVISIBLE);
            crpv1.setVisibility(View.VISIBLE);
            riskScore.setText(String.valueOf(farmerHomeDto.getRiskScore()));
            riskScoreTitle.setText(R.string.test_Risk_Score_text_risk);
            riskScoreText.setText("Risk");
            riskScoreText.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.red));
            crpv1.setPercent(Math.min(percentage, 100));

        }else {
            crpv1.setVisibility(View.INVISIBLE);
            crpv2.setVisibility(View.VISIBLE);
            riskScore.setText(String.valueOf(farmerHomeDto.getRiskScore()));
            riskScoreTitle.setText(R.string.test_Risk_Score_text_good);
            riskScoreText.setText("good hands");
            riskScoreText.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.green));
            crpv2.setPercent(Math.min(percentage, 100));

        }

        TextView totStockTxt = parent.findViewById(R.id.textView19);
        TextView relesedStockTxt = parent.findViewById(R.id.textView21);
        TextView expIncomeTxt = parent.findViewById(R.id.textView23);
        TextView stockProgressTxt = parent.findViewById(R.id.textView26);
        TextView dateTxt = parent.findViewById(R.id.textView24);
        TextView farmNameTxt = parent.findViewById(R.id.textView14);
        TextView totStockSr = parent.findViewById(R.id.textView37);
        TextView singleStockTxt = parent.findViewById(R.id.textView144);
        TextView releasedStockSr = parent.findViewById(R.id.textView139);
        View stockProgressBackBar = parent.findViewById(R.id.view2);
        View stockProgressFrontBar = parent.findViewById(R.id.view3);

        String totStock = "S "+String.valueOf(farmerHomeDto.getTotStock());
        String relesedStock = "S "+String.valueOf(farmerHomeDto.getRelesedStock());
        String expIncome = "Rs. "+ new DecimalFormat("#,###").format(farmerHomeDto.getExpectIncome())+" .00";
        String singleStickPrice = "Rs. "+farmerHomeDto.getSingleStockPrice();
        String stockProgressStock = "S "+String.valueOf(farmerHomeDto.getStockProgress());
        String today = new SimpleDateFormat("d  MMMM  yyyy", Locale.getDefault()).format(new Date());

        totStockTxt.setText(totStock);
        relesedStockTxt.setText(relesedStock);
        expIncomeTxt.setText(expIncome);
        stockProgressTxt.setText(stockProgressStock);
        dateTxt.setText(today);
        farmNameTxt.setText(farmerHomeDto.getFarmName());
        totStockSr.setText(totStock);
        releasedStockSr.setText(relesedStock);
        farmProgressText.setText(farmerHomeDto.getFarmStatus());
        singleStockTxt.setText(singleStickPrice);

        if (farmerHomeDto.isPriceDrop()){
            singleStockTxt.setTextColor(ContextCompat.getColor(parent.getContext(),R.color.red));
        }else {
            singleStockTxt.setTextColor(ContextCompat.getColor(parent.getContext(),R.color.green));
        }

        Log.i("FarmShareLog",String.valueOf(farmerHomeDto.getRelesedStock()));

        if (farmerHomeDto.getRelesedStock() > 0 && farmerHomeDto.getStockProgress() > 0){
            int maxValue = farmerHomeDto.getRelesedStock();
            int currentValue = farmerHomeDto.getStockProgress() ;
            int vBackWidth = stockProgressBackBar.getWidth();
            float viewPercentage = (float) currentValue / maxValue;
            int vFrontWidth = (int) (viewPercentage * vBackWidth);
            stockProgressFrontBar.getLayoutParams().width = vFrontWidth;
            stockProgressFrontBar.requestLayout();
        }else{
            stockProgressFrontBar.getLayoutParams().width = 30;
            stockProgressFrontBar.requestLayout();
        }



        //load chart
        List<Entry> entries = new ArrayList<>();

        int ci = 1;
        for (ChartEntruDto chartEntruDto : farmerHomeDto.getChartEntryList()){
            entries.add(new Entry(ci, (float) chartEntruDto.getValue()));
            ci++;
        }

        loadChart(parent,entries, farmerHomeDto.isPriceDrop());

        LinearLayout tableConatiner = parent.findViewById(R.id.investorTableDataContainer);
        TextView investorCount = parent.findViewById(R.id.textView28);

        investorCount.setText(farmerHomeDto.getInvestorsCount());

        farmId = farmerHomeDto.getFarmId();

        for (int i = tableConatiner.getChildCount() - 1; i >= 0; i--) {
            View child = tableConatiner.getChildAt(i);
            if (child.getId() != R.id.headerRow) {
                tableConatiner.removeViewAt(i);
            }
        }

        for (StockAllocationTableItemDto tableData : farmerHomeDto.getTableItemList()){

            View tableItem = getLayoutInflater().inflate(R.layout.fragment_farmer_home_investor_table_item,null);

            ImageView image = tableItem.findViewById(R.id.imageView12);
            TextView name = tableItem.findViewById(R.id.textView32);
            TextView stock = tableItem.findViewById(R.id.textView33);
            TextView amount = tableItem.findViewById(R.id.textView34);

            image.setImageResource(R.drawable.globe);
            name.setText(tableData.getName());
            stock.setText(tableData.getStock());
            amount.setText(tableData.getAmount());

            tableConatiner.addView(tableItem);

        }

        //load status button

        String progress = farmerHomeDto.getFarmStatus();


        if(progress.equals("Start")){
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_start));
            farmProgressButton.setOnClickListener(null);
            farmProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewClick) {

                    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                    View startDialog = layoutInflater.inflate(R.layout.farm_status_alert_dialog,null,false);

                    ImageView imageView = startDialog.findViewById(R.id.imageView13);
                    ImageView imageView2 = startDialog.findViewById(R.id.imageView14);
                    TextView selectedDateStart = startDialog.findViewById(R.id.textView141);
                    TextView selectedDateEnd = startDialog.findViewById(R.id.textView142);
                    LinearLayout startDialogButton = startDialog.findViewById(R.id.startDialogButton);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View viewClick2) {
                            // Get the current date
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            // Create and show DatePickerDialog
                            DatePickerDialog datePickerDialog = new DatePickerDialog(parent.getContext(),
                                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                                        // Show the selected date (Month is zero-based, so add 1)
                                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                        selectedDateStart.setText("Start Date: " + selectedDate);
                                        startDate = selectedDate;
                                    }, year, month, day);

                            datePickerDialog.show();
                        }
                    });

                    imageView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View viewClick2) {
                            // Get the current date
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            // Create and show DatePickerDialog
                            DatePickerDialog datePickerDialog = new DatePickerDialog(parent.getContext(),
                                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                                        // Show the selected date (Month is zero-based, so add 1)
                                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                        selectedDateEnd.setText("End Date: " + selectedDate);
                                        endDate = selectedDate;
                                    }, year, month, day);

                            datePickerDialog.show();
                        }
                    });

                    startDialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View viewClick) {
                            Sweetalert pDialog = new Sweetalert(parent.getContext(), Sweetalert.PROGRESS_TYPE);
                            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                            pDialog.setTitleText("Updating...");
                            pDialog.setCancelable(false);
                            pDialog.show();

                            if (startDate == null) {
                                pDialog.cancel();
                                showErrorDialog("Please select start date", parent);
                            } else if (endDate == null) {
                                pDialog.cancel();
                                showErrorDialog("Please select end date", parent);
                            } else {
                                farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.gradient_cultivating));
                                farmProgressText.setText("Cultivating");

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("startDate", startDate);
                                updates.put("endDate", endDate);
                                updates.put("status", "Cultivating");

                                firestore.collection("farms").document(String.valueOf(farmId))
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            loadFarmData(farmId, parent);
                                            pDialog.cancel();
                                            new Sweetalert(parent.getContext(), Sweetalert.SUCCESS_TYPE)
                                                    .setTitleText("Success")
                                                    .setContentText("Farm season started successfully")
                                                    .show();

                                            selectedDateStart.setText("Start date");
                                            selectedDateEnd.setText("End date");
                                            startDate = null;
                                            endDate = null;
                                        })
                                        .addOnFailureListener(e -> {
                                            pDialog.cancel();
                                            showErrorDialog("Failed to start farm season. Please try again.", parent);
                                            Log.e("FarmerHomeFragment", "Error starting farm season", e);
                                        });
                            }
                        }
                    });

                    new AlertDialog.Builder(parent.getContext()).setView(startDialog).show();
                }
            });
        }

        if(progress.equals("Cultivating")){
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_cultivating));
            farmProgressButton.setOnClickListener(null);
            farmProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewClick) {
                    updateFarmStatus("Planting",parent);
                    farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_planting));
                    farmProgressText.setText("Planting");
                }
            });



        } else if (progress.equals("Planting")) {
            Log.i("FarmShareLog",progress);
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_planting));

            farmProgressButton.setOnClickListener(null);
            farmProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewClick) {
                    updateFarmStatus("Growing",parent);
                    farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_growing));
                    farmProgressText.setText("Growing");
                }
            });



        } else  if (progress.equals("Growing")) {
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_growing));

            farmProgressButton.setOnClickListener(null);
            farmProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewClick) {
                    updateFarmStatus("Harvesting",parent);
                    farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_harvesting));
                    farmProgressText.setText("Harvesting");
                }
            });



        } else if (progress.equals("Harvesting")) {
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_harvesting));

            farmProgressButton.setOnClickListener(null);
            farmProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View viewClick) {

                    Intent intent =  new Intent(parent.getContext(), FarmerPaymentActivity.class);
                    intent.putExtra("id",farmId);
                    startActivity(intent);

//                    updateFarmStatus("Completed",parent);
//                    farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_compleat));
//                    farmProgressText.setText("Completed");

                }
            });


        } else if (progress.equals("Completed")) {
            farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_compleat));

            farmProgressButton.setOnClickListener(null);
//            farmProgressButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View viewClick) {
//                    updateFarmStatus("Start",parent);
//                    farmProgressButton.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.gradient_start));
//                    farmProgressText.setText("Start");
//
//                }
//            });


        }

    }

    private void loadChart(View parent,List<Entry> entries , boolean isPriceDrop){
        LineChart lineChart = parent.findViewById(R.id.lineChart);


// Line DataSet
        LineDataSet dataSet = new LineDataSet(entries, "Stock Price ($)");  // Fill color
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Enable smooth cubic lines
        dataSet.setColor(Color.parseColor("#7AF27B"));
        dataSet.setCircleColor(Color.parseColor("#7AF27B"));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawFilled(true); // Enable filled area
        dataSet.setFillDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.gradient_farmer_chart)); // Set the gradient
        dataSet.setDrawValues(false);

        if(isPriceDrop){
            dataSet.setColor(Color.parseColor("#f27a7a"));
            dataSet.setCircleColor(Color.parseColor("#f27a7a"));
            dataSet.setFillDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.gradient_farmer_chart_red));
        }

// Line Data
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh chart

// Customization
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setGranularity(1f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);


    }

    private void uploadImageToFirebase(Uri soilReportFileUri, View parent, SoilReportDto soilReportDto) {
        if (soilReportFileUri == null) {
            showErrorDialog("Please select a soil report file", parent);
            return;
        }

        Sweetalert pDialog = new Sweetalert(parent.getContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Uploading...");
        pDialog.setCancelable(false);
        pDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("soil_reports/" + farmId + "_" + System.currentTimeMillis() + ".pdf");

        fileRef.putFile(soilReportFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        Map<String, Object> soilReportData = new HashMap<>();
                        soilReportData.put("ph", soilReportDto.getPh());
                        soilReportData.put("moisture", soilReportDto.getMoisture());
                        soilReportData.put("organic", soilReportDto.getOrganic());
                        soilReportData.put("nutrient", soilReportDto.getNutrient());
                        soilReportData.put("documentUrl", imageUrl);
                        soilReportData.put("updatedAt", FieldValue.serverTimestamp());

                        firestore.collection("farms").document(String.valueOf(farmId))
                                .update("soilReport", soilReportData)
                                .addOnSuccessListener(aVoid -> {
                                    pDialog.cancel();
                                    new Sweetalert(parent.getContext(), Sweetalert.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Soil report updated successfully")
                                            .show();

                                    phText.setText("");
                                    nutrientText.setText("");
                                    moistureText.setText("");
                                    organicText.setText("");
                                    this.soilReportFileUri = null;
                                    soilReportBtn.setImageResource(R.drawable.addfolder);
                                })
                                .addOnFailureListener(e -> {
                                    pDialog.cancel();
                                    new Sweetalert(parent.getContext(), Sweetalert.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Failed to update soil report. Please try again.")
                                            .show();
                                    Log.e("FarmerHomeFragment", "Error updating soil report", e);
                                });
                    }).addOnFailureListener(e -> {
                        pDialog.cancel();
                        Log.e("Firebase", "Failed to get download URL", e);
                        showErrorDialog("Failed to get download URL. Please try again.", parent);
                    });
                }).addOnFailureListener(e -> {
                    pDialog.cancel();
                    Log.e("Firebase", "Upload failed", e);
                    showErrorDialog("Failed to upload file. Please try again.", parent);
                });
    }

    private void showErrorDialog(String message, View parent) {
        new Sweetalert(parent.getContext(), Sweetalert.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(message)
                .show();
    }

    private void updateFarmStatus(String status, View parent) {
        Sweetalert pDialog = new Sweetalert(parent.getContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Updating...");
        pDialog.setCancelable(false);
        pDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        firestore.collection("farms").document(String.valueOf(farmId))
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    loadFarmData(farmId, parent);

                    String text = "Exciting news! farm " + farmName + " has moved to the " + status + " stage. Stay tuned for progress updates!";

                    // Create notification in commen collection
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("title", "Farm Status Updated!");
                    notificationData.put("text", text);
                    notificationData.put("createdAt", FieldValue.serverTimestamp());

                    firestore.collection("commen").add(notificationData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("FarmerHomeFragment", "Notification added to commen collection");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FarmerHomeFragment", "Error adding notification to commen collection", e);
                            });

                    // Create notification in notifications collection
                    Map<String, Object> farmNotification = new HashMap<>();
                    farmNotification.put("farmId", farmId);
                    farmNotification.put("farmName", farmName);
                    farmNotification.put("status", status);
                    farmNotification.put("createdAt", FieldValue.serverTimestamp());
                    farmNotification.put("type", "status");

                    firestore.collection("notifications")
                            .add(farmNotification)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("FarmerHomeFragment", "Notification created successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FarmerHomeFragment", "Error creating notification", e);
                            });

                    pDialog.cancel();
                    new Sweetalert(parent.getContext(), Sweetalert.SUCCESS_TYPE)
                            .setTitleText("Success")
                            .setContentText("Farm status updated successfully")
                            .show();
                })
                .addOnFailureListener(e -> {
                    pDialog.cancel();
                    showErrorDialog("Failed to update farm status. Please try again.", parent);
                    Log.e("FarmerHomeFragment", "Error updating farm status", e);
                });
    }

    private void firebaseNotification(QuerySnapshot snapshots, View parent) {
        if (snapshots != null) {
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if (isInitialLoad.get()) {
                    continue;
                }

                if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                    DocumentSnapshot document = dc.getDocument();
                    String title = document.getString("title");
                    String text = document.getString("text");

                    SQLiteHelper sqLiteHelper = new SQLiteHelper(parent.getContext(), "farmShareFarmer.db", null, 1);

                    new Thread(() -> {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("title", title);
                        contentValues.put("text", text);

                        long id = sqLiteDatabase.insert("notification", null, contentValues);
                        Log.i("FarmShareLog", "Sqlite id : " + String.valueOf(id));

                        sqLiteDatabase.close();
                    }).start();

                    //notification
                    NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);

                    if (notificationManager == null) {
                        Log.e("NotificationError", "NotificationManager is null");
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(
                                "C1",
                                "Channel1",
                                NotificationManager.IMPORTANCE_HIGH
                        );
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    Notification notification = new NotificationCompat.Builder(requireContext(), "C1")
                            .setContentTitle(title)
                            .setContentText(text)
                            .setSmallIcon(R.drawable.newlogo2)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify((int) System.currentTimeMillis(), notification);
                }
            }
            isInitialLoad.set(false);
        }
    }

    private void firebaseNotificationInvestor(QuerySnapshot snapshots, View parent) {
        if (snapshots != null) {
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if (isInitialLoadInvestor.get()) {
                    continue;
                }

                if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                    DocumentSnapshot document = dc.getDocument();
                    String title = document.getString("title");
                    String text = document.getString("text");

                    SQLiteHelper sqLiteHelper = new SQLiteHelper(parent.getContext(), "farmShareFarmer.db", null, 1);

                    new Thread(() -> {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("title", title);
                        contentValues.put("text", text);

                        long id = sqLiteDatabase.insert("notification", null, contentValues);
                        Log.i("FarmShareLog", "Sqlite id : " + String.valueOf(id));

                        sqLiteDatabase.close();
                    }).start();

                    //notification
                    NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);

                    if (notificationManager == null) {
                        Log.e("NotificationError", "NotificationManager is null");
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(
                                "C1",
                                "Channel1",
                                NotificationManager.IMPORTANCE_HIGH
                        );
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    Notification notification = new NotificationCompat.Builder(requireContext(), "C1")
                            .setContentTitle(title)
                            .setContentText(text)
                            .setSmallIcon(R.drawable.newlogo2)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify((int) System.currentTimeMillis(), notification);
                }
            }
            isInitialLoadInvestor.set(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (investorListener != null) {
            investorListener.remove();
        }
        if (commenListener != null) {
            commenListener.remove();
        }
    }

}