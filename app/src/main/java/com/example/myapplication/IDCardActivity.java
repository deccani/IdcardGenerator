package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IDCardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText Name, Designation, Company;
    private Button selectpicture, generateIDCard;
    private ImageView IDCardImage, picture;
    private Bitmap selectedImageBitmap;
    private Bitmap idCardBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card);

        Name = findViewById(R.id.name);
        Designation = findViewById(R.id.designation);
        Company = findViewById(R.id.company);
        selectpicture = findViewById(R.id.selectpicture);
        generateIDCard = findViewById(R.id.generateidcard);
        picture = findViewById(R.id.picture);
        IDCardImage = findViewById(R.id.idcardimage);

        selectpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        generateIDCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateIDCard();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
                picture.setImageBitmap(selectedImageBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateIDCard() {
        String name = Name.getText().toString();
        String designation = Designation.getText().toString();
        String company = Company.getText().toString();

        int width = 400;
        int height = 600; // Increase the height to fit photo and text
        int imageSize = 150; // Size for the photo

        idCardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(idCardBitmap);
        Paint paint = new Paint();

        // Background color
        canvas.drawColor(Color.WHITE);

        // Draw selected image if available
        if (selectedImageBitmap != null) {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedImageBitmap, imageSize, imageSize, false);
            int left = (width - imageSize) / 2;
            int top = 20;
            canvas.drawBitmap(resizedBitmap, left, top, null);
        }

        // Text color and size
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Calculate position for the text
        int textStartY = imageSize + 40; // Start drawing text below the image with some margin

        // Draw text
        canvas.drawText("Name: " + name, 20, textStartY, paint);
        canvas.drawText("Designation: " + designation, 20, textStartY + 40, paint);
        canvas.drawText("Company: " + company, 20, textStartY + 80, paint);

        // Display the generated ID card
        IDCardImage.setImageBitmap(idCardBitmap);

        // Save the generated ID card to storage
        saveIDCardToStorage(idCardBitmap, name);
    }

    private void saveIDCardToStorage(Bitmap bitmap, String userName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/certificates");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "idcard.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "ID Card saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            uploadCertificateToFirebase(file, userName, uid);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save ID Card", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCertificateToFirebase(File file, String userName, String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference certificateRef = storageRef.child("certificates/" + uid + "/" + userName + ".png");

        Uri fileUri = Uri.fromFile(file);
        UploadTask uploadTask = certificateRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "ID Card uploaded successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload ID Card", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(IDCardActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
