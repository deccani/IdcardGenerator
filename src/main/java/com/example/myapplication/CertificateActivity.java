package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;

public class CertificateActivity extends AppCompatActivity {

    private ImageView certificateImageView;
    private EditText usernameEditText;
    private Button generateButton;
    private int selectedTemplate = R.drawable.template1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        certificateImageView = findViewById(R.id.certificateImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        generateButton = findViewById(R.id.generateButton);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateCertificate();
            }
        });
    }

    public void selectTemplate(View view) {
        switch (view.getId()) {
            case R.id.template1ImageView:
                selectedTemplate = R.drawable.template1;
                break;
            case R.id.template2ImageView:
                selectedTemplate = R.drawable.template2;
                break;
            case R.id.template3ImageView:
                selectedTemplate = R.drawable.template3;
                break;
        }
        Toast.makeText(this, "Template selected", Toast.LENGTH_SHORT).show();
    }

    private void generateCertificate() {
        // Retrieve selected username
        String userName = usernameEditText.getText().toString().trim();

        // Check if user has entered a name
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load the selected template bitmap
        Bitmap templateBitmap = BitmapFactory.decodeResource(getResources(), selectedTemplate);
        int templateWidth = templateBitmap.getWidth();
        int templateHeight = templateBitmap.getHeight();

        // Create a new bitmap with the same dimensions as the template
        Bitmap certificateBitmap = Bitmap.createBitmap(templateWidth, templateHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(certificateBitmap);
        canvas.drawBitmap(templateBitmap, 0, 0, null);

        // Customize certificate text and design
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        // Set initial text size and adjust based on template size
        float textSize = templateWidth / 30f;  // Adjust this value to change text size
        paint.setTextSize(textSize);

        // Measure the text dimensions
        String title = "Certificate of Achievement";
        String subTitle = "This is to certify that";
        String completionText = "has successfully completed the course";

        Rect titleBounds = new Rect();
        paint.getTextBounds(title, 0, title.length(), titleBounds);

        Rect subTitleBounds = new Rect();
        paint.getTextBounds(subTitle, 0, subTitle.length(), subTitleBounds);

        Rect nameBounds = new Rect();
        paint.getTextBounds(userName, 0, userName.length(), nameBounds);

        Rect completionTextBounds = new Rect();
        paint.getTextBounds(completionText, 0, completionText.length(), completionTextBounds);

        // Calculate total height of the text block
        int totalTextHeight = titleBounds.height() + subTitleBounds.height() + nameBounds.height() + completionTextBounds.height() + 120; // Adjust vertical spacing
        float startY = (templateHeight - totalTextHeight) / 2;

        // Calculate vertical center positions and draw text
        float centerX = templateWidth / 2f;

        // Draw the "Certificate of Achievement" text
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        float titleX = centerX - titleBounds.width() / 2f;
        float titleY = startY;
        canvas.drawText(title, titleX, titleY, paint);

        // Draw the "This is to certify that" text
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        float subTitleX = centerX - subTitleBounds.width() / 2f;
        float subTitleY = titleY + titleBounds.height() + 30;  // Adjust vertical spacing
        canvas.drawText(subTitle, subTitleX, subTitleY, paint);

        // Draw the user's name
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        float nameX = centerX - nameBounds.width() / 2f;
        float nameY = subTitleY + subTitleBounds.height() + 30;  // Adjust vertical spacing
        canvas.drawText(userName, nameX, nameY, paint);

        // Draw the completion text
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        float completionTextX = centerX - completionTextBounds.width() / 2f;
        float completionTextY = nameY + nameBounds.height() + 30;  // Adjust vertical spacing
        canvas.drawText(completionText, completionTextX, completionTextY, paint);

        // Add border to the certificate
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        canvas.drawRect(0, 0, certificateBitmap.getWidth(), certificateBitmap.getHeight(), borderPaint);

        // Display generated certificate on screen
        certificateImageView.setVisibility(View.VISIBLE);
        certificateImageView.setImageBitmap(certificateBitmap);

        // Save certificate to storage
        saveCertificateToStorage(certificateBitmap, userName);

    }

    private void saveCertificateToStorage(Bitmap bitmap, String userName) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Certificates");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, "Certificate_" + userName + ".png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Certificate saved to: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            uploadCertificateToFirebase(file, userName, uid);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save certificate", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCertificateToFirebase(File file, String userName, String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference certificateRef = storageRef.child("certificates/" + uid + "/" + userName + ".png");

        Uri fileUri = Uri.fromFile(file);
        UploadTask uploadTask = certificateRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Certificate uploaded successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload certificate", Toast.LENGTH_SHORT).show();
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
            // Logout logic
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CertificateActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
