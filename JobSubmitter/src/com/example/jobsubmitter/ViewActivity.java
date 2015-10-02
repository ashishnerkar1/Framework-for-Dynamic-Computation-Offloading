package com.example.jobsubmitter;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class ViewActivity extends Activity {

	ImageView iv;
	String iName = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        iv = (ImageView)findViewById(R.id.IV);
        Bundle ext = getIntent().getExtras();
        if(ext != null){
        	iName = ext.getString("iname");
        }
        BitmapDrawable bd = (BitmapDrawable) BitmapDrawable.createFromPath(iName);
        iv.setImageDrawable(bd);

    }

    
}
