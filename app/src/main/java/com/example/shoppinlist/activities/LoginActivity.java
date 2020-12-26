package com.example.shoppinlist.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.shoppinlist.R;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.example.shoppinlist.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;

public class LoginActivity extends AppCompatActivity {
    MaterialButton loginBtn;
    MaterialButton signUp;
    EditText emailEditText;
    EditText passwordEditText;
    FragmentContainerView fragmentContainerView;
    String TAG = ShoppinListConstants.CONSTANT_LOG_TAG;
    private ListenerRegistration listenerRegistration;
    boolean isRemoveRegistration = false;
    boolean isShowGroupSelect =false;
    static ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(getApplicationContext());
        loginBtn = findViewById(R.id.login_button);
        signUp = findViewById(R.id.sign_up_btn_login);
        fragmentContainerView = findViewById(R.id.fragment_view);
        fragmentContainerView.setVisibility(View.GONE);
        emailEditText = findViewById(R.id.email_editext);
        passwordEditText = findViewById(R.id.password_editext);
        progressBar = findViewById(R.id.progressBar_login);
        Intent intent = getIntent();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(ShoppinListConstants.CONSTANT_SHOPPING_LIST_SHARED_PREF, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(ShoppinListConstants.CONSTANT_IS_USER_LOGGED_IN, false)) {
            if (intent.getExtras() != null) {
                if (intent.getExtras().getString(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA) != null) {
                    if(intent.getExtras().getString(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA).equals("switch_space")){
                        String loggedUserId = sharedPreferences.getString(ShoppinListConstants.CONSTANT_USER_ID_STRING, "");
                        if (!loggedUserId.equals("")) {
                            intent.getExtras().clear();
                            isShowGroupSelect=true;
                            showChooseSpaceFragment(loggedUserId);
                        }
                    }
                }
            }
            if (!isShowGroupSelect) {
                String groupId = sharedPreferences.getString(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, "");
                Intent homePageIntent = new Intent(this, MainActivity.class);
                homePageIntent.putExtra(ShoppinListConstants.CONSTANT_GROUP_ID_STRING, groupId);
                startActivity(homePageIntent);
                this.finish();
            }
        }

        if (!isShowGroupSelect && intent.getExtras() != null) {
            if (intent.getExtras().getString(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA) != null) {
                if (intent.getExtras().getString(ShoppinListConstants.CONSTANT_STRING_LOGIN_EXTRA).equals("user_register")) {
                    intent.getExtras().clear();
                    Toast.makeText(this,"Successfully Registered! Please Sign in ",Toast.LENGTH_LONG).show();
                }
            }
        }
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(register);
            }
        });

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isInternetAvailable()) {
                    authenticateUser();
                } else {
                    Toast.makeText(LoginActivity.this, "No internet connection !!", Toast.LENGTH_LONG).show();
                }
            }});


    }

    private void authenticateUser() {
        Log.d(TAG, "Authenticating user");
        final String email = emailEditText.getEditableText().toString();
        String psw = passwordEditText.getEditableText().toString();
        if (!email.equals("")) {
            if (!psw.equals("")) {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference userCollection = firebaseFirestore.collection(ShoppinListConstants.CONSTANT_USERS_STRING);
                userCollection.whereEqualTo(ShoppinListConstants.CONSTANT_EMAIL_STRING,email)
                        .whereEqualTo(ShoppinListConstants.CONSTANT_PSW_STRING,psw)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    if (snapshot.getData().get(ShoppinListConstants.CONSTANT_EMAIL_STRING).equals(email)) {
                                        User user = (User) snapshot.toObject(User.class);
                                        user.setId(snapshot.getId());
                                        Log.d(TAG, "UserID: "+user.getId()+"\nUser email: "+user.getEmail());
                                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(ShoppinListConstants.CONSTANT_SHOPPING_LIST_SHARED_PREF, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(ShoppinListConstants.CONSTANT_EMAIL_STRING, user.getEmail());
                                        editor.putString(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, user.getName());
                                        editor.putString(ShoppinListConstants.CONSTANT_USER_ID_STRING, user.getId());
                                        editor.putBoolean(ShoppinListConstants.CONSTANT_IS_USER_LOGGED_IN, true);
                                        editor.apply();
                                        editor.commit();
                                        showChooseSpaceFragment(user.getId());
                                    }
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Something went Wrong! Please try later !", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                passwordEditText.setError("Please enter password");
            }
        }else{
            emailEditText.setError("Please enter email");
        }
    }

    void showChooseSpaceFragment(String userId){
        progressBar.setVisibility(View.VISIBLE);
        fragmentContainerView.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString(ShoppinListConstants.CONSTANT_USERS_STRING, userId);
        FragmentManager spaceManager = getSupportFragmentManager();
        if (isShowGroupSelect) {
            spaceManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_view,GroupSelectionFragment.class,bundle)
                    .commit();
        }else{
            spaceManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_view,GroupSelectionFragment.class,bundle).addToBackStack("tag")
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(fragmentContainerView.getVisibility() == View.VISIBLE){
            if (!isShowGroupSelect)
            fragmentContainerView.setVisibility(View.GONE);
        }
        super.onBackPressed();
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