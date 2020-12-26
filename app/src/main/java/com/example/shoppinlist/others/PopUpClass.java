package com.example.shoppinlist.others;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.shoppinlist.R;
import com.example.shoppinlist.activities.ItemDetailView;
import com.example.shoppinlist.models.Item;
import com.example.shoppinlist.models.ShoppinListConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

public class PopUpClass  {
    String categoryId ;
    Context mContext;
    public PopUpClass(String categoryId,Context mcontext) {
        this.categoryId = categoryId;
        this.mContext = mcontext;
    }

    public void showPopupWindow(final View view) {
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_pop_up_layout, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);


        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.END, 0, 0);

        //Initialize the elements of our window, install the handler

        final EditText editText = popupView.findViewById(R.id.edittext);

        Button create = popupView.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                Map<String,String> itemMap = new HashMap<>();
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_NAME_STRING, String.valueOf(editText.getText()));
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_PRIORITY_STRING,"0");
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_CATEGORY_STRING,categoryId);
                itemMap.put(ShoppinListConstants.CONSTANT_ITEM_STATUS_STRING,ShoppinListConstants.CONSTANT_ITEM_STATUS_ACTIVE_STRING);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(ShoppinListConstants.CONSTANT_COLLECTION_ITEM_STRING)
                        .add(itemMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(mContext,"Item created",Toast.LENGTH_SHORT).show();
                                popupWindow.dismiss();
//                                Intent i = new Intent(mContext,ItemDetailView.class);
//                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                i.putExtra("collectionId",categoryId);
//                                mContext.startActivity(i);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(mContext,"Item not created",Toast.LENGTH_SHORT).show();

                            }
                        });

            }
        });



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return false;
            }
        });
    }
}
