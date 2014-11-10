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

import java.net.URI;
import android.net.Uri;

import android.widget.ImageView;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

public class ConfirmImage extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_confirm);
        Intent intent = getIntent();
        ImageView jpgView = (ImageView)findViewById(R.id.image_preview);
        String myJpgPath = intent.getStringExtra("latest_image_uri");
        Toast.makeText(getApplicationContext(),myJpgPath, Toast.LENGTH_LONG).show();
        BitmapDrawable d = new BitmapDrawable(getResources(), myJpgPath);
        jpgView.setImageDrawable(d);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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
        Intent intent = new Intent(this, AwaitingResponse.class);
        startActivityForResult(intent, 2);
    }


}
