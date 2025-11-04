package com.kavindu.farmshare.farmer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kavindu.farmshare.R;
import com.kavindu.farmshare.model.FarmItem;

import java.util.ArrayList;

public class FarmItemAdapter extends RecyclerView.Adapter<FarmItemAdapter.ViewHolder> {

    private final ArrayList<FarmItem> farmItems;

    public FarmItemAdapter(ArrayList<FarmItem> farmItems) {
        this.farmItems = farmItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_farmer_farm__item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FarmItem farmItem = farmItems.get(position);

        // Set farm image based on crop type
        if ("Rice".equals(farmItem.getCropType())) {
            holder.farmImage.setImageResource(R.drawable.rice);
        } else if ("Corn".equals(farmItem.getCropType())) {
            holder.farmImage.setImageResource(R.drawable.corn);
        }

        // Set risk indicator
        if ("true".equals(farmItem.getIsAtRisk())) {
            holder.riskIndicator.setBackground(
                ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.gradient_risk)
            );
            holder.riskText.setText("At risk");
        } else {
            holder.riskIndicator.setBackground(
                ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.gradient_good)
            );
            holder.riskText.setText("In good hands");
        }

        // Set farm name and stock count
        holder.farmName.setText(farmItem.getFarmName());
        holder.stockCount.setText("S " + farmItem.getStockCount());

        // Set click listener for the farm item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), FarmerFarmDetailsActivity.class);
            intent.putExtra("farmId", farmItem.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return farmItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView farmImage;
        final TextView farmName;
        final TextView riskText;
        final View riskIndicator;
        final TextView stockCount;
        final ConstraintLayout farmItemLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            farmImage = itemView.findViewById(R.id.imageView15);
            farmName = itemView.findViewById(R.id.textView39);
            riskText = itemView.findViewById(R.id.textView40);
            riskIndicator = itemView.findViewById(R.id.view5);
            stockCount = itemView.findViewById(R.id.textView41);
            farmItemLayout = itemView.findViewById(R.id.framItem);
        }
    }
}