package com.example.chattingappproject.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chattingappproject.databinding.ActivitySignUpBinding;
import com.example.chattingappproject.utilities.Constants;
import com.example.chattingappproject.utilities.PreferenceMmanager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceMmanager preferenceManager;
    private String encodeImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceMmanager(getApplicationContext());
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpdetails()){
                signup();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToasts(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    // data storing to database
    private void signup(){
                loading(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
               HashMap<String, Object>user=new HashMap<>();
               user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
               user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
               user.put(Constants.KEY_PASSWORD, binding.password.getText().toString());
               user.put(Constants.KEY_IMAGE, encodeImage);
               database.collection(Constants.KEY_COLLECTION_USERS)
                       .add(user)
                       .addOnSuccessListener(documentReference -> {
                            loading(false);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                            preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                            preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                            preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);
                            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                       })
                       .addOnFailureListener(exception ->{
                            loading(false);
                            showToasts(exception.getMessage());
                       });

    }
        //image
    private String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight= bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap= Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage= registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode()== RESULT_OK){
                    if (result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        try {
                            InputStream inputStream= getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage= encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
        // input
    private Boolean isValidSignUpdetails(){
        if(encodeImage==null){
            showToasts("select profile picture ");
            return false;
        }else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToasts("Enter name");
            return false;
        }else if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToasts("Enter email");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToasts("Enter valid email");
            return false;
        }else if (binding.password.getText().toString().trim().isEmpty()){
             showToasts("Enter Password");
              return false;
        }else if (binding.inputconfirmpassword.getText().toString().trim().isEmpty()){
            showToasts("Confirm your password");
            return false;
        }else if (!binding.password.getText().toString().equals(binding.inputconfirmpassword.getText().toString())){
            showToasts("Password & confirm password must be same");
             return false;
        }else {
            return true;
        }
    }
    //loading progress bar
        private  void loading (Boolean isloading){
        if(isloading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
        }

    private class DEFAULT {
    }
}
