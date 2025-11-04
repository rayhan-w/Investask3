package com.kavindu.farmshare.farmer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kavindu.farmshare.BuildConfig;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.RequestDto;
import com.kavindu.farmshare.dto.ResponseDto;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.model.FarmItem;
import com.kavindu.farmshare.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import taimoor.sultani.sweetalert2.Sweetalert;

public class FarmerFarmsFragment extends Fragment {

    private static final String TAG = "FarmerFarmsFragment";
    private RecyclerView recyclerView;
    private OkHttpClient client;

    public FarmerFarmsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farmer_farms, container, false);

        recyclerView = view.findViewById(R.id.famsRecyclerView);
        client = new OkHttpClient();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        loadFarms(view);

        return view;
    }

    private void loadFarms(View view) {
        if (!NetworkUtils.isNetworkConnected(getContext())) {
            Toast.makeText(getContext(), "No internet connection. Please check your connection and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        Sweetalert pDialog = new Sweetalert(view.getContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing");
        pDialog.setCancelable(false);
        pDialog.show();

        SharedPreferences sp = getActivity().getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
        String user = sp.getString("user", null);

        if (user != null) {
            new Thread(() -> {
                try {
                    UserDto userDto = new Gson().fromJson(user, UserDto.class);
                    RequestDto requestDto = new RequestDto();
                    requestDto.setId(userDto.getId());

                    String json = new Gson().toJson(requestDto);
                    RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

                    Request request = new Request.Builder()
                            .url(BuildConfig.URL + "/api/farms/farmer")
                            .post(body)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResponseDto responseDto = new Gson().fromJson(response.body().string(), ResponseDto.class);
                            
                            if (responseDto.isSuccess()) {
                                ArrayList<FarmItem> farmItems = new Gson().fromJson(
                                        new Gson().toJson(responseDto.getData()),
                                        new TypeToken<ArrayList<FarmItem>>() {}.getType()
                                );

                                requireActivity().runOnUiThread(() -> {
                                    recyclerView.setAdapter(new FarmItemAdapter(farmItems));
                                    pDialog.dismissWithAnimation();
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    pDialog.dismissWithAnimation();
                                    new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                            .setTitleText("Error")
                                            .setContentText(responseDto.getMessage())
                                            .show();
                                });
                            }
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                pDialog.dismissWithAnimation();
                                new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText("Failed to connect to server")
                                        .show();
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Network error", e);
                    requireActivity().runOnUiThread(() -> {
                        pDialog.dismissWithAnimation();
                        new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                .setTitleText("Connection Error")
                                .setContentText("Unable to connect to server. Please check your internet connection.")
                                .show();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error", e);
                    requireActivity().runOnUiThread(() -> {
                        pDialog.dismissWithAnimation();
                        new Sweetalert(view.getContext(), Sweetalert.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("An unexpected error occurred")
                                .show();
                    });
                }
            }).start();
        }
    }
}