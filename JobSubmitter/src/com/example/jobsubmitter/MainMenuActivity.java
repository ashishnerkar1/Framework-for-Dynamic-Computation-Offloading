package com.example.jobsubmitter;

import java.io.File;
import java.util.Vector;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class MainMenuActivity extends Activity {

	private String root="/sdcard";
	String searchQuery;
	int selectedIndex = -1;
	ListView lv;
	Button proceed, view;
	EditText searchText;
	ImageButton btnSearch;
	TextView tvSelDoc;
	Vector<SingleFile> sfv;
	String IP, DOCNAME, DOCPATH;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
        	IP = extras.getString("ip");
        }
        
        lv = (ListView)findViewById(R.id.lvDocumentNames);
        searchText = (EditText)findViewById(R.id.etSearchBox);
        btnSearch = (ImageButton)findViewById(R.id.imgBtnSearch);
        proceed = (Button)findViewById(R.id.btnSelectDocProceed);
        view = (Button)findViewById(R.id.btnSelectDocView);
        tvSelDoc = (TextView)findViewById(R.id.tvSelectedDocument);
        tvSelDoc.setText("No Document Selected!");
        searchQuery = "";
        
        sfv = new Vector<SingleFile>();
        
        search();
        
        btnSearch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				searchQuery = searchText.getText().toString();
				search();
			}
		});
        
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		selectedIndex = arg2;
        		tvSelDoc.setText("Selected File : " + sfv.elementAt(selectedIndex).fileName + "\nPath : " + sfv.elementAt(selectedIndex).filePath);
        		DOCNAME = sfv.elementAt(selectedIndex).fileName;
        		DOCPATH = sfv.elementAt(selectedIndex).filePath;
        	}
		});
        
        proceed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ii = new Intent(arg0.getContext(), SendDocumentActivity.class);
				ii.putExtra("ip", IP);
				ii.putExtra("docname", DOCNAME);
				ii.putExtra("docpath", DOCPATH);
				startActivity(ii);
			}
		});
        
        view.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent ii = new Intent(arg0.getContext(), ViewActivity.class);
				ii.putExtra("iname", DOCPATH);
				startActivity(ii);
			}
		});
        
    }

    void search(){
    	sfv.clear();
        
        getDir(new File(root));
        
        String[] fileList = new String[sfv.size()];
        System.out.println("Size : " + sfv.size());
        for(int i=0;i<sfv.size();i++){
        	fileList[i] = sfv.elementAt(i).fileName + "\n" + sfv.elementAt(i).filePath;
        }
        
        lv.setAdapter(new ArrayAdapter<String>(this,R.layout.listitem, R.id.file_name, fileList));
        //lv.invalidate();
        //lv.setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, PL));

    }
    
    void getDir(File dir) {
        try {
            File files[] = dir.listFiles();
            
            for(File file: files) {
                if(file.isFile()) {
                	if(searchQuery.equals("")){
                		if(file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".bmp")){
                			sfv.add(new SingleFile(file.getName(), file.getPath()));
                		}
                	}else{
	                	if(file.getName().toLowerCase().contains(searchQuery)) {
	                		if(file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".bmp")){
	                			sfv.add(new SingleFile(file.getName(), file.getPath()));
	                		}
	                    }
                	}

                }else if(file.isDirectory()) {
                    getDir(file);
                }
            }
        }catch(Exception e) {
            ;
        }
    }    

    


}
