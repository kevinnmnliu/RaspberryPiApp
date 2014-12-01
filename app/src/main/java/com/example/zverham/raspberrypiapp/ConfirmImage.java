package com.example.zverham.raspberrypiapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.content.Intent;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.Gravity;

import java.net.URI;
import android.net.Uri;

import java.io.File;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap.CompressFormat;
import java.io.FileOutputStream;

import android.widget.ImageView;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import android.graphics.*;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class ConfirmImage extends ActionBarActivity {

    private DbxAccountManager mDbxAcctMgr;
    DbxFileSystem dbxFs;
    private Intent _intent;
    private boolean is_dropbox = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "ii0pnl9e1abqnj1", "4szs0iuyfmte9r0");
        setContentView(R.layout.image_confirm);
        Intent intent = getIntent();
        _intent = intent;
        ImageView jpgView = (ImageView)findViewById(R.id.image_preview);
        String myJpgPath = intent.getStringExtra("latest_image_uri");
        String ipAddress = intent.getStringExtra("rpi_ip");
        is_dropbox = intent.getBooleanExtra("is_dropbox", false);
        Toast toast = Toast.makeText(getApplicationContext(),"Sending to: "+ipAddress, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 0);
        toast.show();

        if(is_dropbox) {
            try {
                dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
                DbxFile file = dbxFs.open(new DbxPath(intent.getStringExtra("latest_image_uri")));
                BitmapDrawable d = new BitmapDrawable(getResources(), file.getReadStream());
                d.setGravity(android.view.Gravity.FILL_HORIZONTAL);
                jpgView.setImageDrawable(d);
                file.close();
            } catch(Exception e){
                    System.out.println("exception!");
                    System.out.println(e);
            }

        } else {

                BitmapDrawable d = new BitmapDrawable(getResources(), myJpgPath);
                d.setGravity(android.view.Gravity.FILL_HORIZONTAL);
                jpgView.setImageDrawable(d);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_ip:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==2){
            finish();
        }
    }

    public void retake(View v) {
        finish();
    }

    public void submit(View v) {

//        String myJpgPath = _intent.getStringExtra("latest_image_uri");


//        // get the BufferedImage, using the ImageIO class
//        BitmapDrawable d = new BitmapDrawable(getResources(), myJpgPath);
//        Bitmap b = d.getBitmap();
//        int[][] rgbArray = marchThroughImage(b);
//        JSONObject lightObject = makeJSONLights(rgbArray);
//        try {
//            System.out.println(lightObject.toString(2));
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        String address = _intent.getStringExtra("rpi_ip");    //THIS WILL NEED TO BE CHANGED
//        address = "http://" + address + "/rpi";
//
//        JSONObject add = new JSONObject();
//        try {
//            add.put("address", address);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//
//        /* DROPBOX */
//        File f = new File(myJpgPath);
////        try {
////            f.createNewFile();
////            ByteArrayOutputStream bos = new ByteArrayOutputStream();
////            b.compress(CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
////            byte[] bitmapdata = bos.toByteArray();
////            FileOutputStream fos = new FileOutputStream(f);
////            fos.write(bitmapdata);
////            fos.flush();
////            fos.close();
////        } catch (Exception e) {
////            System.out.println(e);
////        }
//
//        try {
//            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
//            DbxFile testFile = dbxFs.create(new DbxPath(myJpgPath));
//            try {
//                testFile.writeFromExistingFile(f, false);
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//
//        new Connection().execute(lightObject, add);

        Intent intent = new Intent(this, AwaitingResponse.class);
        intent.putExtra("myJpgPath", _intent.getStringExtra("latest_image_uri"));
        intent.putExtra("rpi_ip", _intent.getStringExtra("rpi_ip"));
        intent.putExtra("is_dropbox", _intent.getBooleanExtra("is_dropbox", false));
        startActivityForResult(intent, 2);
    }

    public static JSONObject makeLight(int[] rgb, int id) {
        JSONObject light = new JSONObject();

        try {
            light.put("lightId", id);
            light.put("red", rgb[0]);
            light.put("green", rgb[1]);
            light.put("blue", rgb[2]);
            light.put("intensity", 1);
        } catch (Exception e) {
            System.out.println(e);
        }

        return light;
    }

    public static JSONObject makeJSONLights(int[][] rgb){

        // 3. build jsonObject

        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < 32*31; i++) {
                JSONObject light = makeLight(rgb[i], i%32);
                jsonObject.accumulate("lights", light);
            }
            jsonObject.put("propagate", true);
        } catch (Exception e) {
            System.out.println(e);
        }
        return jsonObject;
    }

    public int[] getPixelRGB(int pixel) {   //returns an int array filled with RGB values, in rgb order

        int[] rgbArray;
        rgbArray = new int[3];

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        rgbArray[0] = red;
        rgbArray[1] = green;
        rgbArray[2] = blue;

        return rgbArray;
    }

    private int[][] marchThroughImage(Bitmap image) {  //given a bufferimage, it goes through and returns an array of all the rgb values
        int w = image.getWidth();
        int h = image.getHeight();

        int size = w*h;
        int [][] returnArray = new int[100*100][];
        int counter = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int pixel = image.getPixel(j, i);
                int[] rgb = getPixelRGB(pixel);
                //System.out.println("test: "+rgb[0] + " "+rgb[1]+ " "+rgb[2]);
                returnArray[counter] = rgb;
                counter += 1;
            }
        }
        return returnArray;
    }

    // HTTP POST request
    private static Object sendPost(JSONObject jo, String path) throws Exception {

        //instantiates httpClient to make request
        DefaultHttpClient httpClient = new DefaultHttpClient();

        //url with the post data
        HttpPost httpPost = new HttpPost(path);

        //passes the results to a string builder/entity
        StringEntity se = new StringEntity(jo.toString());

        //sets the post request as the resulting string
        httpPost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        //Handles what is returned from the page
        ResponseHandler responseHandler = new BasicResponseHandler();
        return httpClient.execute(httpPost, responseHandler);
    }

    private class Connection extends AsyncTask<JSONObject, Void, Object> {

        @Override
        protected Object doInBackground(JSONObject... arg0) {
            try {
                System.out.println(arg0[1].getString("address"));
                sendPost(arg0[0], arg0[1].getString("address"));
            } catch(Exception e) {
                System.out.println(e);
            }

            return null;
        }



    }


}
