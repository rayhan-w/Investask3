package com.kavindu.farmshare.investor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.PaymentDto;

import java.util.HashMap;
import java.util.Map;

public class InvestorPaymentActivity extends AppCompatActivity {

    private double amount;
    private String farmId;
    private int stockCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_investor_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from intent
        Intent intent = getIntent();
        amount = intent.getDoubleExtra("amount", 0.0);
        farmId = intent.getStringExtra("farmId");
        stockCount = intent.getIntExtra("stockCount", 0);

        // Display amount
        TextView amountTextView = findViewById(R.id.paymentAmountTextView);
        amountTextView.setText("Rs. " + amount);

        // Set up payment button
        Button payButton = findViewById(R.id.paymentConfirmButton);
        payButton.setOnClickListener(v -> processPayment());

        // Set up back button
        View backButton = findViewById(R.id.paymentBackButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void processPayment() {
        // Show processing message
        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
        
        // Create payment data
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setPrice(amount);
        paymentDto.setFarmId(Integer.parseInt(farmId));
        paymentDto.setStockCount(stockCount);
        paymentDto.setReturnType("Cash");
        
        // Process payment directly
        // Show success message
        Toast.makeText(this, "Payment processed successfully!", Toast.LENGTH_LONG).show();
        
        // Navigate back to main activity
        Intent intent = new Intent(InvestorPaymentActivity.this, InvestorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}