package com.iatli.bluetoothprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class DebugImageActivity extends AppCompatActivity {
    private Bitmap bmp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_image);

        Intent intent = getIntent();

        if(intent != null) {
            //byte[] byteArray =intent.getByteArrayExtra("image");
            //bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            ImageView imgV = findViewById(R.id.img_view);
            imgV.setImageBitmap(PrintActivity.bmp);
        }

    }
}