package com.example.androidfacebook.signup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.Manifest;
import com.example.androidfacebook.R;
import com.example.androidfacebook.entities.DataHolder;
import com.example.androidfacebook.entities.User;
import com.example.androidfacebook.login.Login;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SignUp extends AppCompatActivity {
    private EditText username;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private EditText password;
    private EditText confirmPassword;
    private EditText displayName;
    private byte[] selectedImageByteArray; // Variable to hold the selected image's byte array
    // Declare two ActivityResultLaunchers for picking from gallery and capturing from camera
    // Declare an ActivityResultLauncher for picking an image from gallery
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    handleImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    // Declare an ActivityResultLauncher for capturing an image from camera
    private final ActivityResultLauncher<Void> mCaptureImage = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
            result -> {
                if (result != null) {
                    handleImage(result);
                }
            });
    // Method to handle the image
    private void handleImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        selectedImageByteArray = stream.toByteArray();
    }


    @SuppressLint("WrongThread")
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Get the user list from the DataHolder
        List<User> userList= DataHolder.getInstance().getUserList();
        username = findViewById(R.id.textView3);
        password = findViewById(R.id.editTextTextPassword2);
        confirmPassword = findViewById(R.id.editTextTextPassword4);
        displayName = findViewById(R.id.editTextText2);
        Button btnGoBack = findViewById(R.id.btnGoBack);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        Button btnUploadImage = findViewById(R.id.btnSelectPhoto);
        // Go back to the login page
        btnGoBack.setOnClickListener(v -> {
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        });
        btnUploadImage.setOnClickListener(v -> {
            // Check for camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                // Permission has already been granted
                new AlertDialog.Builder(this)
                        .setTitle("Select Image")
                        .setItems(new String[]{"From Gallery", "From Camera"}, (dialog, which) -> {
                            if (which == 0) {
                                // From Gallery
                                mGetContent.launch("image/*");
                            } else {
                                // From Camera
                                mCaptureImage.launch(null);
                            }
                        })
                        .show();
            }
        });

        // Sign up the user
        btnSignUp.setOnClickListener(v -> {
            String usernameStr = username.getText().toString();
            String passwordStr = password.getText().toString();
            String confirmPasswordStr = confirmPassword.getText().toString();
            String displayNameStr = displayName.getText().toString();
            // Check if any of the fields are empty
            if (usernameStr.isEmpty() || passwordStr.isEmpty() || confirmPasswordStr.isEmpty() || displayNameStr.isEmpty() || selectedImageByteArray == null) {
                // show error message.
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the username already exists
            for (User user : userList){
                if(usernameStr.equals(user.getUsername())){
                    Toast.makeText(this,"User already exists",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Check if the password meets the criteria
            String passwordPattern = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
            if (!passwordStr.matches(passwordPattern)) {
                Toast.makeText(this, "Password must be at least 8 characters long," +
                        " include a capital letter and a special character",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if the passwords and confirm password match
            if (!passwordStr.equals(confirmPasswordStr)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }


            // sign up the user.
            User newU = new User(usernameStr,passwordStr,displayNameStr,selectedImageByteArray);
            // Add the new user to the user list
            userList.add(newU);
            Intent i = new Intent(this, Login.class);
            // update the user list in the DataHolder
            DataHolder.getInstance().setUserList(userList);
            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
            startActivity(i);

        });
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }
}