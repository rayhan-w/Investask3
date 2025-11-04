package com.kavindu.farmshare.investor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kavindu.farmshare.BuildConfig;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.ResponseDto;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.farmer.FarmerMainActivity;
import com.kavindu.farmshare.farmer.FarmerSignupActivity;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import taimoor.sultani.sweetalert2.Sweetalert;

public class InvestorSignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_investor_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText mobileEditText = findViewById(R.id.ISignUpeditTextPhone);
        EditText fnameEditText = findViewById(R.id.ISignUpeditTextfname);
        EditText lnameEditText = findViewById(R.id.ISignUpeditTextlname);
        EditText passwordEditText = findViewById(R.id.ISignUpeditTextTextPassword);
        EditText rePasswordEditText = findViewById(R.id.ISignUpeditTextTextPassword2);

        startAnimations();


        mobileEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.iMobileBg,R.id.iMobileIcon,R.id.ISignUptextView8,R.id.ISignUpeditTextPhone);
            }
        });

        fnameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.iFnameBg,R.id.iFnameIcon,R.id.ISignUptextView9,R.id.ISignUpeditTextfname);
            }
        });

        lnameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.iLnameBg,R.id.iLnameIcon,R.id.ISignUptextView10,R.id.ISignUpeditTextlname);
            }
        });

        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.iPasswordBg,R.id.iPasswordIcon,R.id.ISignUptextView11,R.id.ISignUpeditTextTextPassword);
            }
        });

        rePasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                setErrorColor(false,R.id.iRePasswordBg,R.id.iRePasswordIcon,R.id.ISignUptextView12,R.id.ISignUpeditTextTextPassword2);
            }
        });



        ImageView backButton = findViewById(R.id.ISignUpbackImageView1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        LinearLayout button2 = findViewById(R.id.ISignUpsignUpButton2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InvestorSignUpActivity.this,InvestorSignInActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout buttonSignUp = findViewById(R.id.ISignUpsignUpButton);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mobileEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.iMobileBg,R.id.iMobileIcon,R.id.ISignUptextView8,R.id.ISignUpeditTextPhone);
                    errorAnimation(R.id.ISignUpmobileLayout);

                } else if (fnameEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.iFnameBg,R.id.iFnameIcon,R.id.ISignUptextView9,R.id.ISignUpeditTextfname);
                    errorAnimation(R.id.ISignUpfnameLayout);

                } else if (lnameEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.iLnameBg,R.id.iLnameIcon,R.id.ISignUptextView10,R.id.ISignUpeditTextlname);
                    errorAnimation(R.id.ISignUplnameLayout);

                } else if (passwordEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.iPasswordBg,R.id.iPasswordIcon,R.id.ISignUptextView11,R.id.ISignUpeditTextTextPassword);
                    errorAnimation(R.id.ISignUppwLayout);

                } else if (rePasswordEditText.getText().toString().isBlank()){

                    setErrorColor(true,R.id.iRePasswordBg,R.id.iRePasswordIcon,R.id.ISignUptextView12,R.id.ISignUpeditTextTextPassword2);
                    errorAnimation(R.id.ISignUppwConformLayout);

                } else {
                    // Create user data
                    String mobile = mobileEditText.getText().toString();
                    String fname = fnameEditText.getText().toString();
                    String lname = lnameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    
                    // For Firebase Auth, we use email/password with mobile as email
                    String email = mobile + "@farmshare.com";
                    
                    Gson gson = new Gson();

                    Sweetalert pDialog = new Sweetalert(InvestorSignUpActivity.this, Sweetalert.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Processing");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    // Get Firebase Auth instance
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    
                    // Create user with email and password
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(InvestorSignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success, save user data to Firestore
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                
                                // Create user data for Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("mobile", mobile);
                                userData.put("fname", fname);
                                userData.put("lname", lname);
                                userData.put("userType", "investor");
                                userData.put("activeStatus", "active");
                                userData.put("createdAt", new Date());
                                userData.put("profilePic", "");
                                
                                // Save to Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users")
                                    .document(firebaseUser.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Create UserDto for SharedPreferences (backward compatibility)
                                        UserDto userDto = new UserDto();
                                        userDto.setId(firebaseUser.getUid().hashCode()); // Use hash of UID for backward compatibility
                                        userDto.setMobile(mobile);
                                        userDto.setFname(fname);
                                        userDto.setLname(lname);
                                        userDto.setUserType("investor");
                                        userDto.setActiveStatus("active");
                                        userDto.setProfilePic("");
                                        
                                        // Save to SharedPreferences
                                        SharedPreferences sp = getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("user", gson.toJson(userDto));
                                        editor.apply();
                                        
                                        // Show success message
                                        pDialog.cancel();
                                        new Sweetalert(InvestorSignUpActivity.this, Sweetalert.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Registration successful!")
                                            .show();
                                        
                                        // Navigate to main activity
                                        Intent intent = new Intent(InvestorSignUpActivity.this, InvestorMainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to save user data to Firestore
                                        pDialog.cancel();
                                        new Sweetalert(InvestorSignUpActivity.this, Sweetalert.ERROR_TYPE)
                                            .setTitleText("Oops...")
                                            .setContentText("Failed to save user data: " + e.getMessage())
                                            .show();
                                            
                                        // Delete the Firebase Auth user since Firestore save failed
                                        firebaseUser.delete();
                                    });
                            } else {
                                // If sign up fails, display a message to the user
                                pDialog.cancel();
                                String errorMessage = "Registration failed";
                                
                                Exception exception = task.getException();
                                if (exception != null) {
                                    if (exception instanceof FirebaseAuthUserCollisionException) {
                                        errorMessage = "This mobile number is already registered";
                                    } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                                        errorMessage = "Password is too weak. Please use at least 6 characters";
                                    } else {
                                        errorMessage = exception.getMessage();
                                    }
                                }
                                
                                new Sweetalert(InvestorSignUpActivity.this, Sweetalert.ERROR_TYPE)
                                    .setTitleText("Oops...")
                                    .setContentText(errorMessage)
                                    .show();
                            }
                        });


                }

//                Intent intent = new Intent(InvestorSignUpActivity.this,InvestorMainActivity.class);
//                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        startAnimations();
    }

    private void startAnimations(){
        LinearLayout mobileLayout = findViewById(R.id.ISignUpmobileLayout);
        LinearLayout fnameLayout = findViewById(R.id.ISignUpfnameLayout);
        LinearLayout lnameLayout = findViewById(R.id.ISignUplnameLayout);
        LinearLayout pwLayout = findViewById(R.id.ISignUppwLayout);
        LinearLayout pwConformLayout = findViewById(R.id.ISignUppwConformLayout);
        LinearLayout signUpButton = findViewById(R.id.ISignUpsignUpButton);
        LinearLayout signUpButton2 = findViewById(R.id.ISignUpsignUpButton2);

        mobileLayout.setVisibility(View.INVISIBLE);
        fnameLayout.setVisibility(View.INVISIBLE);
        lnameLayout.setVisibility(View.INVISIBLE);
        pwLayout.setVisibility(View.INVISIBLE);
        pwConformLayout.setVisibility(View.INVISIBLE);
        signUpButton.setVisibility(View.INVISIBLE);
        signUpButton2.setVisibility(View.INVISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mobileLayout.setVisibility(View.VISIBLE);
                mobileLayout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },100);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fnameLayout.setVisibility(View.VISIBLE);
                fnameLayout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },200);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lnameLayout.setVisibility(View.VISIBLE);
                lnameLayout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },300);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pwLayout.setVisibility(View.VISIBLE);
                pwLayout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },400);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pwConformLayout.setVisibility(View.VISIBLE);
                pwConformLayout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                signUpButton.setVisibility(View.VISIBLE);
                signUpButton.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },600);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                signUpButton2.setVisibility(View.VISIBLE);
                signUpButton2.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.from_bottom_fade_in));
            }
        },700);
    }

    private void errorAnimation(int id){
        LinearLayout layout = findViewById(id);
        layout.startAnimation(AnimationUtils.loadAnimation(InvestorSignUpActivity.this,R.anim.shake_animation));
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
            color = ContextCompat.getColor(InvestorSignUpActivity.this,R.color.error_red);
            titleColor = color;
            bgDrawable = ContextCompat.getDrawable(InvestorSignUpActivity.this,R.drawable.custon_error_text_view_bg);

        }else {
            color = ContextCompat.getColor(InvestorSignUpActivity.this,R.color.textGray);
            titleColor = ContextCompat.getColor(InvestorSignUpActivity.this,R.color.black);
            bgDrawable = ContextCompat.getDrawable(InvestorSignUpActivity.this,R.drawable.custom_textview_background);
        }


        background.setBackground(bgDrawable);
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        title.setTextColor(titleColor);
        editText.setHintTextColor(color);
    }
}