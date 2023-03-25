package com.example.chattingappproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chattingappproject.databinding.ActivitySigninactivityBinding;
import com.example.chattingappproject.utilities.Constants;
import com.example.chattingappproject.utilities.PreferenceMmanager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class signinactivity extends AppCompatActivity {

    private ActivitySigninactivityBinding binding;
    private PreferenceMmanager preferenceMmanager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceMmanager = new PreferenceMmanager(getApplicationContext());
        if (preferenceMmanager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySigninactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreatedNewAccount.setOnClickListener(v ->
               startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
                binding.buttonSignin.setOnClickListener(v -> {
                    if (isValidSignInDetails()){
                        signIn();
                    }
                });
    }
    //check email and pass is correct
    private void signIn(){
        loading(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&& task.getResult()!=null
                    && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot= task.getResult().getDocuments().get(0);
                        preferenceMmanager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceMmanager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceMmanager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceMmanager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });

    }
    //progress bar of sign in page
    private void loading(Boolean isloading){
        if (isloading){
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.progresBar.setVisibility(View.VISIBLE);
        }else{
            binding.progresBar.setVisibility(View.INVISIBLE);
            binding.buttonSignin.setVisibility(View.VISIBLE);
        }
    }
    private void showToast(String message ){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
    //input email password
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter Valid Email");
            return false;
        }else if (binding.password.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }else {
            return  true;
        }
    }


}