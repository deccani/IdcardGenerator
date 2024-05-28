package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CertificateViewerActivity extends AppCompatActivity {

    private RecyclerView certificatesRecyclerView;
    private CertificateAdapter certificateAdapter;
    private List<Uri> certificateUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certifcate_viewer);

        certificatesRecyclerView = findViewById(R.id.certificatesRecyclerView);
        certificatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateAdapter = new CertificateAdapter(this, certificateUris);
        certificatesRecyclerView.setAdapter(certificateAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        loadCertificatesFromFirebase(uid);
    }

    private void loadCertificatesFromFirebase(String uid) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference userCertificatesRef = storageRef.child("certificates/" + uid);

        userCertificatesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    certificateUris.add(uri);
                    certificateAdapter.notifyDataSetChanged();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load certificate", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load certificates", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(CertificateViewerActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
