package com.example.jobsubmitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SendDocumentActivity extends Activity {
	private static final String NAMESPACE = "http://MyPack/";
//	private String URL = "http://0:8095/DMCCCloud/DMCCService?wsdl";//"http://localhost:8080/WebApplication1/NewWebService?wsdl";	
	
	private String URL = "http://env-6365459.j.layershift.co.uk/DMCCCloud/DMCCService?wsdl"; 
	
	private static final String SOAP_ACTION = "DMCCService";
	String DOCNAME, DOCPATH;
	String ip = "";
	Button startUpload;
	ProgressBar pg;
	public int stat=0, tid = -2;
	int chunkSize = 0, chunkID;
	FileInputStream fin;
	LongOperation lo;
	boolean cancel = false;
	public boolean isServerProcessComplete = false;
	String errorStr = "";
	Handler h;
	TextView tvStatus;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_document);
        
        tvStatus = (TextView)findViewById(R.id.textViewStatus);
        
        Bundle extras = getIntent().getExtras();
        if(extras != null){
        	DOCNAME = extras.getString("docname");
        	DOCPATH = extras.getString("docpath");
        	ip = extras.getString("ip");
        }
  //      URL = "http://" + ip + ":8095/DMCCCloud/DMCCService?wsdl";
        
        h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(stat == 0){
					if(!errorStr.equals("")){
						tvStatus.setText("" + errorStr);
						Toast.makeText(SendDocumentActivity.this, errorStr, Toast.LENGTH_SHORT).show();
						errorStr = "";
						stat = 1;
						LongOperation2 lo2 = new LongOperation2();
						lo2.execute("");
						//cancel = true;
						//done();
					}
				}else if(stat == 1){
					if(!errorStr.equals("")){
						tvStatus.setText("" + errorStr);
						Toast.makeText(SendDocumentActivity.this, errorStr, Toast.LENGTH_SHORT).show();
						errorStr = "";
						stat = 2;
						cancel = true;
						done();
					}
				}
				
				
				if(!cancel) h.postAtTime(this, SystemClock.uptimeMillis() + 500);
			}
		});
        
        pg = (ProgressBar)findViewById(R.id.progressBarUpload);
        startUpload = (Button)findViewById(R.id.btnStartUpload);
        startUpload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				lo = new LongOperation();
				lo.execute("");
			}
		});
        
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	cancel = true;
    }
    
    private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			System.out.println("In doInBackground");
			
			System.out.println("In Pre-Execute");
			ChunkInfo cinfo = new ChunkInfo();
			cinfo.filename = DOCNAME;
			try{
				fin = new FileInputStream(new File(DOCPATH));
				int available = fin.available();
				chunkSize = available / 1024;
				int residualSize = available - (chunkSize * 1024);
				if(residualSize > 0){
					chunkSize++;
				}
			}catch (Exception e) {
				
				Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() of Senddocactiv", e);
				System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

				
				System.err.println("Error : " + e);
				errorStr = e.getMessage();
			}
			
			cinfo.totalChunks = chunkSize;
			System.out.println("Total Chunks : " + chunkSize);
			
			SoapObject request = new SoapObject(NAMESPACE, "initiateFileTransfer");

	    	SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 

			PropertyInfo pi = new PropertyInfo();
			pi.setName("ServerInput");
	        pi.setValue(objectToString(cinfo));
	        pi.setType(String.class);
	        
	        request.addProperty(pi);
	        
	        envelope.setOutputSoapObject(request);
			
	        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);

				SoapObject resultsRequestSOAP = (SoapObject)envelope.bodyIn;
				
				String resp = resultsRequestSOAP.getPrimitivePropertyAsString("return");
				
				String cidString = (String)stringToObject(resp);
				
				chunkID = Integer.parseInt(cidString);
				System.out.println("Initialize response : " + chunkID);
				int x=0;
				pg.setMax(cinfo.totalChunks);
				pg.setProgress(0);
			} catch (Exception e) {
				
				Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() of Senddocactiv", e);
				System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

				
				e.printStackTrace();
				errorStr = e.getMessage();
			}
			
			for(int i=0;i<chunkSize;i++){
				Chunk c = new Chunk();
				c.chunkID = chunkID;
				try{
					if(fin.available() > 1024){
						c.data = new byte[1024];
					}else{
						c.data = new byte[fin.available()];
					}
					fin.read(c.data);
				}catch (Exception e) {
					
					Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() after chunk", e);
					System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

					
					// TODO: handle exception
				}
				
		    	request = new SoapObject(NAMESPACE, "updateChunk");

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
					
					String updateResponse = (String) stringToObject(resp);
					
					System.out.println("Update Response : " + updateResponse);
					if(!updateResponse.equals("successful")){
						Toast.makeText(SendDocumentActivity.this, updateResponse, Toast.LENGTH_SHORT).show();
					}
					pg.setProgress(i+1);
					
				} catch (Exception e) {
					
					Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() Soap resp", e);
					System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

					
					e.printStackTrace();
				}
				
				//String updateResponse = (String)callService(objectToString(c), "updateChunk", "ServerInput");
				
			}
			
			
			System.out.println("In Post-Execute");
			Chunk c = new Chunk();
			c.chunkID = chunkID;
			
	    	request = new SoapObject(NAMESPACE, "finalize");

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
				
				String finalResponse = (String) stringToObject(resp);
				tid = Integer.parseInt(finalResponse);
				
				//System.out.println("Final Response : " + finalResponse);
				//Toast.makeText(SendDocumentActivity.this, "Upload Process Completed Successfully!", Toast.LENGTH_SHORT).show();
				pg.setProgress(0);
				//done();
				
			} catch (Exception e) {
				
				Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() after Soap request ", e);
				System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

				
				e.printStackTrace();
			}
			
			errorStr = "File Uploaded Successfully!\nPlease wait...\nYour request is being processed.";
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			
		}

		@Override
		protected void onPreExecute() {
			
		}
	}
    
    private class LongOperation2 extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			while(true){
				if(cancel == true){
					break;
				}
				SoapObject request = new SoapObject(NAMESPACE, "checkStatus");
	
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
					
					String statString = (String)stringToObject(resp);
					
					int serverStat = Integer.parseInt(statString);
				
			// Numberformat Exception Generated here		
					
					if(serverStat != 0){
						errorStr = "Request processed completely!\nPlease wait...\nFetching response.";
						break;
					}else{
						try{
							Thread.sleep(2500);
						}catch (Exception e) {
							Log.e("YOUR ERROR TAG HERE", "After sleep of thread", e);
							System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

							
						}
					}
					System.out.println("Initialize response : " + chunkID);
				} catch (Exception e) {

					Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() of Longoperation", e);
					System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

					
					e.printStackTrace();
					errorStr = e.getMessage();
					break;
				}
			}
			
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			
		}

		@Override
		protected void onPreExecute() {
			
		}
	}
    
    
    void done(){
    	Intent ii = new Intent(SendDocumentActivity.this, FetchDocumentActivity.class);
		ii.putExtra("ip", ip);
		ii.putExtra("tid", tid);
		startActivity(ii);
    	finish();
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
