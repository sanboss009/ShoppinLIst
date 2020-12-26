package com.example.shoppinlist.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoppinlist.R;
import com.example.shoppinlist.adapters.CategoryRecyclerViewAdapter;
import com.example.shoppinlist.models.Category;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
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

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView ;
    CategoryRecyclerViewAdapter categoryRecyclerViewAdapter;
    ArrayList<Category> categories;
    FloatingActionButton createItemMain;
    public static String selectedCategoryId;
    private Spinner categoryListSpinner;
    ProgressBar mainProgressBar ;
    private Dialog dialogMain;
    private ArrayAdapter spinnerAdapter;
    private ListenerRegistration listenerRegistration;
    String groupId ;
    String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;
    boolean isUserLoggedIn = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.category_recycler_view);
        createItemMain = findViewById(R.id.main_fab);
        mainProgressBar = findViewById(R.id.progressBar_main);
        categories = new ArrayList<>();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().getString(ShoppinListConstants.CONSTANT_GROUP_ID_STRING).equals("")) {
                    isUserLoggedIn = false;
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    this.finish();
                }
                groupId = intent.getExtras().getString(ShoppinListConstants.CONSTANT_GROUP_ID_STRING);
            }
        }
        FirebaseApp.initializeApp(getApplicationContext());
        listenerRegistration = null;
        if(isUserLoggedIn)
            getCategoriesFromDB(false);

        createItemMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateItemPopup(view);
            }
        });


    }



    private void getCategoriesFromDB(final boolean displayCreateItemPopup) {
        Log.d(TAG, "getting categories ...");
        if(listenerRegistration != null){
            listenerRegistration.remove();
        }
        mainProgressBar.setVisibility(View.VISIBLE);
        categories.clear();
        FirebaseFirestore getCatFirestore = FirebaseFirestore.getInstance();
        getCatFirestore.collection(getString(R.string.collection_category))
                .whereEqualTo(ShoppinListConstants.CONSTANT_GROUP_ID_STRING,groupId)
                .orderBy(getString(R.string.priority), Query.Direction.ASCENDING)
                .get();
        getCatFirestore.collection(getString(R.string.collection_category))
                .whereEqualTo(ShoppinListConstants.CONSTANT_GROUP_ID_STRING,groupId)
                .orderBy(getString(R.string.priority), Query.Direction.ASCENDING)
                .addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        categories.clear();
                        if (value != null) {
                            for (DocumentChange change : value.getDocumentChanges()) {
                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    Log.d("TAG", "Change Type:" + change.getDocument().getString(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING));
                                    Category category = new Category();
                                    category.setId(change.getDocument().getId());
                                    category.setName(change.getDocument().getString(getString(R.string.name)));
                                    category.setPriority(change.getDocument().getString(getString(R.string.priority)));
                                    category.setStatus(change.getDocument().getString(getString(R.string.status)));
                                    categories.add(category);
                                }
                            }
                            displayCategories();
                            if(displayCreateItemPopup){
                                createItemMain.performClick();
                            }
                        }
                    }
                });
    }



    private void displayCategories() {
        Log.d(TAG, "displaying Categories ... ");
        categoryRecyclerViewAdapter = new CategoryRecyclerViewAdapter(categories,getApplicationContext());
        recyclerView.setAdapter(categoryRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainProgressBar.setVisibility(View.GONE);
    }

    private void showCreateItemPopup(final View view) {
        Log.d(TAG, "showing create item popup ... ");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        dialogMain = new Dialog(MainActivity.this,R.style.MaterialDialogSheet);
        dialogMain.setContentView(popupView);
        dialogMain.setCancelable(true);
        dialogMain.getWindow().setLayout(width,height);
        dialogMain.getWindow().setGravity(Gravity.CENTER);
        dialogMain.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialogMain.show();

        final EditText editText = popupView.findViewById(R.id.edittext);
        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
        categoryListLayout.setVisibility(View.VISIBLE);
        categoryListSpinner = popupView.findViewById(R.id.popup_category_spinner);
        ImageView addCategory = popupView.findViewById(R.id.craate_category_btn);

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                dialogMain.dismiss();
                showCategoryPopupWindow(view);
            }
        });
        ArrayList<String> categoryArrayList = new ArrayList<>();
        for(Category cat : categories){
            categoryArrayList.add(cat.getName());
        }

        spinnerAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,categoryArrayList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryListSpinner.setAdapter(spinnerAdapter);

        if(!categories.isEmpty())
            selectedCategoryId = categories.get(0).getId();


        categoryListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategoryId = categories.get(i).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                Map<String,String> itemMap = new HashMap<>();
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING,"0");
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_CATEGORY_STRING,selectedCategoryId);
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING,ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);

                if(!String.valueOf(editText.getText()).replace(" ","").equals("")) {
                    FirebaseFirestore addItemFirestore = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference =addItemFirestore.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING);
                    collectionReference.add(itemMap);
                    listenerRegistration = collectionReference.addSnapshotListener(MainActivity.this,new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(isInternetAvailable()){
                                Toast.makeText(getApplicationContext(), "Item created Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "Item created. Sync Failed", Toast.LENGTH_SHORT).show();
                            }
                            dialogMain.dismiss();
                            getCategoriesFromDB(false);
                        }

                    });


                }

            }
        });
    }

    private void showCategoryPopupWindow(final View view) {
        Log.d(TAG, "showing category for adding task...");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final Dialog dialog = new Dialog(MainActivity.this,R.style.MaterialDialogSheet);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(width,height);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        final EditText editText = popupView.findViewById(R.id.edittext);
        editText.setHint("Add category");
        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
        categoryListLayout.setVisibility(View.GONE);
        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: creating category ... ");
                Map<String,String> categoryMap = new HashMap<>();
                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));
                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING,String.valueOf(categories.size()+1));
                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING,ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);
                categoryMap.put(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, groupId);

                if(!String.valueOf(editText.getText()).replace(" ","").equals("")) {
                    FirebaseFirestore addCategoryFirestore = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = addCategoryFirestore.collection(ShoppinListConstants.CONSTANT_COLLECTION_CATEGORY_STRING);
                    collectionReference.add(categoryMap);
                    listenerRegistration =collectionReference.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(isInternetAvailable()){
                                        Toast.makeText(getApplicationContext(), "Category created Successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Category created. Sync Failed", Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                    getCategoriesFromDB(true);
                                }
                            });
                }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_btn:
                showUserOPtionPopup();


        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserOPtionPopup() {
        Log.d(TAG, "showing user option menu...");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_user_options,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final Dialog dialog = new Dialog(MainActivity.this,R.style.MaterialDialogSheet);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(width,height);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        MaterialButton switchSpace = popupView.findViewById(R.id.switch_space_button);
        final MaterialButton logoutButton = popupView.findViewById(R.id.logout_button);

        switchSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent groupSelectionFragment = new Intent(getApplicationContext(),LoginActivity.class);
                groupSelectionFragment.putExtra(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA, "switch_space");
                groupSelectionFragment.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(groupSelectionFragment);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogOutpopup();
                dialog.dismiss();
            }
        });
    }

    void showLogOutpopup(){
        Log.d(TAG, "showLogOutpopup: Showing log out pop up ...");
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder
                .setTitle("Confirm")
                .setMessage("Are you sure you want to Logout?")
                .setCancelable(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Logoff Dismissed");
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(ShoppinListConstants.CONSTANT_SHOPPING_LIST_SHARED_PREF, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        dialogInterface.dismiss();
                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}