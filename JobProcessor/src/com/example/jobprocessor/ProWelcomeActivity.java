package com.example.jobprocessor;


import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ProWelcomeActivity extends Activity {

	EditText etip, etName;
	TextView tv;
	Button btn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_welcome);
        
        
        etName = (EditText)findViewById(R.id.etName);
        etip = (EditText)findViewById(R.id.etIP);
        btn = (Button)findViewById(R.id.btnWelcomeOK);
        tv = (TextView)findViewById(R.id.textViewRam);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent ii = new Intent(arg0.getContext(),MainMenuActivity.class);
				ii.putExtra("ip", etip.getText().toString());
				ii.putExtra("name", etName.getText().toString());
				startActivity(ii);
				finish();
			}
		});
        tv.setText(getTotalRAM());
    }
    public static String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = null;
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }
        return load;
    }
}
