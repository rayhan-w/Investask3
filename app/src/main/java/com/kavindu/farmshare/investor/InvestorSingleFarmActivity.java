package com.kavindu.farmshare.investor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.SingleFarmDto;
import com.kavindu.farmshare.util.FirebaseHelper;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class InvestorSingleFarmActivity extends AppCompatActivity {

    private static final int REQUEST_CALL = 1;
    private String farmId;
    private String phoneNumber;
    private FirebaseFirestore firestore;
    private ListenerRegistration farmListener;
    private RecyclerView recyclerView;
    private List<String> imageUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_investor_single_farm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get farm ID from intent
        farmId = getIntent().getStringExtra("id");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.investorSingleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        com.kavindu.farmshare.investor.ImageAdapter imageAdapter = new com.kavindu.farmshare.investor.ImageAdapter(imageUris, this);
        recyclerView.setAdapter(imageAdapter);

        // Show loading message
        ProgressDialog pDialog = new ProgressDialog(InvestorSingleFarmActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();

        // Get farm data from Firestore
        FirebaseHelper.getFarmById(farmId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    SingleFarmDto singleFarmDto = documentSnapshot.toObject(SingleFarmDto.class);
                    if (singleFarmDto != null) {
                        // Set farm data to UI
                        TextView farmNameTextView = findViewById(R.id.textView26);
                        TextView farmTypeTextView = findViewById(R.id.textView27);
                        TextView farmDescriptionTextView = findViewById(R.id.textView28);
                        TextView farmAddressTextView = findViewById(R.id.textView29);
                        TextView farmPhoneTextView = findViewById(R.id.textView30);
                        TextView farmEmailTextView = findViewById(R.id.textView31);
                        TextView farmStockCountTextView = findViewById(R.id.textView32);
                        TextView farmStockPriceTextView = findViewById(R.id.textView33);
                        TextView farmMinStockCountTextView = findViewById(R.id.textView34);

                        farmNameTextView.setText(singleFarmDto.getName());
                        farmTypeTextView.setText(singleFarmDto.getFarmType());
                        farmDescriptionTextView.setText(singleFarmDto.getDescription());
                        farmAddressTextView.setText(singleFarmDto.getAddress());
                        farmPhoneTextView.setText(singleFarmDto.getPhone());
                        farmEmailTextView.setText(singleFarmDto.getEmail());
                        farmStockCountTextView.setText(String.valueOf(singleFarmDto.getStockCount()));
                        farmStockPriceTextView.setText(String.valueOf(singleFarmDto.getStockPrice()));
                        farmMinStockCountTextView.setText(String.valueOf(singleFarmDto.getMinStockCount()));

                        // Set phone number for call button
                        phoneNumber = singleFarmDto.getPhone();

                        // Set image URIs
                        if (singleFarmDto.getImageUris() != null && !singleFarmDto.getImageUris().isEmpty()) {
                            imageUris.clear();
                            imageUris.addAll(singleFarmDto.getImageUris());
                            imageAdapter.notifyDataSetChanged();
                        }

                        // Set OnClickListener for buy button
                        LinearLayout buyButton = findViewById(R.id.linearLayout10);
                        buyButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(InvestorSingleFarmActivity.this, StockBuyActivity.class);
                                intent.putExtra("id", farmId);
                                startActivity(intent);
                            }
                        });

                        // Set OnClickListener for call button
                        LinearLayout callButton = findViewById(R.id.linearLayout11);
                        callButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                makePhoneCall();
                            }
                        });

                        // Set OnClickListener for email button
                        // Using a generic View since we're not sure of the exact type
                        View emailButton = findViewById(R.id.textView83);
                        emailButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendEmail();
                            }
                        });

                        // Set OnClickListener for back button
                        ImageView backButton = findViewById(R.id.imageView5);
                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });

                        // Cancel progress dialog
                        pDialog.cancel();
                    } else {
                        // Show error message
                    pDialog.dismiss();
                    Toast.makeText(InvestorSingleFarmActivity.this, "Farm not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show error message
                    pDialog.cancel();
                    Toast.makeText(InvestorSingleFarmActivity.this, "Failed to load farm data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Show error message
                pDialog.cancel();
                Toast.makeText(InvestorSingleFarmActivity.this, "Failed to load farm data", Toast.LENGTH_LONG).show();
            }
        });

        // Listen for farm changes
        farmListener = firestore.collection("farm")
                .document(farmId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("InvestorSingleFarmActivity", "Listen failed.", error);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            SingleFarmDto singleFarmDto = snapshot.toObject(SingleFarmDto.class);
                            if (singleFarmDto != null) {
                                // Update stock count
                                TextView farmStockCountTextView = findViewById(R.id.textView32);
                                // Update UI with the latest stock count
                                farmStockCountTextView.setText(String.valueOf(singleFarmDto.getStockCount()));
                            }
                        }
                    }
                });
    }

    private void makePhoneCall() {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            if (ContextCompat.checkSelfPermission(InvestorSingleFarmActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InvestorSingleFarmActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + phoneNumber;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(InvestorSingleFarmActivity.this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        TextView farmEmailTextView = findViewById(R.id.textView31);
        String email = farmEmailTextView.getText().toString();
        if (email != null && !email.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about " + farmId);
            startActivity(Intent.createChooser(intent, "Send Email"));
        } else {
            Toast.makeText(InvestorSingleFarmActivity.this, "Email not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (farmListener != null) {
            farmListener.remove();
        }
    }
}