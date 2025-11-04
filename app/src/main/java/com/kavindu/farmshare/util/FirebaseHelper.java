package com.kavindu.farmshare.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kavindu.farmshare.dto.FarmItemDto;
import com.kavindu.farmshare.dto.PaymentDto;
import com.kavindu.farmshare.dto.SingleFarmDto;
import com.kavindu.farmshare.dto.TransactionDto;
import com.kavindu.farmshare.dto.UserDto;
import com.kavindu.farmshare.model.TransactionItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class for Firebase operations
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Get current Firebase user
     */
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Add a new farm to Firestore
     */
    public static void addFarm(SingleFarmDto farmDto, OnSuccessListener<DocumentReference> successListener, OnFailureListener failureListener) {
        Map<String, Object> farm = new HashMap<>();
        farm.put("farmName", farmDto.getFarmName());
        farm.put("farmType", farmDto.getFarmType());
        farm.put("stockPrice", farmDto.getStockPrice());
        farm.put("valuePrice", farmDto.getValuePrice());
        farm.put("avgIncome", farmDto.getAvgIncome());
        farm.put("expectIncome", farmDto.getExpectIncome());
        farm.put("farmerId", farmDto.getFarmerId());
        farm.put("status", "Pending");
        farm.put("createdAt", new Date());
        farm.put("totalStocks", farmDto.getTotalStocks());
        farm.put("releasedStocks", 0);
        farm.put("latitude", farmDto.getLatitude());
        farm.put("longitude", farmDto.getLongitude());
        
        db.collection("farms")
            .add(farm)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }

    /**
     * Get all farms from Firestore
     */
    public static void getAllFarms(OnCompleteListener<QuerySnapshot> completeListener) {
        db.collection("farms")
            .get()
            .addOnCompleteListener(completeListener);
    }

    /**
     * Get farms by farmer ID
     */
    public static void getFarmsByFarmerId(String farmerId, OnCompleteListener<QuerySnapshot> completeListener) {
        db.collection("farms")
            .whereEqualTo("farmerId", farmerId)
            .get()
            .addOnCompleteListener(completeListener);
    }

    /**
     * Update farm status
     */
    public static void updateFarmStatus(String farmId, String status, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        db.collection("farms")
            .document(farmId)
            .update("status", status)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }
    
    /**
     * Process stock purchase
     */
    public static void processStockPurchase(PaymentDto paymentDto, String userId, String farmName, 
                                           OnSuccessListener<Void> successListener, 
                                           OnFailureListener failureListener) {
        // Get farm reference
        db.collection("farms")
            .document(String.valueOf(paymentDto.getFarmId()))
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Create transaction record
                        Map<String, Object> transaction = new HashMap<>();
                        transaction.put("userId", userId);
                        transaction.put("farmId", String.valueOf(paymentDto.getFarmId()));
                        transaction.put("farmName", farmName);
                        transaction.put("amount", paymentDto.getPrice());
                        transaction.put("stockCount", paymentDto.getStockCount());
                        transaction.put("returnType", paymentDto.getReturnType());
                        transaction.put("timestamp", new Date());
                        transaction.put("status", "Completed");
                        
                        // Create investment record
                        Map<String, Object> investment = new HashMap<>();
                        investment.put("userId", userId);
                        investment.put("farmId", String.valueOf(paymentDto.getFarmId()));
                        investment.put("stockCount", paymentDto.getStockCount());
                        investment.put("investmentAmount", paymentDto.getPrice());
                        investment.put("returnType", paymentDto.getReturnType());
                        investment.put("timestamp", new Date());
                        
                        // Update farm stock count
                        long currentReleasedStocks = documentSnapshot.getLong("releasedStocks") != null ? 
                                                    documentSnapshot.getLong("releasedStocks") : 0;
                        long newReleasedStocks = currentReleasedStocks + paymentDto.getStockCount();
                        
                        // Start a batch operation
                        db.runTransaction(transaction1 -> {
                            // Update farm released stocks
                            transaction1.update(db.collection("farms").document(String.valueOf(paymentDto.getFarmId())), 
                                              "releasedStocks", newReleasedStocks);
                            
                            // Add transaction record
                            transaction1.set(db.collection("transactions").document(), transaction);
                            
                            // Add investment record
                            transaction1.set(db.collection("investments").document(), investment);
                            
                            return null;
                        }).addOnSuccessListener(result -> {
                            if (successListener != null) {
                                successListener.onSuccess(null);
                            }
                        })
                          .addOnFailureListener(failureListener);
                    } else {
                        failureListener.onFailure(new Exception("Farm not found"));
                    }
                }
            })
            .addOnFailureListener(failureListener);
    }
    
    /**
      * Get farm details by ID
      */
     public static void getFarmById(String farmId, OnSuccessListener<DocumentSnapshot> successListener, 
                                   OnFailureListener failureListener) {
         db.collection("farms")
             .document(farmId)
             .get()
             .addOnSuccessListener(successListener)
             .addOnFailureListener(failureListener);
     }

    /**
     * Add a transaction to Firestore
     */
    public static void addTransaction(String userId, String farmId, double amount, String type, OnSuccessListener<DocumentReference> successListener, OnFailureListener failureListener) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("userId", userId);
        transaction.put("farmId", farmId);
        transaction.put("amount", amount);
        transaction.put("type", type); // "Deposit" or "Withdrawal"
        transaction.put("timestamp", new Date());
        
        // Format date for display
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
        transaction.put("formattedDate", sdf.format(new Date()));
        
        db.collection("transactions")
            .add(transaction)
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }

    /**
     * Get transactions by user ID
     */
    public static void getTransactionsByUserId(String userId, OnCompleteListener<QuerySnapshot> completeListener) {
        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(completeListener);
    }

    /**
     * Process payment for farm
     */
    public static void processPayment(PaymentDto paymentDto, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        // First get the farm
        db.collection("farms")
            .document(String.valueOf(paymentDto.getFarmId()))
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Update farm stock count
                        int releasedStocks = documentSnapshot.getLong("releasedStocks").intValue();
                        int newReleasedStocks = releasedStocks + paymentDto.getStockCount();
                        
                        db.collection("farms")
                            .document(String.valueOf(paymentDto.getFarmId()))
                            .update("releasedStocks", newReleasedStocks)
                            .addOnSuccessListener(successListener)
                            .addOnFailureListener(failureListener);
                        
                        // Add stock purchase record
                        Map<String, Object> stockPurchase = new HashMap<>();
                        stockPurchase.put("userId", paymentDto.getUserId());
                        stockPurchase.put("farmId", paymentDto.getFarmId());
                        stockPurchase.put("stockCount", paymentDto.getStockCount());
                        stockPurchase.put("price", paymentDto.getPrice());
                        stockPurchase.put("returnType", paymentDto.getReturnType());
                        stockPurchase.put("purchaseDate", new Date());
                        
                        db.collection("stockPurchases")
                            .add(stockPurchase)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "Stock purchase added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding stock purchase", e);
                                }
                            });
                    } else {
                        failureListener.onFailure(new Exception("Farm not found"));
                    }
                }
            })
            .addOnFailureListener(failureListener);
    }

    /**
     * Convert Firestore query results to TransactionDto
     */
    public static TransactionDto convertToTransactionDto(QuerySnapshot querySnapshot) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setSuccess(true);
        
        ArrayList<TransactionItem> todayList = new ArrayList<>();
        ArrayList<TransactionItem> oldList = new ArrayList<>();
        
        // Get current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        for (QueryDocumentSnapshot document : querySnapshot) {
            String farmId = document.getString("farmId");
            String type = document.getString("type");
            double amount = document.getDouble("amount");
            String formattedAmount = (type.equals("Deposit") ? "+" : "-") + String.format("%,.2f", amount) + " RS";
            String formattedDate = document.getString("formattedDate");
            Date timestamp = document.getDate("timestamp");
            
            // Get farm name
            db.collection("farms")
                .document(farmId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String farmName = documentSnapshot.getString("farmName");
                        TransactionItem item = new TransactionItem(farmName, type, formattedAmount, formattedDate);
                        
                        // Check if transaction is from today
                        String transactionDate = sdf.format(timestamp);
                        if (transactionDate.equals(today)) {
                            todayList.add(item);
                        } else {
                            oldList.add(item);
                        }
                    }
                });
        }
        
        transactionDto.setTodayList(todayList);
        transactionDto.setOldList(oldList);
        
        return transactionDto;
    }
}