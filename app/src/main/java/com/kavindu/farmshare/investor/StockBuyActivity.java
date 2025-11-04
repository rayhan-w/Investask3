package com.kavindu.farmshare.investor;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;
import com.bumptech.glide.Glide;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.PaymentDto;
import com.kavindu.farmshare.util.FirebaseHelper;
import android.app.ProgressDialog;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StockBuyActivity extends AppCompatActivity {

    private String farmId;
    private FirebaseFirestore firestore;
    private ListenerRegistration farmListener;
    private PaymentDto paymentDto;
    private EditText stockCountEditText;
    private TextView availableStockTextView;
    private TextView minStockTextView;
    private TextView totalPriceTextView;
    private Button buyButton;
    private ImageView backButton;
    private Map<String, Object> farmDto;
    private Map<String, Object> investorDto;
    private int availableStock = 0;
    private int minStock = 0;
    private double stockPrice = 0.0;

    private ActivityResultLauncher<Intent> payHareLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Payment successful
                    Intent data = result.getData();
                    if (data != null) {
                        String paymentId = data.getStringExtra("paymentId");
                        
                        // Show progress dialog
                        ProgressDialog pDialog = new ProgressDialog(StockBuyActivity.this);
                        pDialog.setMessage("Processing...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                        
                        // Update farm stock count
                        int stockCount = Integer.parseInt(stockCountEditText.getText().toString());
                        int newStockCount = availableStock - stockCount;
                        
                        // Update farm document in Firestore
                        firestore.collection("farm")
                                .document(farmId)
                                .update("stockCount", newStockCount)
                                .addOnSuccessListener(aVoid -> {
                                    // Create investment document
                                    Map<String, Object> investment = new HashMap<>();
                                    investment.put("farmId", farmId);
                                    investment.put("investorId", investorDto.get("id"));
                                    investment.put("stockCount", stockCount);
                                    investment.put("stockPrice", stockPrice);
                                    investment.put("totalPrice", stockCount * stockPrice);
                                    investment.put("paymentId", paymentId);
                                    investment.put("timestamp", new Date());
                                    
                                    // Add investment to Firestore
                                    firestore.collection("investments")
                                            .add(investment)
                                            .addOnSuccessListener(documentReference -> {
                                                // Create notification for investor
                                                Map<String, Object> notification = new HashMap<>();
                                                notification.put("title", "Stock Purchase Successful");
                                                notification.put("message", "You have successfully purchased " + stockCount + " stocks of " + farmDto.get("farmName"));
                                                notification.put("timestamp", new Date());
                                                notification.put("read", false);
                                                notification.put("userId", investorDto.get("id"));
                                                
                                                // Add notification to Firestore
                                                firestore.collection("notifications")
                                                        .add(notification)
                                                        .addOnSuccessListener(notificationRef -> {
                                                            // Show success message
                                                            pDialog.dismiss();
                                                            Toast.makeText(StockBuyActivity.this, "Stock purchase successful", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                            
                                                            // Show notification
                                                            showNotification("Stock Purchase Successful", "You have successfully purchased " + stockCount + " stocks of " + farmDto.get("farmName"));
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Show error message
                                                            pDialog.dismiss();
                                                            Toast.makeText(StockBuyActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                // Show error message
                                                pDialog.cancel();
                                                Toast.makeText(StockBuyActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Show error message
                                    pDialog.cancel();
                                    Toast.makeText(StockBuyActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    // Payment canceled
                    Toast.makeText(StockBuyActivity.this, "Payment canceled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_buy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get farm ID from intent
        farmId = getIntent().getStringExtra("id");

        // Initialize UI elements
        stockCountEditText = findViewById(R.id.editTextNumber);
        availableStockTextView = findViewById(R.id.textView39);
        minStockTextView = findViewById(R.id.textView40);
        totalPriceTextView = findViewById(R.id.textView41);
        buyButton = findViewById(R.id.investorStockBuyButton);
        backButton = findViewById(R.id.stockBuyBackButton);

        // Set OnClickListener for back button
        backButton.setOnClickListener(view -> finish());

        // Show progress dialog
        ProgressDialog pDialog = new ProgressDialog(StockBuyActivity.this);
        pDialog.setMessage("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        // Get investor data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("investor_prefs", Context.MODE_PRIVATE);
        String investorJson = sharedPreferences.getString("investor", null);
        if (investorJson != null) {
            investorDto = new Gson().fromJson(investorJson, Map.class);
        }

        // Get farm data from Firestore
        FirebaseHelper.getFarmById(farmId, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    farmDto = documentSnapshot.getData();
                    if (farmDto != null) {
                        // Set farm data to UI
                        availableStock = ((Long) farmDto.get("stockCount")).intValue();
                        minStock = ((Long) farmDto.get("minStockCount")).intValue();
                        stockPrice = ((Double) farmDto.get("stockPrice"));

                        availableStockTextView.setText(String.valueOf(availableStock));
                        minStockTextView.setText(String.valueOf(minStock));

                        // Set TextWatcher for stock count EditText
                        stockCountEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                updateTotalPrice();
                            }
                        });

                        // Set OnClickListener for buy button
                        buyButton.setOnClickListener(view -> {
                            if (validateInput()) {
                                int stockCount = Integer.parseInt(stockCountEditText.getText().toString());
                                double totalPrice = stockCount * stockPrice;

                                // Create payment object
                                paymentDto = new PaymentDto();
                                paymentDto.setPrice(totalPrice);
                                paymentDto.setFarmId(Integer.parseInt(farmId));
                                paymentDto.setStockCount(stockCount);
                                paymentDto.setReturnType("Cash");

                                // Start payment activity
                                Intent intent = new Intent(StockBuyActivity.this, InvestorPaymentActivity.class);
                                intent.putExtra("amount", totalPrice);
                                intent.putExtra("farmId", farmId);
                                intent.putExtra("stockCount", stockCount);
                                payHareLauncher.launch(intent);
                            }
                        });

                        // Cancel progress dialog
                        pDialog.cancel();
                    } else {
                        // Show error message
                        runOnUiThread(() -> {
                            pDialog.cancel();
                            Toast.makeText(StockBuyActivity.this, "Farm not found", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    // Show error message
                    runOnUiThread(() -> {
                        pDialog.cancel();
                        Toast.makeText(StockBuyActivity.this, "Farm not found", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                runOnUiThread(() -> {
                    pDialog.cancel();
                    Toast.makeText(StockBuyActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "stock_purchase_channel";
        String channelName = "Stock Purchase";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private boolean validateInput() {
        String stockCountStr = stockCountEditText.getText().toString();
        if (stockCountStr.isEmpty()) {
            Toast.makeText(this, "Please enter stock count", Toast.LENGTH_SHORT).show();
            return false;
        }

        int stockCount = Integer.parseInt(stockCountStr);
        if (stockCount < minStock) {
            Toast.makeText(this, "Stock count must be at least " + minStock, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (stockCount > availableStock) {
            Toast.makeText(this, "Stock count cannot exceed available stock", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateTotalPrice() {
        String stockCountStr = stockCountEditText.getText().toString();
        if (!stockCountStr.isEmpty()) {
            int stockCount = Integer.parseInt(stockCountStr);
            double totalPrice = stockCount * stockPrice;
            totalPriceTextView.setText(String.format(Locale.getDefault(), "%.2f", totalPrice));
        } else {
            totalPriceTextView.setText("0.00");
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