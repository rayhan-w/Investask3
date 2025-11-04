package com.kavindu.farmshare.farmer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kavindu.farmshare.BuildConfig;
import com.kavindu.farmshare.MainActivity;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.ImageDto;
import com.kavindu.farmshare.dto.ResponseDto;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.investor.InvestorMainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import taimoor.sultani.sweetalert2.Sweetalert;

public class FarmerAddFarmActivity extends AppCompatActivity {

    private GoogleMap map;

    private String nicFrontUrl;
    private String nicBackUrl;
    private String ownershipDocUrl;
    private String analysisDocUrl;
    private String soilReportDocUrl;
    private List<String> farmImageUrls;

    private LatLng farmLocatin;
    private String farmType;
    private String farmName;
    private double farmSize;
    private int farmAvgIncome;
    private int farmMinInvest;
    private String farmCodeName;
    private String farmDescription;
    private String farmNicNumber;
    private double soilPh;
    private double soilMoisture;
    private double soilOrganicMatter;
    private double soilNutrient;

    private EditText farmNameEditText;
    private EditText farmSizeEditText;
    private EditText farmAvgEditText;
    private EditText farmMinInvestEditText;
    private EditText farmCodeNameEditText;
    private EditText farmDescriptionEditText;
    private EditText farmNicNumberEditText;
    private EditText farmSoilPhEditText;
    private EditText farmSoilMoisturEditText;
    private EditText farmSoilOrganicEditText;
    private EditText farmSoilNutrientEditText;
    private EditText nicFrontUrlEditText;
    private EditText nicBackUrlEditText;
    private EditText ownershipDocUrlEditText;
    private EditText analysisDocUrlEditText;
    private EditText soilReportUrlEditText;
    private EditText farmImageUrlEditText;
    private TextView farmImageUrlsTextView;
    private Button addFarmImageUrlButton;
    private Spinner farmTypeSpinner;

    private Sweetalert pDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_farmer_add_farm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupCropSpinner();
        setupGoogleMap();
        setupLocationButton();
        setupImageUrlHandling();
        setupSubmitButton();
    }

    private void initializeViews() {
        farmTypeSpinner = findViewById(R.id.farmTypeSpinner);
        farmNameEditText = findViewById(R.id.farmNameEditText);
        farmSizeEditText = findViewById(R.id.farmSizeEditText);
        farmAvgEditText = findViewById(R.id.farmAvgEditText);
        farmMinInvestEditText = findViewById(R.id.farmMinInvestEditText);
        farmCodeNameEditText = findViewById(R.id.farmCodeNameEditText);
        farmDescriptionEditText = findViewById(R.id.farmDescriptionEditText);
        farmNicNumberEditText = findViewById(R.id.farmNicNumberEditText);
        farmSoilPhEditText = findViewById(R.id.farmSoilPhEditText);
        farmSoilMoisturEditText = findViewById(R.id.farmSoilMoisturEditText);
        farmSoilOrganicEditText = findViewById(R.id.farmSoilOrganicEditText);
        farmSoilNutrientEditText = findViewById(R.id.farmSoilNutrientEditText);
        nicFrontUrlEditText = findViewById(R.id.nicFrontUrlEditText);
        nicBackUrlEditText = findViewById(R.id.nicBackUrlEditText);
        ownershipDocUrlEditText = findViewById(R.id.ownershipDocUrlEditText);
        analysisDocUrlEditText = findViewById(R.id.analysisDocUrlEditText);
        soilReportUrlEditText = findViewById(R.id.soilReportUrlEditText);
        farmImageUrlEditText = findViewById(R.id.farmImageUrlEditText);
        farmImageUrlsTextView = findViewById(R.id.farmImageUrlsTextView);
        addFarmImageUrlButton = findViewById(R.id.addFarmImageUrlButton);

        farmImageUrls = new ArrayList<>();
    }

    private void setupCropSpinner() {
        ArrayList<String> cropArrayList = new ArrayList<>();
        cropArrayList.add("Select");
        cropArrayList.add("Owner");
        cropArrayList.add("Partnership");

        CropAdapter cropAdapter = new CropAdapter(
                this,
                R.layout.fragment_custom_spinner_item,
                cropArrayList,
                R.layout.fragment_cuntom_spinner_selected
        );

        farmTypeSpinner.setAdapter(cropAdapter);
    }

    private void setupGoogleMap() {
        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.mapContainer, supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setCompassEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    
                    // Get current location and move camera there
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(FarmerAddFarmActivity.this);
                    fusedLocationClient.getLastLocation().addOnSuccessListener(FarmerAddFarmActivity.this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        } else {
                            // Fallback to default location if current location not available
                            LatLng defaultLocation = new LatLng(6.9271, 79.8612);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
                        }
                    });
                } else {
                    String[] permissionArray = new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    };
                    requestPermissions(permissionArray, 100);
                    
                    // Set default location when no permissions
                    LatLng defaultLocation = new LatLng(6.9271, 79.8612);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
                }

                // Add marker at center when map is moved
                googleMap.setOnCameraIdleListener(() -> {
                    if (map != null) {
                        LatLng centerLatLng = map.getCameraPosition().target;
                        map.clear(); // Clear previous markers
                        map.addMarker(new MarkerOptions().position(centerLatLng).title("Farm Location"));
                    }
                });

                // Handle map clicks
                googleMap.setOnMapClickListener(latLng -> {
                    if (map != null) {
                        map.clear();
                        map.addMarker(new MarkerOptions().position(latLng).title("Farm Location"));
                        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        farmLocatin = latLng;
                    }
                });
            }
        });
    }

    private void setupLocationButton() {
        Button mapButton = findViewById(R.id.addFarmLocationButton);
        mapButton.setOnClickListener(view -> {
            if (map != null) {
                farmLocatin = map.getCameraPosition().target;
                map.clear();
                map.addMarker(new MarkerOptions().position(farmLocatin).title("Farm Location"));
                showSuccessMessage("Location selected successfully");
                Log.i("FarmShareLog", "Selected location: " + farmLocatin.latitude + ", " + farmLocatin.longitude);
            } else {
                showErrorDialog("Map is not ready yet. Please try again.");
            }
        });
    }

    private void setupImageUrlHandling() {
        addFarmImageUrlButton.setOnClickListener(view -> {
            String imageUrl = farmImageUrlEditText.getText().toString().trim();
            if (!imageUrl.isEmpty() && isValidUrl(imageUrl)) {
                farmImageUrls.add(imageUrl);
                farmImageUrlEditText.setText("");
                updateFarmImageUrlsDisplay();
            } else {
                showErrorDialog("Please enter a valid image URL");
            }
        });
    }

    private void updateFarmImageUrlsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < farmImageUrls.size(); i++) {
            sb.append(i + 1).append(". ").append(farmImageUrls.get(i)).append("\n");
        }
        farmImageUrlsTextView.setText(sb.toString());
    }

    private void setupSubmitButton() {
        LinearLayout submitButton = findViewById(R.id.addFarmSubmitButton);
        submitButton.setOnClickListener(view -> validateAndSubmitFarm());
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private void validateAndSubmitFarm() {
        if (farmTypeSpinner.getSelectedItem().toString().equals("Select")) {
            showErrorDialog("Select crop type");
        } else if (farmNameEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm name");
        } else if (farmSizeEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm size");
        } else if (farmAvgEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm Avg income");
        } else if (farmMinInvestEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter Min invest");
        } else if (farmCodeNameEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm code name");
        } else if (farmDescriptionEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm description");
        } else if (farmNicNumberEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter owner NIC");
        } else if (farmSoilPhEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter soil PH level");
        } else if (farmSoilMoisturEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter soil moisture content");
        } else if (farmSoilOrganicEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter soil organic matter");
        } else if (farmSoilNutrientEditText.getText().toString().isBlank()) {
            showErrorDialog("Please enter farm nutrient level");
        } else if (!isValidUrl(nicFrontUrlEditText.getText().toString())) {
            showErrorDialog("Please provide valid NIC front image URL");
        } else if (!isValidUrl(nicBackUrlEditText.getText().toString())) {
            showErrorDialog("Please provide valid NIC back image URL");
        } else if (!isValidUrl(ownershipDocUrlEditText.getText().toString())) {
            showErrorDialog("Please provide valid ownership document URL");
        } else if (!isValidUrl(analysisDocUrlEditText.getText().toString())) {
            showErrorDialog("Please provide valid farm output record document URL");
        } else if (!isValidUrl(soilReportUrlEditText.getText().toString())) {
            showErrorDialog("Please provide valid farm soil report document URL");
        } else if (farmImageUrls.isEmpty()) {
            showErrorDialog("Please add at least one farm image URL");
        } else if (farmLocatin == null) {
            showErrorDialog("Please select farm location");
        } else {
            collectFormData();
            saveFarmToFirestore();
        }
    }

    private void collectFormData() {
        farmType = farmTypeSpinner.getSelectedItem().toString();
        soilNutrient = Double.parseDouble(farmSoilNutrientEditText.getText().toString());
        soilOrganicMatter = Double.parseDouble(farmSoilOrganicEditText.getText().toString());
        soilMoisture = Double.parseDouble(farmSoilMoisturEditText.getText().toString());
        soilPh = Double.parseDouble(farmSoilPhEditText.getText().toString());
        farmNicNumber = farmNicNumberEditText.getText().toString();
        farmDescription = farmDescriptionEditText.getText().toString();
        farmCodeName = farmCodeNameEditText.getText().toString();
        farmMinInvest = Integer.parseInt(farmMinInvestEditText.getText().toString());
        farmAvgIncome = Integer.parseInt(farmAvgEditText.getText().toString());
        farmSize = Double.parseDouble(farmSizeEditText.getText().toString());
        farmName = farmNameEditText.getText().toString();

        nicFrontUrl = nicFrontUrlEditText.getText().toString().trim();
        nicBackUrl = nicBackUrlEditText.getText().toString().trim();
        ownershipDocUrl = ownershipDocUrlEditText.getText().toString().trim();
        analysisDocUrl = analysisDocUrlEditText.getText().toString().trim();
        soilReportDocUrl = soilReportUrlEditText.getText().toString().trim();
    }

    private void saveFarmToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Gson gson = new Gson();

        SharedPreferences sharedPreferences = getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user", null);

        if (userJson != null) {
            UserDto user = gson.fromJson(userJson, UserDto.class);

            pDialog = new Sweetalert(this, Sweetalert.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Saving farm data");
            pDialog.setCancelable(false);
            pDialog.show();

            Map<String, Object> farmData = new HashMap<>();
            farmData.put("farmType", farmType);
            farmData.put("userId", String.valueOf(user.getId()));
            farmData.put("soilNutrient", soilNutrient);
            farmData.put("soilOrganicMatter", soilOrganicMatter);
            farmData.put("soilMoisture", soilMoisture);
            farmData.put("soilPh", soilPh);
            farmData.put("ownerNic", farmNicNumber);
            farmData.put("description", farmDescription);
            farmData.put("codeName", farmCodeName);
            farmData.put("minInvest", farmMinInvest);
            farmData.put("avgIncome", farmAvgIncome);
            farmData.put("farmSize", farmSize);
            farmData.put("farmName", farmName);
            farmData.put("location", new GeoPoint(farmLocatin.latitude, farmLocatin.longitude));
            
            // Add document URLs
            farmData.put("nicFrontUrl", nicFrontUrl);
            farmData.put("nicBackUrl", nicBackUrl);
            farmData.put("ownershipDocUrl", ownershipDocUrl);
            farmData.put("analysisDocUrl", analysisDocUrl);
            farmData.put("soilReportDocUrl", soilReportDocUrl);
            farmData.put("farmImageUrls", farmImageUrls);

            db.collection("Farms")
                    .add(farmData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("FarmShareLog", "Farm added successfully with ID: " + documentReference.getId());
                        runOnUiThread(() -> {
                            pDialog.cancel();
                            new Sweetalert(this, Sweetalert.SUCCESS_TYPE)
                                    .setTitleText("Success")
                                    .setContentText("Farm added successfully")
                                    .show();

                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(this, FarmerMainActivity.class);
                                startActivity(intent);
                                finish();
                            }, 700);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.w("FarmShareLog", "Error adding farm", e);
                        runOnUiThread(() -> {
                            pDialog.cancel();
                            new Sweetalert(this, Sweetalert.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Failed to add farm: " + e.getMessage())
                                    .show();
                        });
                    });
        }
    }

    private void showErrorDialog(String message) {
        new Sweetalert(this, Sweetalert.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText(message)
                .show();
    }

    private void showSuccessMessage(String message) {
        new Sweetalert(this, Sweetalert.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(message)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                map.setMyLocationEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
            }
        }
    }
    private class CropAdapter extends ArrayAdapter<String> {
        List<String> cropList;
        int layout;
        int selectedLayout;

        public CropAdapter(@NonNull Context context, int resource, @NonNull List<String> objects,int selectedResource) {
            super(context, resource, objects);
            this.cropList = objects;
            this.layout = resource;
            this.selectedLayout = selectedResource;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(layout,parent,false);
            ImageView image = view.findViewById(R.id.imageView8);
            TextView text = view.findViewById(R.id.textView50);

            String crop = cropList.get(position);
            if(crop.equals("Select")){
                image.setImageResource(R.drawable.empty_crop);
                text.setText("Select");
            } else if(crop.equals("Owner")){
                image.setImageResource(R.drawable.rice);
                text.setText("Owner");
            } else if (crop.equals("Partnership")) {
                image.setImageResource(R.drawable.corn);
                text.setText("Partnership");
            }

            return view;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            @SuppressLint("ViewHolder") View view = inflater.inflate(selectedLayout,parent,false);
            ImageView image = view.findViewById(R.id.imageViewSelected);

            String crop = cropList.get(position);
            if(crop.equals("Select")){
                image.setImageResource(R.drawable.empty_crop);
            } else if(crop.equals("Rice")){
                image.setImageResource(R.drawable.rice);
            } else if (crop.equals("Corn")) {
                image.setImageResource(R.drawable.corn);
            }

            return view;
        }
    }
}