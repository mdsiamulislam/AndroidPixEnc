package com.project.pixels;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button selectBtn,downloadBtn;
    ImageView imgPreView;
    private static final int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectBtn = findViewById(R.id.selectBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        imgPreView = findViewById(R.id.imgPreView);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);


            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    // Load the image as a bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                        if (bitmap != null) {
                            // Display the bitmap in the ImageView
                            imgPreView.setVisibility(View.VISIBLE);
                            imgPreView.setImageBitmap(bitmap);
                            downloadBtn.setVisibility(View.VISIBLE);
                            downloadBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Download Image");
                                    builder.setMessage("If you want to download this image . You Have to give a password for this image.");
                                    final EditText input = new EditText(MainActivity.this);
                                    builder.setView(input);

                                    builder.setPositiveButton("Yes",
                                    new  DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String password = input.getText().toString();
                                            if (MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "images", "Pixels Image") != null) {
                                                Toast.makeText(MainActivity.this, "Image saved password is" + password, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    builder.setNegativeButton("No", null);
                                    builder.show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}