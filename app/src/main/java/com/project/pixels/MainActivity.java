package com.project.pixels;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgPreView;
    private Button selectBtn, downloadBtn;
    private ProgressBar progressBar;

    private Bitmap originalBitmap;
    private int[] pixelsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPreView = findViewById(R.id.imgPreView);
        selectBtn = findViewById(R.id.selectBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        progressBar = findViewById(R.id.progressBar);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap != null) {
                    saveImage(originalBitmap);
                } else {
                    showToast("No image selected");
                }
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                    if (bitmap != null) {
                        displayImage(bitmap);
                        processImage(bitmap);
                    } else {
                        showToast("Error loading image");
                    }
                } catch (Exception e) {
                    showToast("Error loading image: " + e.getMessage());
                }
            } else {
                showToast("No image selected");
            }
        }
    }

    private void displayImage(Bitmap bitmap) {
        imgPreView.setVisibility(View.VISIBLE);
        imgPreView.setImageBitmap(bitmap);
        originalBitmap = bitmap;
    }

    private void processImage(Bitmap bitmap) {
        new ProcessImageTask().execute(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private class ProcessImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            pixelsArray = new int[width * height];
            bitmap.getPixels(pixelsArray, 0, width, 0, 0, width, height);

            int length = pixelsArray.length;
            int start = 0;
            int end = length - 1;

            // Generate random values based on pixel data
            Random random = new Random("Siam".hashCode());

            int[] numbers = new int[length];
            boolean[] used = new boolean[length];

            for (int i = 0; i < length; ) {
                int randomNumber = random.nextInt(end - start + 1);

                if (used[i] && used[randomNumber]) {
                    i++;
                    continue;
                }
                if (!used[i] && !used[randomNumber]) {
                    numbers[i] = randomNumber;
                    numbers[randomNumber] = i;

                    used[randomNumber] = true;
                    used[i] = true;
                    i++;
                }
            }

            for (int i = 0; i < length; i++) {
                numbers[i] = pixelsArray[numbers[i]];
            }


            // Create a new bitmap with modified pixel values
            Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            newBitmap.setPixels(numbers, 0, width, 0, 0, width, height);
            return newBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imgPreView.setImageBitmap(result);
            originalBitmap = result;
            progressBar.setVisibility(View.GONE);
            downloadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void saveImage(Bitmap bitmap) {
        // Save bitmap as image file
        String filename = "pixel_image.jpg";
        File file = new File(getExternalFilesDir(null), filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

            // Add image to gallery
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), filename, null);

            // Show a toast message
            showToast("Image saved to Gallery");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
