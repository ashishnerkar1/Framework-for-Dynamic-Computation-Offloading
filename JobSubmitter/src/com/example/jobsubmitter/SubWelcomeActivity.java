package com.example.jobsubmitter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class SubWelcomeActivity extends Activity {

	EditText etip;
	Button btn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_welcome);
        
        etip = (EditText)findViewById(R.id.etIP);
        btn = (Button)findViewById(R.id.btnWelcomeOK);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent ii = new Intent(arg0.getContext(),MainMenuActivity.class);
				ii.putExtra("ip", etip.getText().toString());
				startActivity(ii);
				finish();
			}
		});
    }
}
