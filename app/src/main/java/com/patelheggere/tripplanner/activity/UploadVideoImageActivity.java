
package com.patelheggere.tripplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.utils.GPSTracker;
import com.patelheggere.tripplanner.utils.UtilsClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadVideoImageActivity extends AppCompatActivity {
    private static final String TAG = "UploadVideoImageActivit";
    private long TandaID;
    private static final int SELECT_FILE = 1100;
    private static final int REQUEST_CAMERA = 1200;
    private static final int CAMERA_REQUEST_CODE_VEDIO = 3000;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String stageURL = null, audienceURL =null, videoURL=null;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String userChoosenTask;
    private String imageUrl = null;
    private AlertDialog alertDialog;
    private Button mImageButton, mVideoButton, mImageAudeinceBtn, mButtonSubmit;
    private GPSTracker gps;
    private ImageView imageViewStage, imageViewAudience;
    private int imageType = 1;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video_image);
        mActionBar = getSupportActionBar();
        if(mActionBar!=null)
        {
            mActionBar.setTitle("Capture Photo and Videp");
        }
        mImageButton = findViewById(R.id.imageupload);
        mVideoButton = findViewById(R.id.videoupload);
        mImageAudeinceBtn = findViewById(R.id.audienceimageupload);
        imageViewAudience = findViewById(R.id.audiestagephoto);
        imageViewStage = findViewById(R.id.stagephoto);
        mButtonSubmit = findViewById(R.id.submitbtn);
        TandaID = getIntent().getLongExtra("ID", 0);
        if (!UtilsClass.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageType = 1;
                selectImage();
            }
        });

        mImageAudeinceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageType = 2;
                selectImage();
            }
        });

        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageType = 3;
                RecordVideo();
            }
        });

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stageURL==null || stageURL.length()<5)
                {
                    Toast.makeText(UploadVideoImageActivity.this, "Upload Stage Photo", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(audienceURL==null || audienceURL.length()<5)
                {
                    Toast.makeText(UploadVideoImageActivity.this, "Upload Audience Photo", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(videoURL==null || videoURL.length()<5)
                {
                    Toast.makeText(UploadVideoImageActivity.this, "Upload Video", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        gps = new GPSTracker(this);
        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.d(TAG, "onCreate: "+latitude+" long:"+longitude);
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
    }
    LocationManager locationManager;

    boolean gotLocation = false;

    double longitude = 0.0;
    double latitude = 0.0;

    public boolean validLatLng (double lat, double lng) {
        if(lat != 0.0 && lng != 0.0){
            this.gotLocation = true;
            return true;
        } else return false;
    }

    public boolean haveLocation() {
        return this.gotLocation;
    }


    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged (Location location){
            if (!haveLocation() && validLatLng(location.getLatitude(), location.getLongitude())) {
                //System.out.println("got new location");
                //Log.i("mLocationListener", "Got location");   // for logCat should ->  import android.util.Log;

                // Stops the new update requests.
                locationManager.removeUpdates(mLocationListener);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Log.d(TAG, "onLocationChanged: longitude: "+longitude+" latitude:"+latitude);
              //  File f = new File(Environment.getExternalStorageDirectory(), "/PhotoGPSApp/Attachment" + ".jpg");
              //  geoTag(f.getAbsolutePath(), latitude, longitude);

            }
        }

        public void onStatusChanged(java.lang.String s, int i, android.os.Bundle bundle) {
        }

        public void onProviderEnabled(java.lang.String s){
        }

        public void onProviderDisabled(java.lang.String s){
        }

    };

    private void RecordVideo()
    {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent,
                    CAMERA_REQUEST_CODE_VEDIO);
        }
    }
    private void uploadImage(final Uri path) {
        if(imageType==1)
        {
            imageViewStage.setImageURI(path);
        }
        else if(imageType==2)
        {
            imageViewAudience.setImageURI(path);
        }

        AlertDialog.Builder  builder= new AlertDialog.Builder(this);
        builder.setMessage("are you sure want to upload?");
        builder.setTitle("Confirm");
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
               // imageViewUploaded.setImageURI(path);
                storageReference = BaseApplication.getFirebaseStorage();
                filePath = path;
                if(filePath != null)
                {
                    final ProgressDialog progressDialog = new ProgressDialog(UploadVideoImageActivity.this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();

                    final StorageReference ref = storageReference.child("Matantara/"+TandaID+"/"+ UtilsClass.getDateTimeFromMilli2(System.currentTimeMillis()));
                    //UploadTask uploadTask =
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Uri downloadUrl = uri;
                                            imageUrl = downloadUrl.toString();
                                            if(imageType==1)
                                            {
                                                stageURL = imageUrl;
                                            }
                                            else if(imageType==2)
                                            {
                                                audienceURL = imageUrl;
                                            }
                                            else if(imageType==3)
                                            {
                                                videoURL = imageUrl;
                                            }
                                         //   imageViewUploaded.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    Toast.makeText(UploadVideoImageActivity.this, "Attachment Uploaded", Toast.LENGTH_SHORT).show();
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadVideoImageActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                }
                            });

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            if (requestCode == PERMISSION_ALL) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                    }
                }
                if ("Choose from Library".equalsIgnoreCase(userChoosenTask))
                    galleryIntent();
                else if ("Take Photo".equalsIgnoreCase(userChoosenTask)) {
                    cameraIntent();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
            else if(requestCode==CAMERA_REQUEST_CODE_VEDIO)
            {
                Uri videoUri = data.getData();
                uploadImage(videoUri);
               // path = Utils.getRealPathFromURI(videoUri, this);
            }
        }
    }
    private void onSelectFromGalleryResult(Intent data) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Uri path = data.getData();
        if (path != null) {
            Log.d(TAG, "onSelectFromGalleryResult: URI:" + path.toString());
            if (path.toString().contains("com.google.android.apps.photos")) {
                return;
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String filePath = getRealPathFromUri(UploadVideoImageActivity.this, path);
            if (filePath != null) {
                //getUploadConfirm(new File(filePath));
                uploadImage(path);
            }
        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    UploadVideoImageActivity.this,
                    path, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();

            if (cursor != null) {
                int column_index =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result = cursor.getString(column_index);
                if (result != null) {
                    //getUploadConfirm(new File(result));
                    uploadImage(Uri.parse(result));
                }
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (destination != null) {
            //getUploadConfirm(destination);
            uploadImage(Uri.fromFile(destination));
        }
    }
    public static String getRealPathFromUri(Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadVideoImageActivity.this);
        builder.setTitle("Select Attachment!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean gallery = UtilsClass.checkPermission(UploadVideoImageActivity.this);
                boolean camera = UtilsClass.checkPermissionCamera(UploadVideoImageActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (camera)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (gallery)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}