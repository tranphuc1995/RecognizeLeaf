package com.bachkhoa.recognize_leaf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    static final int CAMERA_PIC_REQUEST = 1;

    private Button button_open_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls(); // setup UI
        addEvents();  // add Events
    }

    private void addEvents() {
        button_open_camera_click();
    }

    private void button_open_camera_click() {
        button_open_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasCamera = isPermissionGranted(PERMISSION_CAMERA);
                boolean hasWriteExternalStorage = isPermissionGranted(PERMISSION_WRITE_EXTERNAL_STORAGE);
                boolean hasReadExternalStorage = isPermissionGranted(PERMISSION_READ_EXTERNAL_STORAGE);
                if (hasCamera && hasReadExternalStorage && hasWriteExternalStorage) {
                    // open camera
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                } else {
                    // get permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{PERMISSION_CAMERA, PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        });
    }

    private void addControls() {
        findId();
    }

    private void findId() {
        button_open_camera = (Button) findViewById(R.id.button_open_camera);
    }

    private boolean isPermissionGranted(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) return true;
        else if (result == PackageManager.PERMISSION_DENIED) return false;
        else throw new IllegalStateException("Cannot check permission " + permission);
    }

    // Callback permission grant
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
        } else {
            Toast.makeText(this, "Không có quyền mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    // Callback capture picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ImageView imageview = (ImageView) findViewById(R.id.imageview_leaf); //sets imageview as the bitmap
            imageview.setImageBitmap(image);
        }
    }
}
