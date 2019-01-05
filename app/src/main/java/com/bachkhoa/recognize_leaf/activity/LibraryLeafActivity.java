package com.bachkhoa.recognize_leaf.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bachkhoa.recognize_leaf.adapter.LibraryLeafAdapter;
import com.bachkhoa.recognize_leaf.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LibraryLeafActivity extends AppCompatActivity {

    private List<Bitmap> listImage = new ArrayList<>();
    private List<Bitmap> listOriginal = new ArrayList<>();
    private RecyclerView rv_leaf;
    private TextView textview_loading;
    private LibraryLeafAdapter libraryLeafAdapter;
    private GridLayoutManager gridLayoutManager;
    private int start = 6;
    private int end = 6;
    private boolean isLoadMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_leaf);
        addControls();
        addEvents();
    }

    private void addEvents() {
        rv_leaf_loadmore();
    }

    private void rv_leaf_loadmore() {
        rv_leaf.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if (isLoadMore) {
                        isLoadMore = false;
                        loadmore();
                    }
                }
            }
        });
    }

    private void loadmore() {
        start = end;
        if ((end + 6) >= listImage.size()) {
            end = listImage.size();
            isLoadMore = false;
        } else {
            end += 6;
            isLoadMore = true;
        }
        for (int i = start; i < end; i++) {
            libraryLeafAdapter.getListImage().add(listImage.get(i));
            libraryLeafAdapter.notifyItemInserted(libraryLeafAdapter.getItemCount() - 1);
        }
        Log.d("debug_test", "" + libraryLeafAdapter.getItemCount());
    }

    private void addControls() {
        findId();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataAsset("library");
                initRvLeaf();
                textview_loading.setVisibility(View.GONE);
            }
        }, 100);
    }

    private void initRvLeaf() {
        List<Bitmap> listTemp = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            listTemp.add(listImage.get(i));
        }
        gridLayoutManager = new GridLayoutManager(this, 3);
        rv_leaf.setLayoutManager(gridLayoutManager);
        libraryLeafAdapter = new LibraryLeafAdapter(this, listTemp);
        rv_leaf.setAdapter(libraryLeafAdapter);
    }

    private void findId() {
        rv_leaf = (RecyclerView) findViewById(R.id.rv_leaf);
        textview_loading = (TextView) findViewById(R.id.textview_loading);
    }

    private void getDataAsset(String path) {
        String[] list;

        try {
            list = getAssets().list(path);
            if (list.length > 0) {

                for (String file : list) {
                    InputStream ims = getAssets().open(path + "/" + file);
                    listOriginal.add(drawableToBitmap(Drawable.createFromStream(ims, null)));
                    listImage.add(getResizedBitmap(drawableToBitmap(Drawable.createFromStream(ims, null)), 150, 150));
                }
            } else {
                System.out.println("Failed Path = " + path);
                System.out.println("Check path again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void returnBitmapToMainActivity(int position) {
        Intent intent = new Intent();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Bitmap bitmap =/* getResizedBitmap(listOriginal.get(position), 800, 600);*/listOriginal.get(position);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
        intent.putExtra("byteArray", bs.toByteArray());
        setResult(2, intent);
        finish();
    }
}
