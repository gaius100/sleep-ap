/**
 * Copyright (c) 2013, J. Behar, A. Roebuck, M. Shahid, J. Daly, A. Hallack, 
 * N. Palmius, K. Niehaus, G. Clifford (University of Oxford). All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 	1. 	Redistributions of source code must retain the above copyright notice, this 
 * 		list of conditions and the following disclaimer.
 * 	2.	Redistributions in binary form must reproduce the above copyright notice, 
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * NOT MEDICAL SOFTWARE.
 * 
 * This software is provided for informational or research purposes only, and is not
 * for professional medical use, diagnosis, treatment or care, nor is it intended to
 * be a substitute therefor. Always seek the advice of a physician or other qualified
 * health provider properly licensed to practice medicine or general healthcare in
 * your jurisdiction concerning any questions you may have regarding any health
 * problem. Never disregard professional medical advice or delay in seeking it
 * because of something you have observed through the use of this software. Always
 * consult with your physician or other qualified health care provider before
 * embarking on a new treatment, diet or fitness programme.
 * 
 * Graphical charts copyright (c) AndroidPlot (http://androidplot.com/), SVM 
 * component copyright (c) LIBSVM (http://www.csie.ntu.edu.tw/~cjlin/libsvm/) - all 
 * rights reserved.
 * */

package ibme.sleepap.server;

import ibme.sleepap.history.HistoryTable;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class ServerAccess {
	private enum AccessType {
		Send,
		Update
	}
	private class AsyncWebAccess extends AsyncTask<Void, Void, String> {
		
		public AsyncWebAccess(String strDataToSend, AccessType access) {
			dataToSend = strDataToSend;
			accessType = access;
		}
		
		/*private class MySSLSocketFactory extends SSLSocketFactory {
		    SSLContext sslContext = SSLContext.getInstance("TLS");

		    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		        super(truststore);

		        TrustManager tm = new X509TrustManager() {
		            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		            }

		            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		            }

		            public X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		        };

		        sslContext.init(null, new TrustManager[] { tm }, null);
		    }

		    @Override
		    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		    }

		    @Override
		    public Socket createSocket() throws IOException {
		        return sslContext.getSocketFactory().createSocket();
		    }
		}*/
		
		private String dataToSend;
		private AccessType accessType;
		
		private final String URL_STRING = "http://dhcp-531.wolf.ox.ac.uk/SleepAp/process_data.php";
		private final String URL_STRING_UPDATE = "http://dhcp-531.wolf.ox.ac.uk/SleepAp/get_status.php?guid=";
		
        @Override
        protected String doInBackground(Void... params) {
        	switch (accessType) {
        	case Send:
        		sendRequest();
        		break;
        	case Update:
        		updateStatus();
        		break;
        	}
        	return "";
        }
        protected void sendRequest() {
			try {
		        //KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		        //trustStore.load(null, null);

		        //SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
		        //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		        /*HttpParams httpparams = new BasicHttpParams();
		        HttpProtocolParams.setVersion(httpparams, HttpVersion.HTTP_1_1);
		        HttpProtocolParams.setContentCharset(httpparams, HTTP.UTF_8);

		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        //registry.register(new Scheme("https", sf, 443));

		        ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpparams, registry);*/

		        //List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
		        
		        //for (NameValuePair nvp : params)
		        //	nameValuePairs.add(nvp);
                
				showMessage("connecting");
				
                HttpClient httpclient = new DefaultHttpClient();//(ccm, httpparams);
                HttpPost httppost = new HttpPost(URL_STRING);
                
                //StringEntity se = new StringEntity("{\"patientid\":\"1234\",\"datetime\":\"2012-04-21 12:34:56\"}");
                StringEntity se = new StringEntity(dataToSend);
                
                httppost.setEntity(se);

                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");
                
                HttpResponse response = httpclient.execute(httppost);
                String data = new BasicResponseHandler().handleResponse(response);            

                Log.i("ServerAccess:SetUpUserInfo:doInBackground", data);
                
                JSONObject jObject=new JSONObject(data);
                //jObject.getString("status");
                
                guid = jObject.getString("guid");
                
                if (database != null) {
        			ContentValues historyValues = new ContentValues();
        			historyValues.put(HistoryTable.COLUMN_CLOUD_GUID, guid);
                	database.update(HistoryTable.TABLE_NAME, historyValues, HistoryTable.COLUMN_START_DATE + " = " + databaseKey, null);
                }
                
                //ShowMessage("Server updated successfully!");
                showMessage(jObject.getString("status"));
                
                //updateStatus();
                
                currentActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateHandler.postDelayed(updateRunnable, 500);
						
					}
				});
                
                //Output.setText(data);
                
                //return data;
                
                //mWebView.loadDataWithBaseURL(httppost.getURI().toString(), data, "text/html", HTTP.UTF_8, null);

            } catch (Exception ex) {
            	ex.printStackTrace();
            	
            	showMessage("Error sending to server: " + ex.getMessage());
                
            	if (processButton != null) {
                	currentActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							processButton.setEnabled(true);
						}
                	});
            	}
            }
        }
        protected void updateStatus() {
        	//while(true)
        	//{
	        //	try {
			//		Thread.sleep(5000);
			//	} catch (InterruptedException e) {
			//	}
	        	
				try {
			        //KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			        //trustStore.load(null, null);
	
			        //SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			        //sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	
			        /*HttpParams httpparams = new BasicHttpParams();
			        HttpProtocolParams.setVersion(httpparams, HttpVersion.HTTP_1_1);
			        HttpProtocolParams.setContentCharset(httpparams, HTTP.UTF_8);
	
			        SchemeRegistry registry = new SchemeRegistry();
			        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			        //registry.register(new Scheme("https", sf, 443));
	
			        ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpparams, registry);*/
	
			        //List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
			        
			        //for (NameValuePair nvp : params)
			        //	nameValuePairs.add(nvp);
	                
	                HttpClient httpclient = new DefaultHttpClient(); //(ccm, httpparams);
	                HttpGet httpget = new HttpGet(URL_STRING_UPDATE + guid);
	                
	                HttpResponse response = httpclient.execute(httpget);
	                String data = new BasicResponseHandler().handleResponse(response);            
	
	                Log.i("ServerAccess:SetUpUserInfo:doInBackground", data);
	                
	                JSONObject jObject=new JSONObject(data);
	                //jObject.getString("status");
	                
	                //ShowMessage("Server updated successfully!");
	                
	                if (jObject.getString("status").equals("processed")) {
	                	showMessage(jObject.getString("result"));
	                	if (processButton != null) {
		                	currentActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									processButton.setEnabled(true);
								}
		                	});
	                	}
	                    if (database != null) {
	            			ContentValues historyValues = new ContentValues();
	            			historyValues.put(HistoryTable.COLUMN_CLOUD_RESULT, jObject.getString("result"));
	                    	database.update(HistoryTable.TABLE_NAME, historyValues, HistoryTable.COLUMN_START_DATE + " = " + databaseKey, null);
	                    }
	                } else {
	                	showMessage(jObject.getString("status"));
	                	
	                    currentActivity.runOnUiThread(new Runnable() {
	    					@Override
	    					public void run() {
	    						updateHandler.postDelayed(updateRunnable, 500);
	    						
	    					}
	    				});
	                }
	                //Output.setText(data);
	                
	                //mWebView.loadDataWithBaseURL(httppost.getURI().toString(), data, "text/html", HTTP.UTF_8, null);
	                
	                //if (jObject.getString("status").equals("completed"))
	                //	break;
	
	            } catch (Exception ex) {
	            	ex.printStackTrace();
	            	
	            	showMessage("Error reading from server: " + ex.getMessage());
	            	
                	if (processButton != null) {
	                	currentActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								processButton.setEnabled(true);
							}
	                	});
                	}
	            }
        	//}
        }
        @Override
        protected void onPostExecute(String result) {
        	//Log.i("ServerAccess:SetUpUserInfo:onPostExecute", result);
        }
        
        /*public class ShowMessage implements Runnable, OnClickListener{
    		private String message;
    		
    		public ShowMessage(String strMessage) {
    			message = strMessage;
    		}
    		
			@Override
			public void run() {
	        	/*AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(currentActivity);

	        	dlgAlert.setMessage(message);
	        	dlgAlert.setTitle(currentActivity.getResources().getString(R.string.app_name));
	        	dlgAlert.setPositiveButton("OK", this);
	        	dlgAlert.setCancelable(true);
	        	dlgAlert.create().show();* /
	        	
	        	scoreDisplay.setText(message);
			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//dismiss the dialog
			}
        }*/
        
        private void showMessage(String strMessage) {
        	final String message = strMessage;
        	currentActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					String strPrefix = currentActivity.getResources().getString(ibme.sleepap.R.string.cloudDefaultScore);
					scoreDisplay.setText(strPrefix.replace("-", message));
				}
			});
        }
    }

	private Activity currentActivity;
	private TextView scoreDisplay;
	private Button processButton;
	private Handler updateHandler;
	private Runnable updateRunnable;
	private SQLiteDatabase database;
	private String databaseKey;
	private String guid;
	
	public ServerAccess(Activity ctx, TextView tvScoreDisplay, Handler hUpdate, Runnable rUpdate) {
		currentActivity = ctx;
		scoreDisplay = tvScoreDisplay;
		updateHandler = hUpdate;
		updateRunnable = rUpdate;
	}
	
	public void sendData(Map<String, Object> map) {
		//List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
		
		//nameValuePairs.add(new BasicNameValuePair("patientid", "321"));
		//nameValuePairs.add(new BasicNameValuePair("datetime", strLocationName));
		//nameValuePairs.add(new BasicNameValuePair("participants", ((Integer)iNumberTrained).toString()));
		
		JSONObject json=new JSONObject();
		
		for (Map.Entry<String, Object> entry : map.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    
			try {
				if ((value != null) && (value.getClass().isArray())) {
					double[] values = (double[])value;
					
					Log.i("SendData", "Length = " + values.length);
					
					//ArrayList<Double> listvalues = new ArrayList<Double>();
					JSONArray ja = new JSONArray();
					
					for (double d : values)
						ja.put(d);
					
					//Log.i("SendData", "Converted length = " + ja.length());
					
					json.put(key, ja);
			    } else {
			    	json.put(key, value);
			    }
			} catch (JSONException e) {
				Log.e("SendData", e.getMessage());
			}
		}
		try {
			json.put("guid", guid);
		} catch (JSONException e) {
			Log.e("SendData", e.getMessage());
		}
		
		String jsonText = json.toString();
		
		Log.i("SendData", jsonText);
		
		AsyncWebAccess awe = new AsyncWebAccess(jsonText, AccessType.Send);
		
		awe.execute();
	}
	public void UpdateStatus() {
		AsyncWebAccess awe = new AsyncWebAccess(null, AccessType.Update);
		
		awe.execute();
	}
	public String getGUID() {
		return guid;
	}
	public void setGUID(String newGUID) {
		guid = newGUID;
	}
	public SQLiteDatabase getDB() {
		return database;
	}
	public String getDBKey() {
		return databaseKey;
	}
	public void setDB(SQLiteDatabase db, String key) {
		database = db;
		databaseKey = key;
	}
	public void setDBKey(String key) {
		databaseKey = key;
	}
	public void setCloudProcessButton(Button button) {
		processButton = button;
	}
}
