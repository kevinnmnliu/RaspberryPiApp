package com.example.zverham.raspberrypiapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.widget.*;


public class HomePage extends ActionBarActivity {

    private DbxFileSystem dbxFs;
    private static DbxAccountManager mDbxAcctMgr;
    static final int REQUEST_LINK_TO_DBX = 0;

    private Camera mCamera;
    private CameraPreview mPreview;

    private static String latest_image_uri;
    private static boolean is_dropbox = false;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private EditText ipField;
    private LinearLayout dbSettings;
    private Button loginButton;
    private Button logoutButton;
    private TextView dbPlaceholder;

    private static String[] mFileList;
    private static File mPath = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "MyCameraApp");

    private static ArrayList<String> dFileArray;
    private static String[] dFileList;

    private static String mChosenFile;
    private static final String FTYPE = ".jpg";
    private static final int DIALOG_LOAD_FILE = 1000;

    private static final String sync_path = "/Dropbox/";

    /**
     * Create a File for saving an image or video
     */
    private static void loadFileList() {
        try {
            mPath.mkdirs();
        } catch (SecurityException e) {

        }
        if (mPath.exists()) {

            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }
            };
            String templist[] = mPath.list(filter);
            mFileList = new String[templist.length + 1];
            mFileList[0] = mPath.getParent();
            for (int x = 0; x < mFileList.length - 1; x++) {
                mFileList[x + 1] = templist[x];
            }
        } else {

            mFileList = new String[0];
        }
    }


    /* DROPBOX! */
    public void onClickLinkToDropbox(View v) {

        if (mDbxAcctMgr.getLinkedAccount() == null) {
            mDbxAcctMgr.startLink((Activity) this, REQUEST_LINK_TO_DBX);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "You are already logged in!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onClickLogoutFromDropbox(View v) {
        if (mDbxAcctMgr.getLinkedAccount() == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "You are not logged in!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            mDbxAcctMgr.getLinkedAccount().unlink();
            logoutButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            dbPlaceholder.setText("Sign in to dropbox...");
            Toast toast = Toast.makeText(getApplicationContext(), "You are logged out!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void getDropBoxFiles(DbxPath path) {
        System.out.println("getting dropbox files...");
        try {

            if (dbxFs != null) {
                List<DbxFileInfo> currentList = dbxFs.listFolder(path);
                for (int i = 0; i < currentList.size(); i++) {
                    DbxFileInfo currentInfo = currentList.get(i);
                    if (currentInfo.isFolder) {
                        getDropBoxFiles(currentInfo.path);
                    } else {
//                        Toast toast = Toast.makeText(getApplicationContext(), currentInfo.path.toString(), Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
//                        toast.show();
                        dFileArray.add(currentInfo.path.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        dFileList = new String[dFileArray.size()];
        for (int i = 0; i < dFileArray.size(); i++) {
            dFileList[i] = dFileArray.get(i);
        }
    }

    public void listenForChangesOnPath(DbxPath path) {
        Toast toast = Toast.makeText(getApplicationContext(), "IN METHOD!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
        toast.show();
        try {
            dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
//            dbxFs.syncNowAndWait();
//            System.out.println("!!!!!!!!!!!!!!!!");
        } catch (Exception e) {
            System.out.println(e);
        }
        dbxFs.addPathListener(new DbxFileSystem.PathListener() {
            @Override
            public void onPathChange(DbxFileSystem fs, DbxPath registeredPath, Mode registeredMode) {
                Toast toast = Toast.makeText(getApplicationContext(), "CREATED!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
                System.out.println("IN ON PATH CHANGE!!!!!");
                try {
                    FileInputStream fis = fs.open(registeredPath).getReadStream();
                    File f = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    byte[] b = new byte[(int) f.length()];
                    FileOutputStream fos = new FileOutputStream(f);
                    fis.read(b);
                    fos.write(b);
                    fos.close();
                    fis.close();
                    System.out.println(registeredPath.toString());
                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }, path, DbxFileSystem.PathListener.Mode.PATH_OR_DESCENDANT);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                Toast toast = Toast.makeText(getApplicationContext(), "You are logged in!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
                System.out.println("sign in successful");
                dbSettings.setVisibility(View.GONE);
                dbPlaceholder.setText("Welcome!");
                loginButton.setVisibility(View.GONE);
                logoutButton.setVisibility(View.VISIBLE);
//                listenForChangesOnPath(new DbxPath(sync_path));
                try {
                    dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
                } catch (Exception e) {
                    System.out.println(e);
                }

            } else {
                System.out.println("sign in unsuccessful");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /* ----------- */

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            latest_image_uri = mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {

            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
            is_dropbox = false;
            startNewIntent();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mCamera = getCameraInstance();

            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            // Add a listener to the Capture button
            Button captureButton = (Button) findViewById(R.id.take_picture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera


                            v.setBackgroundResource(R.drawable.round_button_pressed);
                            mCamera.takePicture(null, null, mPicture);


                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.take_picture).setBackgroundResource(R.drawable.round_button);
//        mCamera = getCameraInstance();
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //releaseCamera();              // release the camera immediately on pause event
        //mPreview.finish();

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        findViewById(R.id.take_picture).setBackgroundResource(R.drawable.round_button);
//    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

//    public static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//    }
//
//    public void setupUI(View view) {
//
//        //Set up touch listener for non-text box views to hide keyboard.
//        if(!(view instanceof EditText) && !(view instanceof Button)) {
//
//            view.setOnTouchListener(new OnTouchListener() {
//
//                public boolean onTouch(View v, MotionEvent event) {
//                    hideSoftKeyboard(HomePage.this);
//                    return false;
//                }
//
//            });
//        }
//
//        //If a layout container, iterate over children and seed recursion.
//        if (view instanceof ViewGroup) {
//
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//
//                View innerView = ((ViewGroup) view).getChildAt(i);
//
//                setupUI(innerView);
//            }
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "ii0pnl9e1abqnj1", "4szs0iuyfmte9r0");

        setContentView(R.layout.page_home);
        ipField = (EditText) findViewById(R.id.ipField);
        dbSettings = (LinearLayout) findViewById(R.id.db_settings);
        loginButton = (Button) findViewById(R.id.login_button);
        logoutButton = (Button) findViewById(R.id.logout_button);
        dbPlaceholder = (TextView) findViewById(R.id.db_placeholder);
        if (mDbxAcctMgr.getLinkedAccount() == null) {
            logoutButton.setVisibility(View.GONE);
        } else {
            loginButton.setVisibility(View.GONE);
            dbPlaceholder.setText("Welcome!");
        }
        dbSettings.setVisibility(View.GONE);
        ipField.setVisibility(View.GONE);
        findViewById(R.id.take_picture).setBackgroundResource(R.drawable.round_button);
        if (mDbxAcctMgr.hasLinkedAccount()) {
            listenForChangesOnPath(new DbxPath(sync_path));
        }
        dFileArray = new ArrayList<String>();
//        setupUI(findViewById(R.id.home_page));

    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {

            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
            }
        }

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
                toggleIP();
                break;
            case R.id.db_signin:
                toggleDB();
                break;
            case R.id.file_system:
                toggleFileSystem();
                break;
            case R.id.db_files:
                try {
                    dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
                } catch (Exception e) {
                    System.out.println(e);
                }
                dFileArray.clear();
                getDropBoxFiles(DbxPath.ROOT);
                DialogFragment newFragment = new DropBoxDialogFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                newFragment.show(fragmentManager, "picker");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void toggleFileSystem() {
        loadFileList();
        DialogFragment newFragment = new FilePickerDialogFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        newFragment.show(fragmentManager, "picker");
    }

    public void toggleDB() {
        LinearLayout dbSettings = (LinearLayout) findViewById(R.id.db_settings);
        if (dbSettings.getVisibility() != View.GONE) {
            dbSettings.setVisibility(View.GONE);
        } else {
            dbSettings.setVisibility(View.VISIBLE);
        }
    }

    public void toggleIP() {
        EditText ipField = (EditText) findViewById(R.id.ipField);
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (ipField.getVisibility() != View.GONE) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            ipField.setVisibility(View.GONE);
        } else {

            ipField.setVisibility(View.VISIBLE);
            ipField.requestFocus();
            inputMethodManager.showSoftInput(ipField, InputMethodManager.SHOW_IMPLICIT);

        }
    }

    public void startNewIntent() {
        EditText ipField = (EditText) findViewById(R.id.ipField);
        String address = ipField.getText().toString();
        Intent intent = new Intent(this, ConfirmImage.class);
        intent.putExtra("latest_image_uri", latest_image_uri);
        intent.putExtra("rpi_ip", address);
        intent.putExtra("is_dropbox", is_dropbox);
        startActivity(intent);
    }

    public static class FilePickerDialogFragment extends DialogFragment {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose a file");
            if (mFileList == null) {
                dialog = builder.create();
                Toast.makeText(getActivity().getApplicationContext(), "My list is null", Toast.LENGTH_LONG);

                return dialog;

            }
            GridView gridView = new GridView(getActivity().getApplicationContext());
            gridView.setAdapter(new ImageAdapter(getActivity(), mFileList, mPath, false));
            gridView.setNumColumns(5);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                    String chosenFile = mFileList[which];
                    if (new File(mPath, chosenFile).isDirectory() || which == 0) {
                        if (which == 0) {
                            mPath = new File(chosenFile);
                        } else {
                            mPath = new File(mPath, chosenFile);
                        }
                        loadFileList();

                        DialogFragment newFragment = new FilePickerDialogFragment();
                        FragmentManager fragmentManager = (getActivity()).getSupportFragmentManager();
                        newFragment.show(fragmentManager, "picker");
                    } else {
                        latest_image_uri = new File(mPath, chosenFile).toString();
                        is_dropbox = false;
                        ((HomePage) getActivity()).startNewIntent();
                    }
                    dismiss();
                }
            });
            builder.setView(gridView);


            dialog = builder.show();
            return dialog;
        }

    }

    public static class DropBoxDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = null;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose a file from Dropbox.");
            if (dFileList == null) {
                dialog = builder.create();
                Toast.makeText(getActivity().getApplicationContext(), "My list is null", Toast.LENGTH_LONG);

                return dialog;

            }
            GridView gridView = new GridView(getActivity().getApplicationContext());
            gridView.setAdapter(new ImageAdapter(getActivity(), dFileList, mPath, true));
            gridView.setNumColumns(5);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {


                    latest_image_uri = dFileList[which];
                    is_dropbox = true;
                    ((HomePage) getActivity()).startNewIntent();


                    dismiss();
                }

            });

            builder.setView(gridView);
            dialog = builder.show();
            return dialog;
        }

    }
}

