package com.example.shoppinlist.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppinlist.R;
import com.example.shoppinlist.models.Group;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.example.shoppinlist.models.User;
import com.example.shoppinlist.models.UserGroup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    MaterialButton create;
    MaterialButton createGroup;
    EditText confirmPasswordEdittext;
    EditText passwordEdittext;
    EditText emailEdittext;
    EditText groupidEdittext;
    TextView title;
    EditText nameEditText;
    User user;
    UserGroup userGroup;
    ListenerRegistration listenerRegistration;
    boolean isRemoveRegistration = false;
    private boolean isUserAdded;
    String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;
    private String userId;
    private String userGroupId;
    private boolean isGroupExists;
    private Group group;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.no_acc_sign_up_layout).setVisibility(View.GONE);
        findViewById(R.id.forgot_psw_layout).setVisibility(View.GONE);
        findViewById(R.id.confirm_button_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.group_id_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.create_grp_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.name_layout).setVisibility(View.VISIBLE);
        title = findViewById(R.id.textview_login);
        title.setText("Sign Up");
        create = findViewById(R.id.login_button);
        create.setText("Sign Up");
        emailEdittext = findViewById(R.id.email_editext);
        nameEditText = findViewById(R.id.name_editext);
        passwordEdittext = findViewById(R.id.password_editext);
        confirmPasswordEdittext = findViewById(R.id.confirm_password_editext);
        groupidEdittext = findViewById(R.id.group_id_editext);
        create = findViewById(R.id.login_button);
        createGroup = findViewById(R.id.creatE_grp_btn);

        user = new User();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable())
                    userSignUp();
                else
                    Toast.makeText(RegisterActivity.this, "No internet Available", Toast.LENGTH_LONG).show();
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCategoryPopupWindow(view);
            }
        });
    }

    void userSignUp() {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        Log.d(TAG, "user_sign_up: initializing.. ");
        if (validateFields()) {
            userGroupId = groupidEdittext.getText().toString();
            Log.d(TAG, "user_sign_up: Validation Success !!! ");
            isGroupExists();
        }
    }

    private void createUserGroup() {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        HashMap<String, String> userGroupHashmap = new HashMap<>();
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_USER_ID_STRING, user.getId());
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, user.getGroups().get(0).getId());
        userGroupHashmap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, user.getGroups().get(0).getName());

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
                            startLoginActivity();
                        }
                    }
                });
    }

    private void createUser() {
        if (isRemoveRegistration)
            listenerRegistration.remove();

        Log.d(TAG, "create_user: Initializing User creation...");
        isUserAdded = false;
        userGroupId = groupidEdittext.getText().toString();

        HashMap<String, String> userHashMap = new HashMap<>();
        userHashMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, nameEditText.getText().toString());
        userHashMap.put(ShoppinListConstants.CONSTANT_EMAIL_STRING, emailEdittext.getText().toString());
        userHashMap.put(ShoppinListConstants.CONSTANT_PSW_STRING, passwordEdittext.getText().toString());
        userHashMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING, ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference userCollection = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_USERS_STRING);
        userCollection.add(userHashMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        userId = documentReference.getId();
                        isUserAdded = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: User creation failed !!");
                        Toast.makeText(RegisterActivity.this, "Unknown Error ! User creation Failed!! Try again later", Toast.LENGTH_LONG).show();
                        isUserAdded = false;
                    }
                });

        listenerRegistration = userCollection.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                isRemoveRegistration = true;
                if (isUserAdded) {
                    if (value.getMetadata().isFromCache()) {
                        Toast.makeText(RegisterActivity.this, "From Cache", Toast.LENGTH_LONG).show();
                    } else {
                        for (DocumentSnapshot snapshot : value) {
                            if (snapshot.getId().equals(userId)) {
                                user = snapshot.toObject(User.class);
                                user.setId(userId);
                                ArrayList<Group> groups = new ArrayList<>();
                                groups.add(group);
                                user.setGroups(groups);
                                Log.d(TAG, "user_sign_up: User created !!! Id: " + user.getId() + " Name: " + user.getName());
                                createUserGroup();
                            }
                        }
                    }
                }
            }
        });
    }

    private void isGroupExists() {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        Log.d(TAG, "isGroupExists: checking group ...");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference groupCollection = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_GROUPS_STRING);
        DocumentReference groupRef = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_GROUPS_STRING).document(userGroupId);
        listenerRegistration = groupRef.addSnapshotListener(RegisterActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                isRemoveRegistration = true;
                if (value.getMetadata().isFromCache()) {
                    Log.d(TAG, "onEvent: Data from cache !! ");
                }
                if (value.getData() == null) {
                    Log.i(TAG, "onEvent: No results");
                    Toast.makeText(RegisterActivity.this, "Space doesn't Exist. Please Create incase you don't have one.", Toast.LENGTH_SHORT).show();
                }else {
                    Log.i(TAG, "onEvent: ");
                    group = new Group();
                    group = value.toObject(Group.class);
                    group.setId(value.getId());
                    Log.d(TAG, "onSuccess: group exists.. GroupId: "+group.getId()+" GroupName:"+group.getName());
                    createUser();
                }
            }
        });
    }

    private boolean validateFields() {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        Log.d(TAG, "validateFields: Initializing validation...");
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);

        String name = nameEditText.getText().toString();
        String email = emailEdittext.getText().toString();
        String psw = passwordEdittext.getText().toString();
        String confirmPsw = confirmPasswordEdittext.getText().toString();
        String groupid = groupidEdittext.getText().toString();

        if (!name.equals("")) {
            if (!email.equals("")) {
                Matcher matcher = pattern.matcher(email);
                if (matcher.matches()) {
                    if (!psw.equals("")) {
                        if (isValidPassword(psw)) {
                            if (!confirmPsw.equals("")) {
                                if (!groupid.equals("")) {
                                    if (psw.equals(confirmPsw)) {
                                        return true;
                                    } else {
                                        passwordEdittext.setError("Password and Confirm password doesn't match");
                                        return false;
                                    }
                                } else {
                                    groupidEdittext.setError("Please enter groupid");
                                    return false;
                                }
                            } else {
                                confirmPasswordEdittext.setError("Please confirm password");
                                return false;
                            }
                        } else {
                            passwordEdittext.setError("Password should be Alphanumeric and Special Characters");
                            return false;
                        }
                    } else {
                        passwordEdittext.setError("Please enter password");
                        return false;
                    }
                } else {
                    emailEdittext.setError("Please enter valid email");
                    return false;
                }
            } else {
                emailEdittext.setError("Please enter email");
                return false;
            }
        } else {
            nameEditText.setError("Please enter name");
            return false;
        }
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    void startLoginActivity() {
        if (isRemoveRegistration) {
            listenerRegistration.remove();
        }
        Intent login = new Intent(this, com.example.shoppinlist.activities.LoginActivity.class);
        login.putExtra(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA, "user_register");
        startActivity(login);
        finish();
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

    private void showCategoryPopupWindow(final View view) {
        Log.d(TAG, "showing create group popup...");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.create_pop_up_layout,null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final Dialog dialog = new Dialog(RegisterActivity.this,R.style.MaterialDialogSheet);
        dialog.setContentView(popupView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(width,height);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();

        final EditText editText = popupView.findViewById(R.id.edittext);
        editText.setHint("Add Space");
        LinearLayout categoryListLayout = popupView.findViewById(R.id.category_spinner_view);
        categoryListLayout.setVisibility(View.GONE);
        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: creating group ... ");
                Map<String,String> groupMap = new HashMap<>();
                groupMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));

                if(!String.valueOf(editText.getText()).replace(" ","").equals("")) {
                    FirebaseFirestore addGroupFirestore = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = addGroupFirestore.collection(ShoppinListConstants.CONSTANT_GROUPS_STRING);
                    collectionReference
                            .add(groupMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    if(isInternetAvailable()){
                                        Toast.makeText(getApplicationContext(), "Group created Successfully", Toast.LENGTH_SHORT).show();
                                        String groupId = documentReference.getId();
                                        groupidEdittext.setText(groupId);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Group created. Sync Failed", Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Group created. Sync Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}
