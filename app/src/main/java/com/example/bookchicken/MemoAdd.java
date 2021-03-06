package com.example.bookchicken;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MemoAdd extends AppCompatActivity {

    private String uri_String;

    public static final int REQUEST_PERMISSION = 11;
    //    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int GALLERY_CODE = 12;

    private ImageView imageView;
    private String img_Path = ""; // ??????????????? ????????? ?????? ??????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_add);

        EditText memodate = (EditText) findViewById(R.id.memodate);
        EditText booktitle = (EditText) findViewById(R.id.booktitle);
        EditText bookmemo = (EditText) findViewById(R.id.bookmemo);

        Button btn_add = (Button) findViewById(R.id.btn_complete);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MemoAdd.this, MemoList_Activity.class);

                MemoList_Activity.arrayDataList.add(new MemoList_Data(
                        memodate.getText().toString(),
                        booktitle.getText().toString(),
                        uri_String,
                        bookmemo.getText().toString()));
                savememoData("memolist", MemoList_Activity.arrayDataList);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

//                SharedPreferences sharedPreferences = getSharedPreferences("BookMemo", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                Gson gson = new GsonBuilder().create();
//                String MemoListData = gson.toJson("MemoList_Data", MemoList_Data.class);
//                editor.putString("MemoList",MemoListData);
//                editor.apply();

//                try {/
//                    Log.d(TAG, "bitmap : " + imageBitmap);
////                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
////                    Bitmap bitmap = drawable.getBitmap();
//                    //ByteArrayOutputStream(????????? ????????? ???????????? ???????????? ???????????? ????????? ?????? ????????????)
//                    //Stream? ???????????? ?????? ?????? ??????
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    byte[] byteArray = stream.toByteArray();
//                    intent.putExtra("image", byteArray);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

        imageView = findViewById(R.id.bookimage);
        Button btn_picture = (Button) findViewById(R.id.btn_imageadd);
        btn_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int permissionCheck = ContextCompat.checkSelfPermission(MemoAdd.this, Manifest.permission.CAMERA);
//                if(permissionCheck == PackageManager.PERMISSION_DENIED){
//                    ActivityCompat.requestPermissions(MemoAdd.this, new String[]{Manifest.permission.CAMERA},0);
                checkPermission();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
    }

    //????????? ???????????? imageView??? ????????????
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                if (uri != null) {
                    imageView.setImageURI(uri);

                    uri_String = uri.toString();
                    //?????? ?????? ?????? ??? ?????? ????????? ??????
                    img_Path = createCopyAndReturnRealPath(this, uri);
                }
            }
        }
    }

    //???????????? ????????? ??? ????????? ?????????
    public static String createCopyAndReturnRealPath(Context context, Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null)
            return null;
        //?????? ?????? ??????
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            //??????????????? ?????? uri??? ?????? ???????????? ????????? ???????????? ???????????????
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            //????????? ???????????? ?????? ??????????????? file????????? ???????????? ????????? ????????????
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
            return null;
        }
        return file.getAbsolutePath();
    }

    //?????? ????????? ????????????
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //?????? ??????????????? ????????????
    private Bitmap rotate(Bitmap src, float degree) {
        Matrix matrix = new Matrix(); //Maxtrix ?????? ??????
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    //?????? ???????????? ?????????(Uri????????? ??????????????? ????????????), contentUri: URI ??????, return String: ?????? ?????? ??????
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String uri = cursor.getString(columnIndex);
        cursor.close();
        return uri;
    }

    // sharedpreference ?????? ?????????!
    private void savememoData(String key, ArrayList<MemoList_Data> arrayDataList) {
        SharedPreferences sf = getSharedPreferences("memolist", MODE_PRIVATE);
        Type listType = new TypeToken<ArrayList<MemoList_Data>>() {}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(arrayDataList, listType);
        SharedPreferences.Editor editor = sf.edit();
        editor.putString("memolist", json);
        editor.apply();
    }

    //?????? ??????
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //????????? ????????? ?????? ??????
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "??? ?????? ???????????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    public void onRequestPermissionResult(int requestCode, String permission[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                //????????? ???????????? result ????????? ????????????
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "?????? ??????", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "?????? ??????", Toast.LENGTH_LONG).show();
                    finish(); // ????????? ????????? ??? ??????
                }
            }
        }
    }
}


    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == 1){
//            if(resultCode == RESULT_OK){
//
//                Uri uri = null;
//                if(data != null){
//                    uri = data.getData();
//                }
//                if(uri != null){
//                    imageView.setImageURI(uri);
//                }
//
//                try {
//                    InputStream in = getContentResolver().openInputStream(data.getData());
//
//                    Bitmap img = BitmapFactory.decodeStream(in);
//                    in.close();
//                    imageView.setImageBitmap(img);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }


    //    public void capture() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        if (intent.resolveActivity(getPackageManager()) != null) { // ???????????? ????????? ????????? activity??? ????????? ??????
//
//            File photoFile = null; // ????????? ????????? ????????? ?????? ??????
//
//            try{
//                File tempDir = getCacheDir(); // ????????? ????????? ??????????????? ????????? ???????????????
//
//                //?????????????????? ??????
//                String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
//                String imageFileName = "Capture_" + timeStamp + "_";
//
//                File tempImage = File.createTempFile(
//                    imageFileName, // ????????????
//                        ".jpg", // ????????????
//                        tempDir // ??????
//                );
//
//                mCurrentPhotoPath = tempImage.getAbsolutePath(); // Action_view ???????????? ????????? ??????(??????????????????)
//
//                photoFile = tempImage;
//
//            }catch(IOException e){
//                Log.w(TAG,"?????? ?????? ??????",e);
//            }
//
//            //????????? ??????????????? ?????????????????? ?????? ??????
//            if(photoFile != null){
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.bookchicken.fileprovider", photoFile);
//
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // ????????? uri??? ??????
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE); // ????????? ??????
//            }else{
//                Uri photoURI = Uri.fromFile(tempFile);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//            }
//        }
//    }
//
//        protected void onActivityResult(int requestcode, int resultCode, Intent data) {
//        super.onActivityResult(requestcode, resultCode, data);
//        try{
//            switch(requestcode){
//                case REQUEST_IMAGE_CAPTURE: {
//                    if(resultCode == RESULT_OK){
//
//                        File file = new File(mCurrentPhotoPath);
//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
//
//                        if(bitmap != null){
//                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
//                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                                    ExifInterface.ORIENTATION_UNDEFINED);
//
//                            Bitmap rotateBitmap = null;
//                            switch(orientation){
//                                case ExifInterface.ORIENTATION_ROTATE_90:
//                                    rotateBitmap = rotateImage(bitmap, 90);
//                                break;
//
//                                case ExifInterface.ORIENTATION_ROTATE_180:
//                                    rotateBitmap = rotateImage(bitmap, 180);
//                                break;
//
//                                case ExifInterface.ORIENTATION_ROTATE_270:
//                                    rotateBitmap = rotateImage(bitmap, 270);
//                                break;
//
//                                case ExifInterface.ORIENTATION_NORMAL:
//                              default:
//                                  rotateBitmap = bitmap;
//                                break;
//                            }
//                            imageView.setImageBitmap(rotateBitmap); // ????????? ??????
//                        }
//                    }
//                }
//                break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        }
//
//    //???????????? ?????? ????????? ????????????
//    public static Bitmap rotateImage(Bitmap source, float angle){
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//    }

//    //????????? ?????? ?????????
//    private void saveImg(){
//        try{
//            //????????? ?????? ??????
//            File storageDir = new File(getFilesDir() + "/capture");
//            if(!storageDir.exists()) // ????????? ????????? ??????.
//                storageDir.mkdirs();
//
//            String filename = "????????????" + ".jpg";
//
//            //????????? ????????? ??????
//            File file = new File(storageDir, filename);
//            boolean deleted = file.delete();
//            Log.w(TAG,"Delete Dup Check" + deleted);
//            FileOutputStream output = null;
//
//            try{
//                output = new FileOutputStream(file);
//                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//                Bitmap bitmap = drawable.getBitmap();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//            }catch (FileNotFoundException e){
//                e.printStackTrace();
//            }finally{
//                try{
//                    assert output != null;
//                    output.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }catch (Exception e){
//        }
//    }

//    public void onResume(){
//        super.onResume();
//        checkPermission(); // ????????????
//    }




//    //??????????????? ????????? ????????????
//    public void capture() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//
//            try{
//                tempFile = createImageFile();
//            }catch(IOException e){
//                Toast.makeText(this,"????????? ?????? ??????! ?????? ??????????????????",Toast.LENGTH_SHORT).show();
//                finish();
//                e.printStackTrace();
//            }
//            if(tempFile != null){
//                Uri photoUri = FileProvider.getUriForFile(this,
//                        "{applicationId}.provider", tempFile);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//
//            }else{
//                Uri photoUri = Uri.fromFile(tempFile);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//            }
//        }
//    }

//    //???????????? ?????? ????????? ????????? ?????? ?????????(???????????? ????????? ??????, ??????????????? ?????? ????????? ?????? ????????? ????????? ???????????? ????????? ??? ??????)
//    private File createImageFile() throws IOException{
//        //????????? ?????? ??????
//        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
//        String imageFileName = "bookimage_" + timeStamp + "_";
//
//        //???????????? ????????? ?????? ??????(bookimage)
//        File storageDir = new File(Environment.getExternalStorageDirectory() + "/bookimage/");
//        if(!storageDir.exists()) storageDir.mkdirs();
//
//        //??? ?????? ??????
//        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
//
//        return image;
//    }
//
//    //???????????? tempFile??? ????????? ????????? bitmap ????????? ????????? ??? imageView??? ?????? ???????????? ???????????????
//    private void setImage(){
//        ImageView imageView = findViewById(R.id.bookimage);
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
//
//        imageView.setImageBitmap(originalBm);
//    }


//    protected void onActivityResult(int requestcode, int resultCode, Intent data) {
//        super.onActivityResult(requestcode, resultCode, data);
//        if (requestcode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//           setImage();
//        }
//    }




//    private void saveBitmapToJpeg(Bitmap bitmap, String name){
//
//        //??????????????? ?????? ????????? ???????????????
//        File storage = getCacheDir();
//
//        //????????? ?????? ??????
//        String fileName = name + ".jpg";
//
//        //storage??? ?????? ??????????????? ???????????????
//        File tempFile = new File(storage, fileName);
//
//        try{
//            //???????????? ??? ????????? ???????????????
//            tempFile.createNewFile();
//
//            //????????? ??? ??? ?????? ???????????? ???????????????
//            FileOutputStream out = new FileOutputStream(tempFile);
//
//            //compress ????????? ?????? ???????????? ???????????? ???????????????
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//            //????????? ?????? ??? ???????????????
//            out.close();
//
//        }catch(FileNotFoundException e) {
//            Log.d(TAG, "FileNotFoundException: " + e.getMessage());
//        }catch(IOException e){
//            Log.e(TAG, "IOException: " + e.getMessage());
//        }
//    }
//
//    private void getBitmapFromCacheDir(){
//        String found;
//        Bitmap bitmap;
//
//        File file = new File(getCacheDir().toString());
//        File[] files = file.listFiles();
//
//        for(File tempFile : files){
//            if(tempFile.getName().contains("")){
//                found = (tempFile.getName());
//                String path = getCacheDir() + "/" + found;
//                bitmap = BitmapFactory.decodeFile(path);
//            }
//        }
//        return bitmap;
//    }

//    //setStringArrayPref??? ArrayList??? Json?????? ???????????? SharedPreferences??? String??? ???????????? ??????
//    public static void setStringArrayPref(Context context, String key, ArrayList<MemoList_Data> values){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        JSONArray a = new JSONArray();
//
//        for (int i = 0; i<values.size(); i++){
//            a.put(values.get(i));
//        }
//        if(!values.isEmpty()){
//            editor.putString(key, a.toString());
//        }else{
//            editor.putString(key, null);
//        }
//        editor.apply();
//    }
//}

//        Button btn_image = (Button) findViewById(R.id.btn_imageadd);
//        btn_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                resultLauncher.launch(intent);
//            }
//        });
//    }
//
//    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Intent intent = result.getData();
//                        Bundle extras = intent.getExtras();
//                        Bitmap imageBitmap = (Bitmap) extras.get("intent");
//                        imageView.setImageBitmap(imageBitmap);
//                    }
//                }
//            });


//        imageView = findViewById(R.id.bookimage);
//        Button btn_picture = (Button) findViewById(R.id.btn_imageadd);
//        btn_picture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                takePicture();
//            }
//        });
//    }
//
//    public void takePicture() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//    public void onActivityResult(int requestcode, int resultCode, Intent data) {
//        super.onActivityResult(requestcode, resultCode, data);
//        if (requestcode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
//        }
//    }
//}

//    }
