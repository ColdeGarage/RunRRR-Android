package com.example.android.run;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MissionPopActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";
    private static int liveOrDie;
    private static String uid;
    private static String token;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_pop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) { selectImage();
//            }
//        });
        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImage.setVisibility(View.GONE);

        Bundle bundleReciever = getIntent().getExtras();
        String mName = bundleReciever.getString("name");
        String mTime = bundleReciever.getString("time");
        String mContent = bundleReciever.getString("content");
        String mType = bundleReciever.getString("type");
        String mState = bundleReciever.getString("state");
        uid = bundleReciever.getString("uid");
        token = bundleReciever.getString("token");
        String readDataFromHttp;

        //get liveOrdie
        MissionsFragment.MyTaskGet httpGetMember = new MissionsFragment.MyTaskGet();
        httpGetMember.execute("http://coldegarage.tech:8081/api/v1.1/member/read?operator_uid="+String.valueOf(uid)+"&token="+token+"&uid="+String.valueOf(uid));

        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGetMember.get();
            //Parse JSON info
            parseJson(readDataFromHttp,"member");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("liveOrDie"+liveOrDie);

        //missions type : MAIN,SUB,URG, set different icon
        ImageView Type = (ImageView) findViewById(R.id.list_type);
        switch (mType){
            case "0":
//                Type.setImageResource(R.drawable.missions_limit);
                break;
            case "1":
//                Type.setImageResource(R.drawable.missions_main_2);
                break;
            case "2":
//                Type.setImageResource(R.drawable.missions_sub);
                break;
            default:
                break;
        }

        //state type : -1:unsolved 0:being judged 1:success 2:fail
        switch(mState){
            case "-1":
                break;
            case "0": //waiting
                break;
            case "1": //passed
                break;
            case "2": //failed
                break;
            default:
                break;
        }

        // Set title with mission name
        TextView Name = (TextView) findViewById(R.id.list_name);
        Name.setText(mName);

        // Set content of mission details
        TextView Content = (TextView) findViewById(R.id.mission_content);
        Content.setText(mContent);

        // Set picture of mission details
        ImageView Picture = (ImageView) findViewById(R.id.mission_picture);
        Picture.setImageResource(R.drawable.yichun8787);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //====================取得任務頁面顯示的內容===========================
    //Parse json received from server
    void parseJson (String info, String missionOrReport){
        try {
//                    System.out.println(info);
            JSONObject payload = new JSONObject(new JSONObject(info).getString("payload"));
            JSONArray objects = payload.getJSONArray("objects");
            JSONObject subObject = objects.getJSONObject(0);
            liveOrDie = subObject.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //====================上傳任務照片===========================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MissionPopActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MissionPopActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
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

        ivImage.setImageBitmap(thumbnail);
        ivImage.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
        ivImage.setVisibility(View.VISIBLE);
    }
}
