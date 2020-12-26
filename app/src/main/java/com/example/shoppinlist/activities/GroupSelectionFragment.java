package com.example.shoppinlist.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinlist.R;
import com.example.shoppinlist.adapters.GroupSelectionAdapter;
import com.example.shoppinlist.models.Category;
import com.example.shoppinlist.models.Group;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.example.shoppinlist.models.User;
import com.example.shoppinlist.models.UserGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupSelectionFragment extends Fragment {
    private static final String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;
    RecyclerView groupRecyclerView;
    ArrayList<UserGroup> userGroups;
    ListenerRegistration listenerRegistration;
    boolean isRemoveRegistration = false;
    User user ;
    private Dialog dialogMain;
    String user_id;
    ImageView addSpaceImageView;
    public GroupSelectionFragment(){
        super(R.layout.fragment_choose_space);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userGroups = new ArrayList<>();
        groupRecyclerView = view.findViewById(R.id.group_recyclerview);
        addSpaceImageView = view.findViewById(R.id.add_space_icon);
        user_id = requireArguments().getString(ShoppinListConstants.CONSTANT_USERS_STRING);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        addSpaceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddGroupPopup(view);
            }
        });
        getGroups();
    }

    void getGroups(){
        Log.d(TAG,"Getting groups..");
        if (isRemoveRegistration) {
            listenerRegistration.remove();
            isRemoveRegistration = false;
        }
        userGroups.clear();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference groupUserReference = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_USER_GROUP_STRING);
        groupUserReference
                .whereEqualTo(ShoppinListConstants.CONSTANT_USER_ID_STRING, user_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        LoginActivity.progressBar.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            UserGroup userGroup = doc.toObject(UserGroup.class);
                            userGroup.setId(doc.getId());
                            Log.d(TAG, "GroupId: " + userGroup.getGroup_id() + " groupName: " +userGroup.getName());
                            userGroups.add(userGroup);
                        }
                        if(userGroups.size() > 0)
                            showGroups();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LoginActivity.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to fetch groups.", Toast.LENGTH_SHORT).show();
                LoginActivity.progressBar.setVisibility(View.GONE);
            }
        });
    }

    void showAddGroupPopup(View view) {
        Log.d(TAG, "showing add group popup ... ");
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        dialogMain = new Dialog(getContext(),R.style.MaterialDialogSheet);
        dialogMain.setContentView(popupView);
        dialogMain.setCancelable(true);
        dialogMain.getWindow().setLayout(width,height);
        dialogMain.getWindow().setGravity(Gravity.CENTER);
        dialogMain.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialogMain.show();

        final EditText editText = popupView.findViewById(R.id.edittext);
        editText.setHint("Space Id");
        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
        categoryListLayout.setVisibility(View.VISIBLE);
        popupView.findViewById(R.id.popup_category_spinner).setVisibility(View.INVISIBLE);
        ImageView addGroup = popupView.findViewById(R.id.craate_category_btn);

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                dialogMain.dismiss();
//                showCategoryPopupWindow(view2);
            }
        });
//        ArrayList<String> categoryArrayList = new ArrayList<>();
//        for(Category cat : categories){
//            categoryArrayList.add(cat.getName());
//        }

//        if(!categories.isEmpty())
//            selectedCategoryId = categories.get(0).getId();

        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().equals(""))
                    isGroupExists(editText.getText().toString());
            }

        });


    }

//    private void showCategoryPopupWindow(final View view) {
//        Log.d(TAG, "showing category for adding task...");
//        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
//        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
//        int width = LinearLayout.LayoutParams.MATCH_PARENT;
//        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//
//        final Dialog dialog = new Dialog(getContext(),R.style.MaterialDialogSheet);
//        dialog.setContentView(popupView);
//        dialog.setCancelable(true);
//        dialog.getWindow().setLayout(width,height);
//        dialog.getWindow().setGravity(Gravity.CENTER);
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        dialog.show();
//
//        final EditText editText = popupView.findViewById(R.id.edittext);
//        editText.setHint("Add category");
//        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
//        categoryListLayout.setVisibility(View.GONE);
//        Button create = popupView.findViewById(R.id.create);
//        create.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Log.d(TAG, "onClick: creating category ... ");
//                Map<String,String> categoryMap = new HashMap<>();
//                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));
//                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING,String.valueOf(categories.size()+1));
//                categoryMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING,ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);
//                categoryMap.put(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, groupId);
//
//                if(!String.valueOf(editText.getText()).replace(" ","").equals("")) {
//                    FirebaseFirestore addCategoryFirestore = FirebaseFirestore.getInstance();
//                    CollectionReference collectionReference = addCategoryFirestore.collection(ShoppinListConstants.CONSTANT_COLLECTION_CATEGORY_STRING);
//                    collectionReference.add(categoryMap);
//                    listenerRegistration =collectionReference.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                            if(isInternetAvailable()){
//                                Toast.makeText(getApplicationContext(), "Category created Successfully", Toast.LENGTH_SHORT).show();
//                            }else{
//                                Toast.makeText(getApplicationContext(), "Category created. Sync Failed", Toast.LENGTH_SHORT).show();
//                            }
//                            dialog.dismiss();
//                            getCategoriesFromDB(true);
//                        }
//                    });
//                }
//
//            }
//        });
//    }

    void showGroups(){
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(ShoppinListConstants.CONSTANT_SHOPPING_LIST_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, userGroups.get(0).getGroup_id());
        editor.apply();
        GroupSelectionAdapter groupSelectionAdapter = new GroupSelectionAdapter(userGroups,getActivity().getBaseContext(),getActivity());
        groupRecyclerView.setAdapter(groupSelectionAdapter);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void isGroupExists(String groupId) {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        Log.d(TAG, "isGroupExists: checking group ...");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference groupCollection = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_GROUPS_STRING);
        DocumentReference groupRef = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_GROUPS_STRING).document(groupId);
        listenerRegistration = groupRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                isRemoveRegistration = true;
                if (value.getMetadata().isFromCache()) {
                    Log.d(TAG, "onEvent: Data from cache !! ");
                }
                if (value.getData() == null) {
                    Log.i(TAG, "onEvent: No results");
                    Toast.makeText(getContext(), "Space doesn't Exist. Please Create incase you don't have one.", Toast.LENGTH_SHORT).show();
                }else {
                    Log.i(TAG, "onEvent: ");
                    Log.d(TAG, "onSuccess: group exists.. GroupId: "+value.getId()+" GroupName:"+value.getString(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING));
                    createUerGroup(value.getId(),value.getString(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING));
                }
            }
        });
    }

    private void createUerGroup(String groupId,String groupName) {
        HashMap<String, String> userGroupHashmap = new HashMap<>();
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_USER_ID_STRING, user_id);
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, groupId);
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, groupName);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_USER_GROUP_STRING);
        collectionReference
                .add(userGroupHashmap);
        listenerRegistration = collectionReference
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        isRemoveRegistration=true;
                        if (value.getMetadata().isFromCache()) {
                            Log.i(TAG, "onEvent: IsFromCache is True");
                        }else{
                            Log.i(TAG, "onEvent: Usergroup added successfully... ");
                            dialogMain.dismiss();
                            getGroups();
                        }
                    }
                });
    }

}
