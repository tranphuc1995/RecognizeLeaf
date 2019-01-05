package com.bachkhoa.recognize_leaf.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bachkhoa.recognize_leaf.R;
import com.bachkhoa.recognize_leaf.custom_ui.CustomDrawView;
import com.bachkhoa.recognize_leaf.global.MethodStatic;
import com.bachkhoa.recognize_leaf.global.VariableStatic;
import com.bachkhoa.recognize_leaf.model.ResponseCheck;
import com.bachkhoa.recognize_leaf.model.ResponseUpload;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Map hmLeaf = new HashMap();
    String mCurrentPhotoPath;
    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    static final int CAMERA_PIC_REQUEST = 1;
    static final int LEAF_REQUEST = 2;
    private int heightScreen = 0;
    private int widthScreen = 0;
    private int x1 = -1;
    private int y1 = -1;
    private int x2 = -1;
    private int y2 = -1;

    private Button button_open_camera;
    private Button button_recognize_leaf;
    private ImageView imageview_leaf;
    private Button button_open_library;
    private RadioButton radio_button_first;
    private RadioButton radio_button_second;
    private LinearLayout linear_checkbox;
    private ProgressBar progressbar;
    private LinearLayout linear_image;
    private EditText edittext_domain;
    private Dialog dialogResult;
    private String error = "";
    private Bitmap bitmapUpload;
    private Bitmap bitmapDraw;
    private int result = 1;
    private String id = "";
    /////
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    Canvas canvas;
    Paint paint;
    Point point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls(); // setup UI
        addEvents();  // add Events
    }

    private void addEvents() {
        button_recognize_leaf_click();
        button_open_library_click();
        button_open_camera_click();
        imageview_leaf_touch();
    }

    private void button_recognize_leaf_click() {
        button_recognize_leaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCondition()) {
                    button_recognize_leaf.setEnabled(false);
                    progressbar.setVisibility(View.VISIBLE);
                    callApiRegconizeLeaf();
                } else {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void callApiRegconizeLeaf() {
        MethodStatic.createGlobal(edittext_domain.getText().toString());
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"),
                createTempFile(bitmapUpload));

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("x1", toRequestBody(String.valueOf(x1)));
        map.put("y1", toRequestBody(String.valueOf(y1)));
        map.put("x2", toRequestBody(String.valueOf(x2)));
        map.put("y2", toRequestBody(String.valueOf(y2)));


        Call<ResponseUpload> response = VariableStatic.mService.uploadImage(file, map);
        response.enqueue(new Callback<ResponseUpload>() {
            @Override
            public void onResponse(Call<ResponseUpload> call, Response<ResponseUpload> response) {
                if (response.body() != null) {
                    id = response.body().getId();
                    Log.d("debug_test", id);
                    callApiGetResult();
                }
            }

            @Override
            public void onFailure(Call<ResponseUpload> call, Throwable t) {
                Log.d("debug_test", t.getMessage());
                button_recognize_leaf.setEnabled(true);
            }
        });
    }

    private RequestBody toRequestBody(String value) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
        return body;
    }

    private void callApiGetResult() {
        Call<ResponseCheck> response = VariableStatic.mService.getResult(id);
        response.enqueue(new Callback<ResponseCheck>() {
            @Override
            public void onResponse(Call<ResponseCheck> call, Response<ResponseCheck> response) {
                if (response.body() != null) {
                    if (response.body().getResult() == 0) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("debug_test", "recall api check");
                                callApiGetResult();
                            }
                        }, 2000);
                    } else if (response.body().getResult() == 99) {
                        button_recognize_leaf.setEnabled(true);
                        progressbar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    } else {
                        progressbar.setVisibility(View.GONE);
                        Log.d("debug_test", "train done!");
                        Log.d("debug_test", "" + response.body().getResult());
                        result = response.body().getResult();
                        showDilalogResult();
                        button_recognize_leaf.setEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseCheck> call, Throwable t) {
                button_recognize_leaf.setEnabled(true);
            }
        });
    }

    private File createTempFile(Bitmap bitmap) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis()
                + "_image.jpeg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private boolean checkCondition() {
        boolean isHaveCondition = true;
        if (edittext_domain.getText().toString().equals("")) {
            isHaveCondition = false;
            error = "Chưa nhập domain";
        } else if (x1 == -1) {
            isHaveCondition = false;
            error = "Chưa chọn tọa độ x1";
        } else if (x2 == -1) {
            isHaveCondition = false;
            error = "Chưa chọn tọa độ x2";
        }
        return isHaveCondition;
    }


    private void button_open_library_click() {
        button_open_library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LibraryLeafActivity.class);
                startActivityForResult(intent, LEAF_REQUEST);
            }
        });
    }

    private void imageview_leaf_touch() {
        imageview_leaf.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ////
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (radio_button_first.isChecked()) {
                    x1 = x;
                    y1 = y;
                    radio_button_first.setText("Tọa độ điểm 1:   x1=" + x1 + "   y1=" + y1 + "  (chấm xanh)");
                }
                if (radio_button_second.isChecked()) {
                    x2 = x;
                    y2 = y;
                    radio_button_second.setText("Tọa độ điểm 2:   x2=" + x2 + "   y2=" + y2 + "  (chấm đỏ)");
                }
                //

                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (radio_button_first.isChecked() || radio_button_second.isChecked())
                            drawCircle();
                        break;
                    case MotionEvent.ACTION_MOVE:


                        break;
                    case MotionEvent.ACTION_UP:

                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;

                }
                return false;


            }
        });
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
                    dispatchTakePictureIntent();
                   /* Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);*/
                } else {
                    // get permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{PERMISSION_CAMERA, PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        });
    }


    private void drawCircle() {
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        Bitmap workingBitmap = Bitmap.createBitmap(bitmapUpload);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        ////
        if (x1 != -1 && y1 != -1)
            canvas.drawCircle(x1, y1, 10, paint);
        ////
        paint.setColor(Color.RED);
        if (x2 != -1 && y2 != -1)
            canvas.drawCircle(x2, y2, 10, paint);
        Glide.with(MainActivity.this).load(mutableBitmap).into(imageview_leaf);
    }

    //phúc
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.bachkhoa.recognize_leaf.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);
            }
        }
    }

    //
    private void addControls() {
        createDataHashMap();
        createPoint();
        getHeightWidthScreen();
        findId();
        //showDilalogResult();
    }

    private void createDataHashMap() {
        hmLeaf.put("1", new String("Pubescent bamboo"));
        hmLeaf.put("2", new String("Chinese horse chestnut"));
        hmLeaf.put("3", new String("Anhui Barberry"));
        hmLeaf.put("4", new String("Chinese redbud"));
        hmLeaf.put("5", new String("True indigo"));
        hmLeaf.put("6", new String("Japanese maple"));
        hmLeaf.put("7", new String("Nanmu"));
        hmLeaf.put("8", new String("Castor aralia"));
        hmLeaf.put("9", new String("Chinese cinnamon"));
        hmLeaf.put("10", new String("Goldenrain tree"));
        hmLeaf.put("11", new String("Big-fruited Holly"));
        hmLeaf.put("12", new String("Japanese cheesewood"));
        hmLeaf.put("14", new String("Wintersweet"));
        hmLeaf.put("15", new String("Camphortree"));
        hmLeaf.put("16", new String("Japan Arrowwood"));
        hmLeaf.put("17", new String("Sweet osmanthus"));
        hmLeaf.put("18", new String("Deodar"));
        hmLeaf.put("19", new String("Ginkgo, maidenhair tree"));
        hmLeaf.put("20", new String("Crape myrtle, Crepe myrtle"));
        hmLeaf.put("21", new String("Oleander"));
        hmLeaf.put("22", new String("Yew plum pine"));
        hmLeaf.put("23", new String("Japanese Flowering Cherry"));
        hmLeaf.put("24", new String("Glossy Privet"));
        hmLeaf.put("25", new String("Chinese Toon"));
        hmLeaf.put("26", new String("Peach"));
        hmLeaf.put("27", new String("Ford Woodlotus"));
        hmLeaf.put("28", new String("Trident maple"));
        hmLeaf.put("29", new String("Beale's barberry"));
        hmLeaf.put("30", new String("Southern magnolia"));
        hmLeaf.put("31", new String("Canadian poplar"));
        hmLeaf.put("32", new String("Chinese tulip tree"));
        hmLeaf.put("33", new String("Tangerine"));
    }

    private void createPoint() {
        point = new Point();
        Display cureentDispaly = getWindowManager().getDefaultDisplay();
        cureentDispaly.getSize(point);

        //
    }

    private void findId() {
        button_open_camera = (Button) findViewById(R.id.button_open_camera);
        imageview_leaf = (ImageView) findViewById(R.id.imageview_leaf);
        button_open_library = (Button) findViewById(R.id.button_open_library);
        linear_checkbox = (LinearLayout) findViewById(R.id.linear_checkbox);
        radio_button_first = (RadioButton) findViewById(R.id.radio_button_first);
        radio_button_second = (RadioButton) findViewById(R.id.radio_button_second);
        button_recognize_leaf = (Button) findViewById(R.id.button_recognize_leaf);
        linear_image = (LinearLayout) findViewById(R.id.linear_image);
        edittext_domain = (EditText) findViewById(R.id.edittext_domain);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        progressbar.setVisibility(View.GONE);
    }

    private boolean isPermissionGranted(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) return true;
        else if (result == PackageManager.PERMISSION_DENIED) return false;
        else throw new IllegalStateException("Cannot check permission " + permission);
    }

    private void getHeightWidthScreen() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightScreen = displayMetrics.heightPixels;
        widthScreen = displayMetrics.widthPixels;
    }

    // Callback permission grant
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(this, "Không có quyền mở camera", Toast.LENGTH_SHORT).show();
        }
    }

    // Callback capture picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            linear_image.setVisibility(View.VISIBLE);
            button_recognize_leaf.setVisibility(View.VISIBLE);
            linear_checkbox.setVisibility(View.VISIBLE);
            resetUi();
            radio_button_first.setChecked(false);
            radio_button_second.setChecked(false);
            try {
                File file = new File(mCurrentPhotoPath);
                bitmapUpload = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), Uri.fromFile(file));
                bitmapUpload = RotateBitmap(bitmapUpload, 90);
                if (bitmapUpload != null) {
                    bitmapUpload = getResizedBitmap(bitmapUpload, 800, 600);
                    Glide.with(this).load(bitmapUpload).into(imageview_leaf);
                }
            } catch (Exception e) {

            }
        } else if (requestCode == LEAF_REQUEST && data != null) {
            resetUi();
            linear_image.setVisibility(View.VISIBLE);
            button_recognize_leaf.setVisibility(View.VISIBLE);
            byte[] byteArray = data.getByteArrayExtra("byteArray");
            bitmapUpload = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Glide.with(this).load(bitmapUpload).into(imageview_leaf);
            linear_checkbox.setVisibility(View.VISIBLE);
        }
    }


    private void showDilalogResult() {
        dialogResult = new Dialog(this);
        dialogResult.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogResult.setCancelable(true);
        dialogResult.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogResult.setContentView(R.layout.dialog_result);
        Window window = dialogResult.getWindow();
        window.setGravity(Gravity.CENTER);
        ////
        //  dialogResult.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        dialogResult.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        //find id
        ImageView image_result = (ImageView) dialogResult.findViewById(R.id.image_result);
        ImageView image_choose = (ImageView) dialogResult.findViewById(R.id.image_choose);
        TextView textview_leaf_name = (TextView) dialogResult.findViewById(R.id.textview_leaf_name);
        Glide.with(this).load(bitmapUpload).into(image_choose);
        int pos = 0;
        try {
            String[] list = getAssets().list("image");
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                s = s.replace(".jpg", "");
                if (s.equals(String.valueOf(result))) {
                    pos = i;
                    break;
                }
            }
            InputStream ims = getAssets().open("image" + "/" + list[pos]);
            Glide.with(this).load(drawableToBitmap(Drawable.createFromStream(ims, null))).into(image_result);
            // show leaf name
            Set set = hmLeaf.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                if (me.getKey().toString().equals(String.valueOf(result))) {
                    textview_leaf_name.setText(me.getValue().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void resetUi() {
        x1 = -1;
        y1 = -1;
        x2 = -1;
        y2 = -1;
        radio_button_first.setText("Tọa độ điểm 1:");
        radio_button_second.setText("Tọa độ điểm 2:");
        radio_button_first.setChecked(false);
        radio_button_second.setChecked(false);
    }
}
