
package com.example.a3pract;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnChooseImage, btnRecordVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);

        btnChooseImage.setOnClickListener(v -> openGallery());

        // Проверка разрешений
        btnRecordVideo.setOnClickListener(v -> checkPermissions());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(galleryIntent);
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            recordVideo();
        }
    }


    private void recordVideo() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            resultLauncher.launch(videoIntent);
        } else {
            Toast.makeText(this, "Запись видео недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    String type = getContentResolver().getType(uri);

                    if (type != null && type.startsWith("image/")) {

                        imageView.setImageURI(uri);
                    } else if (type != null && type.startsWith("video/")) {

                        MediaScannerConnection.scanFile(this,
                                new String[]{uri.getPath()}, null,
                                (path, scannedUri) -> Log.d("MainActivity", "Видео добавлено в галерею: " + path));


                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(uri);
                        sendBroadcast(mediaScanIntent);
                    }
                }
            });
}