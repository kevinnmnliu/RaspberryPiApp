package com.example.zverham.raspberrypiapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxAccountManager;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by zverham on 11/30/14.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mFileList;
    private File mPath;
    private boolean isDropbox;
    private DbxAccountManager mDbxAcctMgr;

    public ImageAdapter(Context c, String[] m, File mp, boolean id) {
        mContext = c;
        mFileList = m;
        mPath = mp;
        isDropbox = id;
        mDbxAcctMgr = DbxAccountManager.getInstance(mContext.getApplicationContext(), "ii0pnl9e1abqnj1", "4szs0iuyfmte9r0");
        System.out.println("bool: "+isDropbox);
    }

    public int getCount() {
        return mFileList.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int i, View convertView, ViewGroup parent) {
        if (!isDropbox) {
            System.out.println("in filesystem");
            ImageView imageView;
            TextView textView = new TextView(mContext);
            boolean isDirectory = false;
            //        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            textView.setLayoutParams(new GridView.LayoutParams(85, 85));
            textView.setPadding(8, 8, 8, 8);


            try {
                if (i != 0) {
                    if (!(new File(mPath, mFileList[i]).isDirectory())) {
                        Bitmap b = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mPath.toString() + "/" + mFileList[i]), 48, 48);
                        imageView.setImageBitmap(b);
                    } else {
                        textView.setText(mFileList[i]);
                        isDirectory = true;
                    }

                } else {
                    textView.setText(mFileList[i]);
                    isDirectory = true;
                }
            } catch (Exception e) {

                textView.setText(mFileList[i]);
                isDirectory = true;
            }
            if (isDirectory) {
                return textView;
            } else {
                return imageView;
            }

            // is dropbox
        } else {
            System.out.println("we did done hit da dropbox");
            ImageView imageView;
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);

            System.out.println("Another print " + mFileList[i]);

            try {
                DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
                DbxFile file = dbxFs.open(new DbxPath(mFileList[i]));
                Bitmap b = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(file.getReadStream()), 48, 48);
                imageView.setImageBitmap(b);
                System.out.println("This is the file we're trying to load " + mFileList[i]);
                file.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageView;
        }

    }
}
