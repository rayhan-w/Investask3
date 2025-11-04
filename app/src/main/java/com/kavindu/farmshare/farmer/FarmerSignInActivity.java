package com.kavindu.farmshare.farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kavindu.farmshare.BuildConfig;
import com.kavindu.farmshare.HumanVerifyActivity;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.ResponseDto;
import com.kavindu.farmshare.dto.UserDto;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import taimoor.sultani.sweetalert2.Sweetalert;

public class FarmerSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_farmer_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText mobileEditText = findViewById(R.id.FSignInEditTextPhone);
        EditText passwordEditText = findViewById(R.id.FSignInEditTextPassword);

        startAnimations();

        mobileEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.fSignInMobileBg,R.id.fSignInMobileIcon,R.id.FSignIntextView3,R.id.FSignInEditTextPhone);
            }
        });

        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.fSignInPasswordBg,R.id.fSignInPasswordIcon,R.id.FSignIntextView4,R.id.FSignInEditTextPassword);
            }
        });

        ImageView backImageView = findViewById(R.id.FSignInBackImageView1);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        LinearLayout button2 = findViewById(R.id.FSignInsignInButton2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        LinearLayout signInButton = findViewById(R.id.FSignInsignInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mobileEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.fSignInMobileBg,R.id.fSignInMobileIcon,R.id.FSignIntextView3,R.id.FSignInEditTextPhone);
                    errorAnimation(R.id.FSignInmobileLayout);

                } else if (passwordEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.fSignInPasswordBg,R.id.fSignInPasswordIcon,R.id.FSignIntextView4,R.id.FSignInEditTextPassword);
                    errorAnimation(R.id.FSignInPasswordLayout);

                } else{
                    Gson gson = new Gson();

                    Sweetalert pDialog = new Sweetalert(FarmerSignInActivity.this, Sweetalert.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Processing");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    // For Firebase Auth, we use email/password with mobile as email
                    String email = mobileEditText.getText().toString() + "@farmshare.com";
                    String password = passwordEditText.getText().toString();
                    
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    
                    mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(FarmerSignInActivity.this, task -> {
                            // Don't dismiss dialog here, we'll dismiss it after Firestore operation completes
                            
                            if (task.isSuccessful()) {
                                // Sign in success, get user data from Firestore
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                
                                // Get additional user data from Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        // Ensure dialog is dismissed here
                                        if (pDialog.isShowing()) {
                                            pDialog.dismissWithAnimation();
                                        }
                                        if (documentSnapshot.exists()) {
                                            // Create UserDto from Firestore data
                                            UserDto respUserDto = new UserDto();
                                            respUserDto.setId(firebaseUser.getUid().hashCode()); // Use hash of UID for backward compatibility
                                            respUserDto.setMobile(documentSnapshot.getString("mobile"));
                                            respUserDto.setFname(documentSnapshot.getString("fname"));
                                            respUserDto.setLname(documentSnapshot.getString("lname"));
                                            respUserDto.setUserType(documentSnapshot.getString("userType"));
                                            respUserDto.setActiveStatus(documentSnapshot.getString("activeStatus"));
                                            respUserDto.setProfilePic(documentSnapshot.getString("profilePic") != null ? 
                                                                    documentSnapshot.getString("profilePic") : "");
                                            
                                            // Save user to SharedPreferences
                                            SharedPreferences sp = getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("user", gson.toJson(respUserDto));
                                            editor.apply();
                                            
                                            // Ensure dialog is dismissed here before showing success message
                                            if (pDialog.isShowing()) {
                                                pDialog.dismissWithAnimation();
                                            }
                                            
                                            // Show success message
                                            Sweetalert successAlert = new Sweetalert(FarmerSignInActivity.this, Sweetalert.SUCCESS_TYPE)
                                                .setTitleText("Success")
                                                .setContentText("Login successful!");
                                            
                                            // Show the success message briefly
                                            successAlert.show();
                                            
                                            // Automatically navigate to main activity after a short delay
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                // Dismiss the success alert if it's still showing
                                                if (successAlert.isShowing()) {
                                                    successAlert.dismissWithAnimation();
                                                }
                                                
                                                // Navigate to main activity
                                                Intent intent = new Intent(FarmerSignInActivity.this, FarmerMainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }, 1500); // 1.5 second delay
                                        } else {
                                            // User document doesn't exist in Firestore
                                            new Sweetalert(FarmerSignInActivity.this, Sweetalert.ERROR_TYPE)
                                                .setTitleText("Oops...")
                                                .setContentText("User data not found")
                                                .show();
                                                
                                            // Sign out from Firebase Auth
                                            mAuth.signOut();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Ensure dialog is dismissed here
                                        if (pDialog.isShowing()) {
                                            pDialog.dismissWithAnimation();
                                        }
                                        
                                        // Failed to get user data
                                        new Sweetalert(FarmerSignInActivity.this, Sweetalert.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Failed to get user data: " + e.getMessage())
                                            .show();
                                            
                                        // Sign out from Firebase Auth
                                        mAuth.signOut();
                                    });
                            } else {
                                // Ensure dialog is dismissed here for authentication failure
                                if (pDialog.isShowing()) {
                                    pDialog.dismissWithAnimation();
                                }
                                
                                // If sign in fails, display a message to the user
                                String errorMessage = "Authentication failed";
                                if (task.getException() != null) {
                                    errorMessage = task.getException().getMessage();
                                }
                                
                                new Sweetalert(FarmerSignInActivity.this, Sweetalert.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText(errorMessage)
                                    .show();
                            }
                        });

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimations();
    }

    private void startAnimations(){

        LinearLayout mobileLayout = findViewById(R.id.FSignInmobileLayout);
        LinearLayout passwordLayout = findViewById(R.id.FSignInPasswordLayout);
        LinearLayout button1 = findViewById(R.id.FSignInsignInButton);
        LinearLayout button2 = findViewById(R.id.FSignInsignInButton2);

        mobileLayout.setVisibility(View.INVISIBLE);
        passwordLayout.setVisibility(View.INVISIBLE);
        button1.setVisibility(View.INVISIBLE);
        button2.setVisibility(View.INVISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mobileLayout.setVisibility(View.VISIBLE);
                mobileLayout.startAnimation(AnimationUtils.loadAnimation(FarmerSignInActivity.this,R.anim.from_bottom_fade_in));
            }
        },200);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                passwordLayout.setVisibility(View.VISIBLE);
                passwordLayout.startAnimation(AnimationUtils.loadAnimation(FarmerSignInActivity.this,R.anim.from_bottom_fade_in));
            }
        },400);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button1.setVisibility(View.VISIBLE);
                button1.startAnimation(AnimationUtils.loadAnimation(FarmerSignInActivity.this,R.anim.from_bottom_fade_in));
            }
        },600);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                button2.setVisibility(View.VISIBLE);
                button2.startAnimation(AnimationUtils.loadAnimation(FarmerSignInActivity.this,R.anim.from_bottom_fade_in));
            }
        },800);

    }

    private void errorAnimation(int id){
        LinearLayout layout = findViewById(id);
        layout.startAnimation(AnimationUtils.loadAnimation(FarmerSignInActivity.this,R.anim.shake_animation));
    }

    private void setErrorColor(boolean isError,int backgroundId, int iconId,int titleId,int editTextId){
        LinearLayout background = findViewById(backgroundId);
        ImageView icon = findViewById(iconId);
        TextView title = findViewById(titleId);
        EditText editText = findViewById(editTextId);

        int color;
        int titleColor;
        Drawable bgDrawable;

        if(isError){
            color = ContextCompat.getColor(FarmerSignInActivity.this,R.color.error_red);
            titleColor = color;
            bgDrawable = ContextCompat.getDrawable(FarmerSignInActivity.this,R.drawable.custon_error_text_view_bg);

        }else {
            color = ContextCompat.getColor(FarmerSignInActivity.this,R.color.textGray);
            titleColor = ContextCompat.getColor(FarmerSignInActivity.this,R.color.black);
            bgDrawable = ContextCompat.getDrawable(FarmerSignInActivity.this,R.drawable.custom_textview_background);
        }


        background.setBackground(bgDrawable);
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        title.setTextColor(titleColor);
        editText.setHintTextColor(color);
    }

}

