package com.example.android.run;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MissionPopActivity extends AppCompatActivity {
    public static int REQUEST_CAMERA = 0, SELECT_FILE = 1, RESELECT_FILE = 2, UPLOAD_FROM_GALLERY = 3;

    private static int liveOrDie;
    private static int rid;
    private static String photoUrl;
    private static String uid;
    private static String token;

    private String mid;
    private String mName;
    private String mTime;
    private String mType;
    private String mState;
    private String mContent;
    private String mPrize;
    private String mScore;
    private String mUrl;

    private LinearLayout list;
    private TextView type;
    private TextView name;
    private TextView time;
    private ImageView state;
    private TextView content;
    private ImageView picture;
    private TextView btnSelect;
    private TextView btnCancel;

    private String readDataFromHttp;
    private ImageView selectedPhoto;
    private String photoPath;
    private Intent galleryData;
    private Intent photoData;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_pop);

        //initial
        rid = -1;
        photoUrl = null;

        list = (LinearLayout) findViewById(R.id.list_mission);
        type = (TextView) findViewById(R.id.list_type);
        name = (TextView) findViewById(R.id.list_name);
        time = (TextView) findViewById(R.id.list_time);
        state = (ImageView) findViewById(R.id.list_state);
        content = (TextView) findViewById(R.id.mission_content);
        picture = (ImageView) findViewById(R.id.mission_picture);
        selectedPhoto = (ImageView) findViewById(R.id.select_mission_photo);
        btnSelect = (TextView) findViewById(R.id.photoSelectButton);
        btnCancel = (TextView) findViewById(R.id.popCancelButton);

        Bundle bundleReciever = getIntent().getExtras();
        mName = bundleReciever.getString("title");
        mTime = bundleReciever.getString("time_end");
        mContent = bundleReciever.getString("content");
        mType = bundleReciever.getString("class");
        mState = bundleReciever.getString("state");
        mContent = bundleReciever.getString("content");
        mPrize = bundleReciever.getString("prize");
        mScore = bundleReciever.getString("score");
        mUrl = bundleReciever.getString("url");
        uid = bundleReciever.getString("uid");
        token = bundleReciever.getString("token");

        final Resources resources = getResources();

        //get liveOrdie
        MissionsFragment.MyTaskGet httpGetMember = new MissionsFragment.MyTaskGet();
        httpGetMember.execute(resources.getString(R.string.apiURL)+"/member/read?operator_uid="+uid+"&token="+token+"&uid="+uid);

        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGetMember.get();
            //Parse JSON info
            parseJson(readDataFromHttp, "member");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("liveOrDie = " + liveOrDie);

        //get missionPhoto
        MissionsFragment.MyTaskGet httpGetReport = new MissionsFragment.MyTaskGet();
        httpGetReport.execute(resources.getString(R.string.apiURL)+"/report/read?operator_uid="+uid+"&token="+token+"&uid="+uid);

        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGetReport.get();
            //Parse JSON info
            parseJson(readDataFromHttp, "report");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set mission title and content
        name.setText(mName);
        time.setText(mTime);
        content.setText(mContent+"\n獎勵分數:"+mScore+"\n獎勵金錢:"+mPrize);
        picture.setVisibility(View.GONE);
        selectedPhoto.setVisibility(View.GONE);
        btnSelect.setVisibility(View.GONE);

        if(mUrl != null && !mUrl.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap mBitmap =
                            getBitmapFromURL(resources.getString(R.string.apiURL)+"/download/img/" + mUrl);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mBitmap!=null) {
                                picture.setImageBitmap(mBitmap);
                                picture.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();
        }

        if(photoUrl != null && !photoUrl.equals("")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap mBitmap =
                            getBitmapFromURL(resources.getString(R.string.apiURL)+"/download/img/" + photoUrl);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mBitmap!=null) {
                                selectedPhoto.setImageBitmap(mBitmap);
                                selectedPhoto.setVisibility(View.VISIBLE);
                            }
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
                if(liveOrDie == 1) { //live
                    btnSelect.setVisibility(View.VISIBLE);
                }
                break;
            case "0": //being judged
                state.setBackgroundResource(R.drawable.anim_gif_waiting);
                Object ob_waiting = state.getBackground();
                AnimationDrawable anim_waiting = (AnimationDrawable) ob_waiting;
                anim_waiting.start();
                break;
            case "1": //success
                state.setImageResource(R.drawable.state_passed);
                break;
            case "2": //fail
                if(liveOrDie == 1){ //live
                    btnSelect.setVisibility(View.VISIBLE);
                }
                state.setImageResource(R.drawable.state_failed);
                break;
            default:
                break;
        }

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()) {
                    Alert("please check your internet connection");
                }
                else {
                    selectImage();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(MissionsFragment.MY_MISSION_REFRESH, intent);
        finish();
    }

    //====================取得任務頁面顯示的內容====================
    //Parse json received from server
    private void parseJson (String info, String missionOrReport){
        if(missionOrReport.equals("member")) {
            try {
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                JSONArray objects = payload.getJSONArray("objects");
                JSONObject subObject = objects.getJSONObject(0);
                liveOrDie = subObject.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(missionOrReport.equals("report")) {
            System.out.println(info);
            try {
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                JSONArray objects = payload.getJSONArray("objects");
                int LENGTH = objects.length();
                for(int i=0; i<LENGTH; i++) {
                    JSONObject subObject = objects.getJSONObject(i);
                    if(subObject.getString("mid").equals(mid)) {
                        rid = subObject.getInt("rid");
                        photoUrl = subObject.getString("url");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            case Utility.MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;

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
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result;

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    result = Utility.checkPermission(Utility.MY_PERMISSIONS_REQUEST_CAMERA, MissionPopActivity.this);
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    result = Utility.checkPermission(Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE, MissionPopActivity.this);
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                galleryData = data;
                Intent intent = new Intent(MissionPopActivity.this, CheckPickFromGalleryActivity.class);
                startActivityForResult(intent, SELECT_FILE);
            } else if (requestCode == REQUEST_CAMERA) {
                photoData = data;
                Intent intent = new Intent(MissionPopActivity.this, CheckPickFromGalleryActivity.class);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
        else if(resultCode == RESELECT_FILE) {
            if (requestCode == SELECT_FILE) {
                galleryIntent();
            } else if (requestCode == REQUEST_CAMERA) {
                cameraIntent();
            }
        }
        else if(resultCode == UPLOAD_FROM_GALLERY) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult();
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult();
            }
        }
    }

    private void onCaptureImageResult() {
        Bitmap thumbnail = (Bitmap) photoData.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        MediaStore.Images.Media.insertImage(getContentResolver(), thumbnail,
                "Runrrr"+mid+System.currentTimeMillis() + ".jpg", "this is what you take haha");

        MissionPost(thumbnail);
        selectedPhoto.setImageBitmap(thumbnail);
        selectedPhoto.setVisibility(View.VISIBLE);
        btnSelect.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult() {
        Bitmap bm = null;
        if (galleryData != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), galleryData.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        MissionPost(bm);
        selectedPhoto.setImageBitmap(bm);
        selectedPhoto.setVisibility(View.VISIBLE);
        btnSelect.setVisibility(View.GONE);
    }

    private void MissionPost(Bitmap bitmap){
        if(mState.equals("2")) { //state type : -1:unsolved 0:being judged 1:success 2:fail
            //Convert Bitmap to String for "POST"
            photoPath = BitmapToString(bitmap);

            //POST mid&image to server
            MyTaskPut httpPut = new MyTaskPut();
            httpPut.execute(getResources().getString(R.string.apiURL)+"/report/edit"
                    ,"uid="+uid+"&operator_uid="+uid+"&token="+token+"&rid="+rid+"&image="+photoPath);

            try {
                //get result from function "onPostExecute" in class "myTaskPost"
                readDataFromHttp = httpPut.get();
                System.out.println(readDataFromHttp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Convert Bitmap to String for "POST"
            photoPath = BitmapToString(bitmap);

            //POST mid&image to server
            MyTaskPost httpPost = new MyTaskPost();
            httpPost.execute();

            try {
                //get result from function "onPostExecute" in class "myTaskPost"
                readDataFromHttp = httpPost.get();
                System.out.println(readDataFromHttp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Alert("上傳成功");
        mState = "0";
        state.setImageBitmap(null);
        state.setBackgroundResource(R.drawable.anim_gif_waiting);
        Object ob_waiting = state.getBackground();
        AnimationDrawable anim_waiting = (AnimationDrawable) ob_waiting;
        anim_waiting.start();
    }

    private String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte [] b = baos.toByteArray();
        String bitmapString = Base64.encodeToString(b, Base64.DEFAULT);
        String encodeURL = null;
        try {
            encodeURL = URLEncoder.encode(bitmapString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeURL;
    }

    //show an alert dialog
    private void Alert(String mes) {
        new AlertDialog.Builder(MissionPopActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //HTTPPost
    class MyTaskPost extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try {
                url = new URL(getApplicationContext().getResources().getString(R.string.apiURL) + "/report/create");
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("POST");

                //設置輸出入流串
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //POST方法不能緩存數據,需手動設置使用緩存的值為false
                urlConnection.setUseCaches(false);

                //Send request
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write("uid=" + uid + "&token=" + token + "&operator_uid=" + uid + "&mid=" + mid + "&image=" + photoPath);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line ;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
                // close the reader; this can throw an exception too, so wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    class MyTaskPut extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... arg0) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];
            String para = arg0[1];

            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("PUT");

                //設置輸出入流串
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //POST方法不能緩存數據,需手動設置使用緩存的值為false
                urlConnection.setUseCaches(false);

                //Send request
                DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write(para);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();
                String line ;

                while ((line = reader.readLine()) != null)     {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}