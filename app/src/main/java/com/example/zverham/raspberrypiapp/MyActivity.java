package com.example.zverham.raspberrypiapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.*;

import android.os.AsyncTask;


public class MyActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void exampleMethod(View view) {
        TextView text = (TextView) findViewById(R.id.loadingText);
        EditText ipField = (EditText) findViewById(R.id.enter_ip);
        EditText rField = (EditText) findViewById(R.id.enter_r);
        EditText gField = (EditText) findViewById(R.id.enter_g);
        EditText bField = (EditText) findViewById(R.id.enter_b);
        EditText idField = (EditText) findViewById(R.id.enter_light_id);

        int id = Integer.parseInt(idField.getText().toString());
        int red = Integer.parseInt(rField.getText().toString());
        int green = Integer.parseInt(gField.getText().toString());
        int blue = Integer.parseInt(bField.getText().toString());
        JSONObject jo = new JSONObject();
        try {
            JSONObject internalObject = new JSONObject();
            internalObject.put("lightId", id);
            internalObject.put("red", red);
            internalObject.put("green", green);
            internalObject.put("blue", blue);
            internalObject.put("intensity", 1);
            JSONArray internalArray = new JSONArray();
            internalArray.put(internalObject);

            jo.put("lights", internalArray);
            jo.put("propagate", false);
            text.setText(jo.toString());
        } catch(JSONException e) {
            System.out.println(e);
        }

        String address = ipField.getText().toString();
        address = "http://" + address + "/rpi";

        JSONObject add = new JSONObject();
        try {
            add.put("address", address);
        } catch (Exception e) {
            System.out.println(e);
        }

        JSONObject[] insertArray = new JSONObject[2];
        insertArray[0] = jo;
        insertArray[1] = add;

        new Connection().execute(jo, add);

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
