package com.patelheggere.tripplanner.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.patelheggere.tripplanner.BaseApplication;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.EventDetailModel;
import com.patelheggere.tripplanner.model.PlaceDetails;
import com.patelheggere.tripplanner.utils.UtilsClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = "AddEventActivity";
    private static final int SELECT_FILE = 1100;
    private static final int REQUEST_CAMERA = 1200;
    private List<PlaceDetails> placeDetailsList;
    private EditText mEditTextDate, mEditTextTime, mEditTextContactName, mEditTextPhone;
    private TextInputEditText mTextInputEditTextEventName;
    private Spinner mSpinnerPlace;
    private Button buttonSubmit;
    private List<String> placeList;
    private Calendar calendar;
    private int year, month, day, mHour, mMinute;;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private DatabaseReference databaseReference, databaseReferencePlaces;
    private int selectedPlace;
    private EventDetailModel eventDetailModel;
    private boolean isEdit;
    private String key;
    private ActionBar mActionBar;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageViewAttachment, imageViewUploaded;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String userChoosenTask;
    private String imageUrl = null;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        intiViews();
        initToolBar();
        initData();
        initListener();
    }

    private void initToolBar() {
        mActionBar = getSupportActionBar();
        if(mActionBar!=null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void intiViews() {
        mTextInputEditTextEventName = findViewById(R.id.editTextEventName);
        mEditTextDate = findViewById(R.id.editTextDate);
        mEditTextTime = findViewById(R.id.editTextTime);
        mSpinnerPlace = findViewById(R.id.placeSpinner);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        mEditTextContactName = findViewById(R.id.editTextContactName);
        mEditTextPhone = findViewById(R.id.editTextContactPhone);
        imageViewAttachment = findViewById(R.id.imageViewClip);
        imageViewUploaded = findViewById(R.id.imageViewUploaded);
        disableEditText(mEditTextDate);
        disableEditText(mEditTextTime);
    }

    private void initData()
    {
        if(getIntent().getParcelableExtra("Data")!=null)
        {
            eventDetailModel = getIntent().getParcelableExtra("Data");
            isEdit = getIntent().getBooleanExtra("EDIT", false);
            mTextInputEditTextEventName.setText(eventDetailModel.getEventName());
            key = eventDetailModel.getKey();
            String dateTime = UtilsClass.getDateTimeFromMilli2(eventDetailModel.getTimeStamp());
            mEditTextDate.setText(UtilsClass.getDateDDMMYYY2(eventDetailModel.getTimeStamp()));
            mEditTextTime.setText(UtilsClass.getTime(eventDetailModel.getTimeStamp()));
            selectedDay = Integer.valueOf(dateTime.substring(0,2));
            selectedMonth = Integer.valueOf(dateTime.substring(2,4))-1;
            selectedYear = Integer.valueOf(dateTime.substring(4,8));
            selectedHour = Integer.valueOf(dateTime.substring(8,10));
            selectedMinute = Integer.valueOf(dateTime.substring(11,13));
            if(eventDetailModel.getContactNumber()!=null)
            {
                mEditTextPhone.setText(eventDetailModel.getContactNumber());
            }
            if(eventDetailModel.getContactPerson()!=null)
            {
                mEditTextContactName.setText(eventDetailModel.getContactPerson());
            }
            if(!dateTime.substring(14).equalsIgnoreCase("A"))
            {
                selectedHour+=12;
            }

        }
        if(isEdit)
        {
            mActionBar.setTitle(getString(R.string.update_event));
            buttonSubmit.setText(getString(R.string.update));
        }
        else {
            mActionBar.setTitle(getString(R.string.add_event));
        }
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        databaseReferencePlaces = BaseApplication.getFireBaseRef();
        databaseReferencePlaces = databaseReferencePlaces.child("placeDetails");
        databaseReferencePlaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                placeDetailsList = new ArrayList<>();
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    PlaceDetails placeDetails = new PlaceDetails();
                    placeDetails = dataSnapshot1.getValue(PlaceDetails.class);
                    placeDetailsList.add(placeDetails);
                }
                placeList = new ArrayList<>();
                if(placeDetailsList!=null) {
                    for (int j = 0; j < placeDetailsList.size(); j++) {
                        placeList.add(placeDetailsList.get(j).getPlaceName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddEventActivity.this, android.R.layout.simple_spinner_item, placeList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinnerPlace.setAdapter(adapter);
                    if(eventDetailModel!=null)
                    {
                        for (int i=0; i<placeList.size(); i++)
                        {
                            if(eventDetailModel.getPlaceName().equalsIgnoreCase(placeList.get(i)))
                            {
                                mSpinnerPlace.setSelection(i);
                                selectedPlace = i;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!UtilsClass.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    private void initListener()
    {
        mSpinnerPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPlace = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mEditTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        String dd = "", mm = "";
                        if (i2 < 10) {
                            dd = "0" + i2;
                        } else {
                            dd = "" + i2;
                        }
                        if (i1 < 9) {
                            mm = "0" + (i1 + 1);
                        } else {
                            mm = "" + (i1 + 1);
                        }
                        selectedYear = i;
                        selectedDay = i2;
                        selectedMonth = i1;
                        mEditTextDate.setText(dd + "/" + mm + "/" + i);
                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();
            }
        });
        mEditTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                if(hourOfDay>12)
                                {
                                    mEditTextTime.setText((hourOfDay-12) + ":" + minute+" PM");
                                }else {
                                    mEditTextTime.setText(hourOfDay + ":" + minute+" AM");
                                }
                                selectedHour = hourOfDay;
                                selectedMinute = minute;
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEdit)
                {
                    update();
                }
                else {
                    addEvent();
                }
            }
        });
        imageViewAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }

    private void update()
    {
        if(mTextInputEditTextEventName.getText()==null || mTextInputEditTextEventName.getText().toString().trim().isEmpty())
        {
            mTextInputEditTextEventName.setError("Enter Event Name");
            return;
        }
        if(mEditTextDate.getText()==null || mEditTextDate.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Select Date", Toast.LENGTH_LONG).show();
            return;
        }
        if(mEditTextTime.getText()==null || mEditTextTime.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Select Time", Toast.LENGTH_LONG).show();
            return;
        }
        if(mEditTextContactName.getText()==null ||mEditTextContactName.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Enter Contact Person", Toast.LENGTH_LONG).show();
            return;
        }

        EventDetailModel eventDetailModel = new EventDetailModel();
        Calendar c = Calendar.getInstance();
        c.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
        eventDetailModel.setEventName(mTextInputEditTextEventName.getText().toString());
        eventDetailModel.setPlaceName(placeDetailsList.get(selectedPlace).getPlaceName());
        eventDetailModel.setLat(placeDetailsList.get(selectedPlace).getLat());
        eventDetailModel.setLon(placeDetailsList.get(selectedPlace).getLng());
        eventDetailModel.setTimeStamp(c.getTimeInMillis());
        eventDetailModel.setContactPerson(mEditTextContactName.getText().toString());
        if(mEditTextPhone.getText()!=null && !mEditTextPhone.getText().toString().trim().isEmpty() && mEditTextPhone.getText().toString().length()==10)
        {
           eventDetailModel.setContactNumber(mEditTextPhone.getText().toString());
        }
        if(imageUrl!=null)
        {
            eventDetailModel.setImageURL(imageUrl);
        }
        databaseReference = BaseApplication.getFireBaseRef();
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(eventDetailModel.getKey(), eventDetailModel);
        databaseReference.child("eventDetails").child(key).setValue(eventDetailModel);
        mTextInputEditTextEventName.setText("");
        mEditTextDate.setText("");
        mEditTextTime.setText("");
        mEditTextPhone.setText("");
        mEditTextContactName.setText("");
        imageUrl = null;
        imageViewUploaded.setVisibility(View.GONE);
        Toast.makeText(this, "Event Updated Successfully", Toast.LENGTH_LONG).show();
    }

    private void addEvent()
    {
        if(mTextInputEditTextEventName.getText()==null || mTextInputEditTextEventName.getText().toString().trim().isEmpty())
        {
            mTextInputEditTextEventName.setError("Enter Event Name");
            return;
        }
        if(mEditTextDate.getText()==null || mEditTextDate.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Select Date", Toast.LENGTH_LONG).show();
            return;
        }
        if(mEditTextTime.getText()==null || mEditTextTime.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Select Time", Toast.LENGTH_LONG).show();
            return;
        }
        EventDetailModel eventDetailModel = new EventDetailModel();
        Calendar c = Calendar.getInstance();
        c.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
        eventDetailModel.setEventName(mTextInputEditTextEventName.getText().toString());
        eventDetailModel.setPlaceName(placeDetailsList.get(selectedPlace).getPlaceName());
        eventDetailModel.setLat(placeDetailsList.get(selectedPlace).getLat());
        eventDetailModel.setLon(placeDetailsList.get(selectedPlace).getLng());
        eventDetailModel.setContactPerson(mEditTextContactName.getText().toString());
        if(mEditTextPhone.getText()!=null && !mEditTextPhone.getText().toString().trim().isEmpty() && mEditTextPhone.getText().toString().length()==10)
        {
            eventDetailModel.setContactNumber(mEditTextPhone.getText().toString());
        }
        eventDetailModel.setTimeStamp(c.getTimeInMillis());
        if(imageUrl!=null)
        {
            eventDetailModel.setImageURL(imageUrl);
        }
        databaseReference = BaseApplication.getFireBaseRef();
        databaseReference.child("eventDetails").push().setValue(eventDetailModel);
        mTextInputEditTextEventName.setText("");
        mEditTextDate.setText("");
        mEditTextTime.setText("");
        mEditTextPhone.setText("");
        mEditTextContactName.setText("");
        imageUrl = null;
        imageViewUploaded.setVisibility(View.GONE);
        Toast.makeText(this, "Event Added Successfully", Toast.LENGTH_LONG).show();
    }
    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setCursorVisible(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_right, R.anim.right_left);
    }
    private void uploadImage(final Uri path) {
        AlertDialog.Builder  builder= new AlertDialog.Builder(this);
        builder.setMessage("are you sure want to uplaod?");
        builder.setTitle("Confirm");
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                imageViewUploaded.setImageURI(path);
                storageReference = BaseApplication.getFirebaseStorage();
                filePath = path;
                if(filePath != null)
                {
                    final ProgressDialog progressDialog = new ProgressDialog(AddEventActivity.this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();

                    final StorageReference ref = storageReference.child("EventAttachment/"+ UtilsClass.getDateTimeFromMilli2(System.currentTimeMillis()));
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
                                            imageViewUploaded.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    Toast.makeText(AddEventActivity.this, "Attachment Uploaded", Toast.LENGTH_SHORT).show();
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddEventActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
            String filePath = getRealPathFromUri(AddEventActivity.this, path);
            if (filePath != null) {
                //getUploadConfirm(new File(filePath));
                uploadImage(path);
            }
        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            String result = null;

            CursorLoader cursorLoader = new CursorLoader(
                    AddEventActivity.this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
        builder.setTitle("Select Attachment!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean gallery = UtilsClass.checkPermission(AddEventActivity.this);
                boolean camera = UtilsClass.checkPermissionCamera(AddEventActivity.this);
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
