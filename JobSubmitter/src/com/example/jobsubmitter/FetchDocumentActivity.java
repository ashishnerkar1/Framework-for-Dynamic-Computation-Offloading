package com.example.jobsubmitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import MyPack.Base64;
import MyPack.Chunk;
import MyPack.ChunkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class FetchDocumentActivity extends Activity {
	private static final String NAMESPACE = "http://MyPack/";
//	private String URL = "http://0:8095/DMCCCloud/DMCCService?wsdl";//"http://localhost:8080/WebApplication1/NewWebService?wsdl";	
	
	private String URL = "http://env-6365459.j.layershift.co.uk/DMCCCloud/DMCCService?wsdl";
	
	private static final String SOAP_ACTION = "DMCCService";
	ProgressBar pg;
	String ip = "";
	public int status=0;
	int chunkSize = 0, chunkID;
	FileOutputStream fos;
	LongOperation lo;
	String errorStr = "";
	Handler h;
	int tid = 0;
	boolean cancel = false;
	String DOCNAME = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch_document);
        
        Bundle extras = getIntent().getExtras();
        if(extras != null){
        	tid = extras.getInt("tid");
        	ip = extras.getString("ip");
        }
    //    URL = "http://" + ip + ":8095/DMCCCloud/DMCCService?wsdl";
        
        
        h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				if(!errorStr.equals("")){
					Toast.makeText(FetchDocumentActivity.this, errorStr, Toast.LENGTH_SHORT).show();
					cancel = true;
					Intent ii = new Intent(FetchDocumentActivity.this, ViewActivity.class);
					ii.putExtra("iname", Environment.getExternalStorageDirectory() + "/My Faxes/" + DOCNAME);
					startActivity(ii);
					finish();
				}
				
				if(!cancel)	h.postAtTime(this, SystemClock.uptimeMillis() + 500);
			}
		});
        
        
        pg = (ProgressBar)findViewById(R.id.progressBarDownload);
        
		lo = new LongOperation();
		lo.execute("");
        
    }

    
    private class LongOperation extends AsyncTask<String, Void, String> {

		@Override 
		protected String doInBackground(String... params) {
			System.out.println("In doInBackground");
			
			
			System.out.println("In Pre-Execute");
			status = 1;
			ChunkInfo cinfo = new ChunkInfo();
			SoapObject request = new SoapObject(NAMESPACE, "initializeFetch");

	    	SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 

			PropertyInfo pi = new PropertyInfo();
			pi.setName("ServerInput");
	        pi.setValue(objectToString(tid));
	        pi.setType(String.class);
	        
	        request.addProperty(pi);
	        
	        envelope.setOutputSoapObject(request);
			
	        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);

				SoapObject resultsRequestSOAP = (SoapObject)envelope.bodyIn;
				
				String resp = resultsRequestSOAP.getPrimitivePropertyAsString("return");
				
				cinfo = (ChunkInfo)stringToObject(resp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			DOCNAME = cinfo.filename;
			try{
				fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/My Faxes/" + DOCNAME));
			}catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error : " + e);
			}
			
			System.out.println("Initialize response : " + cinfo.currentChunk + " " + cinfo.totalChunks);
			chunkSize = cinfo.totalChunks;
			pg.setMax(cinfo.totalChunks);
			pg.setProgress(0);
			
			

			for(int i=0;i<chunkSize;i++){
				Chunk c = new Chunk();
				request = new SoapObject(NAMESPACE, "FetchChunk");
				c.uid = DOCNAME;
				c.chunkID = i;
		    	envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 

				pi = new PropertyInfo();
				pi.setName("ServerInput");
		        pi.setValue(objectToString(c));
		        pi.setType(String.class);
		        
		        request.addProperty(pi);
		        
		        envelope.setOutputSoapObject(request);
				
		        androidHttpTransport = new HttpTransportSE(URL);
				
				try {
					androidHttpTransport.call(SOAP_ACTION, envelope);

					SoapObject resultsRequestSOAP = (SoapObject)envelope.bodyIn;
					
					String resp = resultsRequestSOAP.getPrimitivePropertyAsString("return");
					
					c = (Chunk)stringToObject(resp);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try{
					fos.write(c.data);
				}catch (Exception e) {
					// TODO: handle exception
					System.out.println("Error Writing Data : " + e);
				}
				pg.setProgress(i+1);
			}
			try{
				fos.close();
			}catch (Exception e) {
				// TODO: handle exception
			}
			
			errorStr = "File Downloaded Successfully!";
			
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			System.out.println("In Post-Execute");
			//String finalResponse = (String)callService(objectToString(c), "finalize", "ServerInput");
			//System.out.println("Final Response : " + finalResponse);
			//Toast.makeText(FetchDocumentActivity.this, "Download Process Completed Successfully!", Toast.LENGTH_SHORT).show();
			pg.setProgress(0);
			
		}

		@Override
		protected void onPreExecute() {
			
			
		}
	}


    Object stringToObject(String inp){
        byte b[] = Base64.decode(inp);
        Object ret = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(b);
            ObjectInput in = new ObjectInputStream(bis);
            ret = (Object) in.readObject(); 
            bis.close();
            in.close();
        } catch(Exception e) {
            System.out.println("NOT DE-SERIALIZABLE: " + e);
        }
        return ret;
    }
    
    String objectToString(Object obj){
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);   
            out.writeObject(obj);
            b = bos.toByteArray();
        } catch(Exception e) {
            System.out.println("NOT SERIALIZABLE: " + e);
        }         
        return Base64.encode(b);
    }

}
