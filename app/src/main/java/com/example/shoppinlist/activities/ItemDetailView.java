package com.example.shoppinlist.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinlist.R;
import com.example.shoppinlist.adapters.ItemRecyclerViewAdapter;
import com.example.shoppinlist.models.Item;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.example.shoppinlist.others.PopUpClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemDetailView extends AppCompatActivity {

    RecyclerView itemToDoRecyclerView;
    RecyclerView itemCompletedRecyclerView;
    String categoryId ;
    HashMap<Integer,Item> items;
    String CONSTANT_COMPLETED_STATUS = "Completed";
    String CONSTANT_ACTIVE_STATUS = "Active";
    FloatingActionButton createItemButton;
    public ItemRecyclerViewAdapter activeItemRecyclerViewAdapter;
    public ItemRecyclerViewAdapter completedItemRecyclerViewAdapter;
    ProgressBar itemVIewProgressbar;
    TextView completedTextView;
    private ListenerRegistration listenerRegistration;
    boolean isRemoveListener = false;
    String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_view);
        setTitle("ITem view");
        Intent intent = getIntent();
        categoryId = intent.getExtras().getString("collectionId");
        String categoryName = intent.getExtras().getString("categoryName");
        Log.d(TAG,"CategoryId: "+categoryId+" CategoryName: "+categoryName);
        setTitle(categoryName);
        itemCompletedRecyclerView = findViewById(R.id.completd_item_recycler_view);
        itemToDoRecyclerView = findViewById(R.id.to_do_item_recycler_view);
        itemVIewProgressbar = findViewById(R.id.progressBar_item_Activity);
        completedTextView = findViewById(R.id.text_view_completed);
        createItemButton = findViewById(R.id.item_fab);

        items = new HashMap<Integer, Item>();

        completedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemCompletedRecyclerView.getVisibility() == View.VISIBLE){
                    itemCompletedRecyclerView.setVisibility(View.GONE);
                    completedTextView.setText("Completed - Tap to Show items");
                }else{
                    itemCompletedRecyclerView.setVisibility(View.VISIBLE);
                    completedTextView.setText("Completed - Tap to Hide items");

                }
            }
        });

        createItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindowCreateItem(view);
            }
        });
        getItemsFromDB();

    }


    private void showPopupWindowCreateItem(View view) {
        Log.d(TAG,"Showing PopupWindow to Create Item");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final Dialog dialog = new Dialog(ItemDetailView.this,R.style.MaterialDialogSheet);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(width,height);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        final EditText editText = popupView.findViewById(R.id.edittext);
        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
        categoryListLayout.setVisibility(View.GONE);
        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Creating item: "+String.valueOf(editText.getText()));
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING, ""+items.size()+1);
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_CATEGORY_STRING, categoryId);
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING, ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);

                if (!String.valueOf(editText.getText()).replace(" ", "").equals("")) {
                    FirebaseFirestore addItemFirestoreItemDetail = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = addItemFirestoreItemDetail.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING);
                    collectionReference.add(itemMap);
                    listenerRegistration =addItemFirestoreItemDetail.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING).
                            addSnapshotListener(ItemDetailView.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            isRemoveListener=true;
                            Log.d(TAG,"Item Created. Data Fetching from ... : "+(value.getMetadata().isFromCache() ? "local cache":"Server"));
                            if (isInternetAvailable()) {
                                Toast.makeText(getApplicationContext(), "Item created Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Item created. Sync Failed", Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();
                            getItemsFromDB();
                        }
                    });
                }
            }
        });
    }

    public void getItemsFromDB() {
        Log.d(TAG, "getItemsFromDB: Fetching items for groupid: "+categoryId+" ...");
        if(isRemoveListener) {
            listenerRegistration.remove();
            isRemoveListener = false;
        }
        itemVIewProgressbar.setVisibility(View.VISIBLE);
        FirebaseFirestore getItemITemDetialsFS = FirebaseFirestore.getInstance();
        CollectionReference collectionReference =getItemITemDetialsFS.collection(getString(R.string.collection_item));
                collectionReference
                        .whereEqualTo(getString(R.string.category),categoryId)
                        .orderBy(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Log.d(TAG ,"Getting item from ... "+(queryDocumentSnapshots.getMetadata().isFromCache() ? "local cache":"Server"));
                                items.clear();
                                int i = 0;
                                for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()){
//                                    if(change.getType() == DocumentChange.Type.ADDED){
                                        Log.d(TAG, "Adding to items list item with id="+change.getDocument().getId() + " with name= " + change.getDocument().getString(getString(R.string.name)));
                                        Item item = new Item();
                                        item.setId(change.getDocument().getId());
                                        item.setName(change.getDocument().getString(getString(R.string.name)));
                                        item.setPriority(change.getDocument().getString(getString(R.string.priority)));
                                        item.setCategory(change.getDocument().getString(getString(R.string.category)));
                                        item.setStatus(change.getDocument().getString(getString(R.string.status)));
                                        items.put(i++,item);
//                                    }
                                }
                                displayItems();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, " Failure !! " );
                            }
                        });
    }

    private void displayItems() {
        Log.d(TAG,"Displaying Items...");
        ArrayList<Item> completedItems = new ArrayList<>();
        ArrayList<Item> activeItems = new ArrayList<>();
        for (Map.Entry<Integer,Item> item:items.entrySet()){
            if(item.getValue().getStatus().equals(CONSTANT_ACTIVE_STATUS)){
                activeItems.add(item.getValue());
            }
            else if(item.getValue().getStatus().equals(CONSTANT_COMPLETED_STATUS)){
                completedItems.add(item.getValue());
            }
        }
        activeItemRecyclerViewAdapter = new ItemRecyclerViewAdapter(activeItems,this);
        itemToDoRecyclerView.setAdapter(activeItemRecyclerViewAdapter);
        itemToDoRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        completedItemRecyclerViewAdapter = new ItemRecyclerViewAdapter(completedItems,this);
        itemCompletedRecyclerView.setAdapter(completedItemRecyclerViewAdapter);
        itemCompletedRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        itemVIewProgressbar.setVisibility(View.GONE);
    }

    public void updateStatus (final Item item){
        Log.d(TAG, "Updating status of item " + item.getName());
        FirebaseFirestore updateITemFSItemDetails = FirebaseFirestore.getInstance();
        DocumentReference itemRef = updateITemFSItemDetails.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING).document(item.getId());
        itemRef.update(getString(R.string.status),item.getStatus());
        listenerRegistration = itemRef.addSnapshotListener(ItemDetailView.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d(TAG,"Updated item "+item.getName()+" in "+(value.getMetadata().isFromCache() ? "local cache":"Server"));
                isRemoveListener = true;
                if(!isInternetAvailable()){
                    Toast.makeText(getApplicationContext(),"Sync Failed.",Toast.LENGTH_LONG).show();
                }
                getItemsFromDB();
            }
        });

    }

    public void  deleteItem(Item item){
        Log.d(TAG, "Deleting item " + item.getName());
        FirebaseFirestore deletItemFirestore = FirebaseFirestore.getInstance();
        DocumentReference itemRef = deletItemFirestore.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING).document(item.getId());
        itemRef.delete();
        listenerRegistration = itemRef.addSnapshotListener(ItemDetailView.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d(TAG,"item deleted from "+(value.getMetadata().isFromCache() ? "local cache":"Server"));
                isRemoveListener = true;
                if(isInternetAvailable()){
                    Toast.makeText(getApplicationContext(),"Item Deleted Successfully",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Item Deleted. Sync Failed !",Toast.LENGTH_SHORT).show();
                }
                getItemsFromDB();
            }
        });
    }
    public boolean isInternetAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null) return false;
            NetworkInfo.State network = info.getState();
            return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);

        } catch (Exception e) {
            return false;
        }
    }
}
