package com.example.android.run;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MissionPopActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";
    private static int liveOrDie;
    private static String uid;
    private static String token;

    private Bundle bundleReciever;
    private String mName;
    private String mTime;
    private String mUrl;
    private String mContent;
    private String mType;
    private String mState;
    private String readDataFromHttp;

    private LinearLayout list;
    private TextView type;
    private TextView name;
    private TextView time;
    private ImageView state;
    private TextView content;
    private ImageView picture;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_pop);
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

        bundleReciever = getIntent().getExtras();
        mName = bundleReciever.getString("name");
        mTime = bundleReciever.getString("time");
        mContent = bundleReciever.getString("content");
        mType = bundleReciever.getString("type");
        mState = bundleReciever.getString("state");
        mUrl = bundleReciever.getString("url");
        uid = bundleReciever.getString("uid");
        token = bundleReciever.getString("token");

        //get liveOrdie
        MissionsFragment.MyTaskGet httpGetMember = new MissionsFragment.MyTaskGet();
        httpGetMember.execute("http://coldegarage.tech:8081/api/v1.1/member/read?operator_uid="+String.valueOf(uid)+"&token="+token+"&uid="+String.valueOf(uid));

        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGetMember.get();
            //Parse JSON info
            parseJson(readDataFromHttp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("liveOrDie" + liveOrDie);

        list = (LinearLayout) findViewById(R.id.list_mission);
        type = (TextView) findViewById(R.id.list_type);
        name = (TextView) findViewById(R.id.list_name);
        time = (TextView) findViewById(R.id.list_time);
        state = (ImageView) findViewById(R.id.list_state);
        content = (TextView) findViewById(R.id.mission_content);
        picture = (ImageView) findViewById(R.id.mission_picture);

        // Set mission title and content
        name.setText(mName);
        System.out.println("name!!!!!");
        content.setText(mContent);
        System.out.println("url===========" + mUrl);
        if(mUrl != null && mUrl!="") {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO Auto-generated method stub
                    final Bitmap mBitmap =
                            getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + mUrl);
//                    final Bitmap circularBitmap = ImageConverter.getRoundedCornerBitmap(mBitmap, 30);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            picture.setImageBitmap(circularBitmap);
                            picture.setImageBitmap(mBitmap);
                        }
                    });

                }
            }).start();
        }
        //missions type : MAIN,SUB,URG, set different icon
        switch (mType) {
            case "0":
                type.setText("限");
                type.setTextColor(ContextCompat.getColor(this, R.color.limit));
                list.setBackgroundResource(R.color.limit);
                break;
            case "1":
                type.setText("主");
                type.setTextColor(ContextCompat.getColor(this, R.color.main));
                list.setBackgroundResource(R.color.main);
                break;
            case "2":
                type.setText("支");
                type.setTextColor(ContextCompat.getColor(this, R.color.sub));
                list.setBackgroundResource(R.color.sub);
                break;
            default:
                break;
        }

        //state type : -1:unsolved 0:being judged 1:success 2:fail
        switch (mState) {
            case "-1":
                break;
            case "0": //waiting
                state.setImageResource(R.drawable.state_waiting);
                break;
            case "1": //passed
                state.setImageResource(R.drawable.state_passed);
                break;
            case "2": //failed
                state.setImageResource(R.drawable.state_failed);
                break;
            default:
                break;
        }
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
    void parseJson (String info){
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

    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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