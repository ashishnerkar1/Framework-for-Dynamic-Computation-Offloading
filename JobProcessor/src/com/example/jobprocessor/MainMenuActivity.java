package com.example.jobprocessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Vector;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import MyPack.Base64;
import MyPack.VolunteerInfo;
import MyPack.VolunteerTask;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class MainMenuActivity extends Activity {
	
	private static final String NAMESPACE = "http://MyPack/";
//	private String URL = "http://0:8095/DMCCCloud/DMCCService?wsdl";//"http://localhost:8080/WebApplication1/NewWebService?wsdl";	

	private String URL = "http://env-6365459.j.layershift.co.uk/DMCCCloud/DMCCService?wsdl";
	
	private static final String SOAP_ACTION = "DMCCService";
	boolean cancel = false, running = false;
	TextView tvCost, tvStatus;
	Button start, stop;
	ImageView ivIn, ivOut;
	SeekBar sb;
	int cost=1;
	String ip, name;
	Handler h;
	Bitmap bi = null, bi1 = null;
	boolean imgOnce1 = false, imgOnce2 = false;
	String addText = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        sb = (SeekBar)findViewById(R.id.seekBar1);
        tvCost = (TextView)findViewById(R.id.tvCost);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        start = (Button)findViewById(R.id.btnStart);
        stop = (Button)findViewById(R.id.btnStop);
        ivIn = (ImageView)findViewById(R.id.ivIn);
        ivOut = (ImageView)findViewById(R.id.ivOut);
        sb.setMax(2);
        Bundle ext = getIntent().getExtras();
        if(ext != null){
        	ip = ext.getString("ip");
        	name = ext.getString("name");
        }
  //      URL = "http://" + ip + ":8095/DMCCCloud/DMCCService?wsdl";
        tvCost.setText("Cost : " + (sb.getProgress() + 1));
        
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				cost = sb.getProgress() + 1;
				tvCost.setText("Cost : " + cost);
			}
		});
        
        start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				running = true;
				LongOperation lo = new LongOperation();
				lo.execute("");
			}
		});
        
        stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				running = false;
			}
		});
        
        h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!addText.equals("")){
					Calendar c = Calendar.getInstance();
			        String dt = c.get(Calendar.DAY_OF_MONTH) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.YEAR);
			        dt += " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
			        addText = "[" + dt + "]  " + addText;
			        tvStatus.setText(addText + "\r\n" + tvStatus.getText());
			        addText = "";
				}
				if(bi!=null){
					if(!imgOnce1){
						ivIn.setImageBitmap(bi);
						imgOnce1 = true;
					}
				}
				if(bi1!=null){
					if(!imgOnce2){
						ivOut.setImageBitmap(bi1);
						imgOnce2 = true;
					}
				}
				if(!cancel) h.postAtTime(this, SystemClock.uptimeMillis() + 50);
			}
		});
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			while(running){
				VolunteerInfo vi = new VolunteerInfo();
		        vi.cost = cost;
		        vi.volIP = ip;
		        addText = "Checking for available job.";
		        
		        SoapObject request = new SoapObject(NAMESPACE, "checkForJob");
		    	
		    	SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
	
				PropertyInfo pi = new PropertyInfo();
				pi.setName("ServerInput");
		        pi.setValue(objectToString(vi));
		        pi.setType(String.class);
		        
		        request.addProperty(pi);
		        
		        envelope.setOutputSoapObject(request);
				
		        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
				String resp = "";
				
				try {
					androidHttpTransport.call(SOAP_ACTION, envelope);
	
					SoapObject resultsRequestSOAP = (SoapObject)envelope.bodyIn;
					
					resp = resultsRequestSOAP.getPrimitivePropertyAsString("return");
				} catch (Exception e) {
					
					Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() of LongOp of JobProcessor ", e);
					System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);

					e.printStackTrace();
					break;
				}
				
				Vector<VolunteerTask> vtVec = (Vector<VolunteerTask>)stringToObject(resp);
				
				try{
					Thread.sleep(1000);
				}catch (Exception e) {}

				
				if(vtVec.size() == 0){
		            addText = "No Job Found.";
		        }else{
		            addText = "Job Found.   Processing " + vtVec.size() + " elements";
		            for(VolunteerTask vt : vtVec){
		                int hei = (vt.data.length / 4) / vt.ww;
		                System.out.println(vt.ww + "  " + hei);
		                bi = Bitmap.createBitmap(vt.ww, hei, Config.ARGB_8888);
		                bi.setPixels(getIntArr(vt.data), 0, vt.ww, 0, 0, vt.ww, hei);
		                imgOnce1 = false;
		                //bi.setRGB(0, 0, vt.ww, hei, getIntArr(vt.data), 0, vt.ww);
		                //jLabelInput.setIcon(new ImageIcon(bi));
		                bi1 = processbi(bi);
		                //jLabelOutput.setIcon(new ImageIcon(bi1));
		                int[] arr = new int[vt.ww * hei];
		                bi1.getPixels(arr, 0, vt.ww, 0, 0, vt.ww, hei);
		                imgOnce2 = false;
		                vt.data = getBytes(arr);
		                vt.volIP = ip;
		            }
		            addText = "Process complete.   Submitting. . .";
		            
		            
		            request = new SoapObject(NAMESPACE, "submitJob");
			    	
			    	envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); 
		
					pi = new PropertyInfo();
					pi.setName("ServerInput");
			        pi.setValue(objectToString(vtVec));
			        pi.setType(String.class);
			        
			        request.addProperty(pi);
			        
			        envelope.setOutputSoapObject(request);
					
			        androidHttpTransport = new HttpTransportSE(URL);
					resp = "";
					
					try {
						androidHttpTransport.call(SOAP_ACTION, envelope);
		
						SoapObject resultsRequestSOAP = (SoapObject)envelope.bodyIn;
						
						resp = resultsRequestSOAP.getPrimitivePropertyAsString("return");
					} catch (Exception e) {
						
						Log.e("YOUR ERROR TAG HERE", "Exception in doinBackground() of LongOp of JobProcessor ", e);
						System.out.println("Exception in doinBackground() of Senddocactiv aftr residualSize cond :" + e);
		
						e.printStackTrace();
						break;
					}

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
    
    Bitmap processbi(Bitmap bi){
    	int col,rr,gg,bb;
        Bitmap biOut = Bitmap.createBitmap(bi.getWidth(), bi.getHeight(), Config.ARGB_8888);
        for(int y = 0;y<bi.getHeight();y++){
            for(int x = 0;x<bi.getWidth();x++){
            	col = bi.getPixel(x, y);
                rr = col & 0xff;
                gg = (col >> 8) & 0xff;
                bb = (col >> 16) & 0xff;
                rr = (rr + gg + bb)/3;
                col = rr | (rr << 8) | (rr << 16);
                col = col | 0xff000000;
                biOut.setPixel(x, y, col);
            }
        }
        return biOut;
    }
    
    byte[] getBytes(int[] arr){
        byte[] ret = new byte[arr.length * 4];
        for(int i=0;i<arr.length;i++){
            ret[(i*4) + 0] = (byte)((arr[i] >> 24) & 0xff);
            ret[(i*4) + 1] = (byte)((arr[i] >> 16) & 0xff);
            ret[(i*4) + 2] = (byte)((arr[i] >> 8) & 0xff);
            ret[(i*4) + 3] = (byte)(arr[i] & 0xff);
        }
        return ret;
    }
    
    int[] getIntArr(byte[] arr){
        int[] ret = new int[arr.length / 4];
        int temp = 0;
        for(int i=0;i<ret.length;i++){
            ret[i] = 0;
            temp = (arr[(i*4) + 0]);
            ret[i] |= (temp << 24);
            
            temp = (arr[(i*4) + 1]);
            ret[i] |= (temp << 16);
            
            temp = (arr[(i*4) + 2]);
            ret[i] |= (temp << 8);
            
            temp = (arr[(i*4) + 3]);
            ret[i] |= temp;
        }
        return ret;
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
