package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private List<Uri> certificateUris;
    private Context context;

    public CertificateAdapter(Context context, List<Uri> certificateUris) {
        this.context = context;
        this.certificateUris = certificateUris;
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Uri uri = certificateUris.get(position);
        Glide.with(context).load(uri).into(holder.certificateImageView);
    }

    @Override
    public int getItemCount() {
        return certificateUris.size();
    }

    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        ImageView certificateImageView;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            certificateImageView = itemView.findViewById(R.id.certificateImageView);
        }
    }
}
