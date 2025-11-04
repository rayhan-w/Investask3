package com.kavindu.farmshare.investor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kavindu.farmshare.NotificationActivity;
import com.kavindu.farmshare.R;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.model.TransactionItem;

import java.util.ArrayList;

import taimoor.sultani.sweetalert2.Sweetalert;
import com.google.gson.Gson;


public class InvestorTransactionFragment extends Fragment {


    public InvestorTransactionFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        ImageView menuButton = view.findViewById(R.id.investorTransactionMenu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() instanceof InvestorMainActivity) {
                    ((InvestorMainActivity) getActivity()).openDrawer();
                }
            }
        });

        ImageView notificationButton = view.findViewById(R.id.investorTransactionNotification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewClick) {

                Intent intent = new Intent(view.getContext(), NotificationActivity.class);
                startActivity(intent);

            }
        });

        ShapeableImageView profileImge = view.findViewById(R.id.investorTransactionShapeableImageView);
        TextView name = view.findViewById(R.id.textView127);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("com.kavindu.farmshare.data", Context.MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user",null);
        Gson gson = new Gson();

        UserDto userDto = gson.fromJson(userJson, UserDto.class);

        if (userDto.getProfilePic().isEmpty()){
            profileImge.setImageResource(R.drawable.globe);
        }else{
            Glide.with(view.getContext())
                    .load(userDto.getProfilePic())
                    .placeholder(R.drawable.loading)
                    .into(profileImge);
        }

        String userName = userDto.getFname()+" "+userDto.getLname();
        name.setText(userName);

        Sweetalert pDialog = new Sweetalert(view.getContext(), Sweetalert.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Processing");
        pDialog.setCancelable(false);
        pDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Transactions")
                .whereEqualTo("userId", userDto.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<TransactionItem> transactionItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            transactionItems.add(document.toObject(TransactionItem.class));
                        }
                        // Separate transactions into today and old
                        // This logic is simplified, you might need a more robust solution
                        ArrayList<TransactionItem> todayTransactions = new ArrayList<>();
                        ArrayList<TransactionItem> oldTransactions = new ArrayList<>();
                        for(TransactionItem item : transactionItems){
                            // A simple check if the transaction is from today
                            // You should replace this with a proper date comparison
                            if(item.getTime().contains("Today")){
                                todayTransactions.add(item);
                            } else {
                                oldTransactions.add(item);
                            }
                        }

                        transactionItemInflater(R.id.todayTransactionsContainer, view, todayTransactions);
                        transactionItemInflater(R.id.oldTransactionsContainer, view, oldTransactions);

                    } else {
                        // Handle error
                    }
                    pDialog.cancel();
                });


//        oldTransactionItems.add(new TransactionItem("Test Farm 3","Deposit","+20,870.00 RS","Jan 3, 16:30"));




        return view;
    }

    private void transactionItemInflater(int container,View parent, ArrayList<TransactionItem> itemArrayList){

        LinearLayout itemContainer = parent.findViewById(container);

        for (TransactionItem transactionItem : itemArrayList){

            View item = getLayoutInflater().inflate(R.layout.fragment_transaction_item,null);

            TextView name = item.findViewById(R.id.textView132);
            TextView type = item.findViewById(R.id.textView133);
            TextView price = item.findViewById(R.id.textView134);
            TextView time = item.findViewById(R.id.textView135);
            ImageView image = item.findViewById(R.id.imageView28);

            name.setText(transactionItem.getName());
            type.setText(transactionItem.getType());
            price.setText(transactionItem.getPrice());
            time.setText(transactionItem.getTime());

            if (transactionItem.getType().equals("Deposit")){
                image.setImageResource(R.drawable.down_left_arrow);
                image.setBackground(ContextCompat.getDrawable(parent.getContext(),R.drawable.transaction_bg2));
                price.setTextColor(ContextCompat.getColor(parent.getContext(),R.color.green));
            }


            itemContainer.addView(item);

        }

    }
}