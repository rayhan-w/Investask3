package com.kavindu.farmshare.farmer;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kavindu.farmshare.R;

public class FarmerFarmDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_farmer_farm_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get farm ID from intent
        String farmId = getIntent().getStringExtra("farmId");

        // Set up back button
        ImageView backButton = findViewById(R.id.farmDetailsBackButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Load farm details
        loadFarmDetails(farmId);
    }

    private void loadFarmDetails(String farmId) {
        // TODO: Implement farm details loading from database
        // This will be implemented when we have the database structure
    }
}