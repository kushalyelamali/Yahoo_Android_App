// Author : Kushal Yelamali

package com.kushal.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.FacebookDialog;

public class Weather extends Activity {
	
	public final String tag="com.kushal.DEBUG";
	EditText SearchEditText1;
	TextView test;
	RadioButton fahrenheit;
	RadioButton Celsius;
	String url="http://cs-server.usc.edu:16177/SerchServ/Servlet?location=90089&type=zip&tempUnit=f";
	String jsonText;
	Button Search;
	private static final Pattern zipRegex= Pattern.compile("^[0-9]{5}$");
	private static final Pattern cityRegex= Pattern.compile("^[a-zA-Z.'\\s]+\\s*\\,\\s*[a-zA-Z.'\\s]+\\s*\\,*\\s*[a-zA-Z.'\\s]+$");
	private static final Pattern iNumber=Pattern.compile("^\\d+$");
	String type=null;
	String tempUnit=null;
	int alertDisplayed;
	
	private UiLifecycleHelper uiHelper;
    private static final String TAG_LOG = "abc";
    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d(TAG_LOG, String.format("Error: %s", error.toString()));
            displayAlertDialog("message", "Error posting to Facebook");
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d(TAG_LOG, "Success!");
            displayAlertDialog("message", "Posted Succesfully to Facebook");
            
        }
    };

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG_LOG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG_LOG, "Logged out...");
        }
    }
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 uiHelper = new UiLifecycleHelper(this, callback);
	        uiHelper.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_weather);
		SetDegreeSign();
		Search = (Button) findViewById(R.id.SearchButton);
		Search.setOnClickListener(onSearch);
		showHide(8); //0 visible 8 gone
		
		
		TextView tv3 = (TextView) findViewById(R.id.textView3);

		tv3.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	PostDialog("Post Current Weather");
		    }
		});
		
		TextView tv4 = (TextView) findViewById(R.id.textView4);

		tv4.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	PostForecastDialog("Post Weather Forecast");
		    }
		});
		
	}
	
	
	public void postToFbForecast()
	{
		String jsonData = jsonText;
		JSONArray forecast = null;
		String textCond = null,tempCond = null,city = null,region = null,country = null,link = null,image = null,feed = null,temperatureUnit= null;
		JSONObject obj;
		try {
			obj = new JSONObject(jsonData);
			
			 forecast = obj.getJSONObject("weather").getJSONArray("forecast");
	    	
	    	 textCond=obj.getJSONObject("weather").getJSONObject("condition").getString("text");
	    	 tempCond=obj.getJSONObject("weather").getJSONObject("condition").getString("temp");
	    	 city=obj.getJSONObject("weather").getJSONObject("location").getString("city");
	    	 region=obj.getJSONObject("weather").getJSONObject("location").getString("region");
	    	 country=obj.getJSONObject("weather").getJSONObject("location").getString("country");
	    	
	    	 link = obj.getJSONObject("weather").getString("link");
	    	 image = obj.getJSONObject("weather").getString("img");
	    	 feed = obj.getJSONObject("weather").getString("feed");
	    	
	    	 temperatureUnit=obj.getJSONObject("weather").getJSONObject("units").getString("temperature");
	    	
	    	int length= forecast.length();
	    	for(int i=0;i<length;i++)
	    	{
	    		String text=forecast.getJSONObject(i).getString("text");
	    		String high=forecast.getJSONObject(i).getString("high");
	    		String day=forecast.getJSONObject(i).getString("day");
	    		String low=forecast.getJSONObject(i).getString("low");
	    	}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
		String feed1= feed;
        String city1=city+","+region+","+country;
        String picture1="http://www-scf.usc.edu/~csci571/2013Fall/hw8/weather.jpg";
       // String caption1="The current condition for "+city+" is "+textCond+".";
        //String description1="Temperature is "+tempCond+(char) 0x00B0+temperatureUnit;
        String caption1="Weather forecast for "+city+":";
        String description1=null;
		try {
			description1 = forecast.getJSONObject(0).getString("day")+":"+forecast.getJSONObject(0).getString("text")+","+forecast.getJSONObject(0).getString("high")+(char) 0x00B0+temperatureUnit+"/"+forecast.getJSONObject(0).getString("low")+(char) 0x00B0+temperatureUnit+";";
			description1+=forecast.getJSONObject(1).getString("day")+":"+forecast.getJSONObject(1).getString("text")+","+forecast.getJSONObject(1).getString("high")+(char) 0x00B0+temperatureUnit+"/"+forecast.getJSONObject(1).getString("low")+(char) 0x00B0+temperatureUnit+";";
	         description1+=forecast.getJSONObject(2).getString("day")+":"+forecast.getJSONObject(2).getString("text")+","+forecast.getJSONObject(2).getString("high")+(char) 0x00B0+temperatureUnit+"/"+forecast.getJSONObject(2).getString("low")+(char) 0x00B0+temperatureUnit+";";
	         description1+=forecast.getJSONObject(3).getString("day")+":"+forecast.getJSONObject(3).getString("text")+","+forecast.getJSONObject(3).getString("high")+(char) 0x00B0+temperatureUnit+"/"+forecast.getJSONObject(3).getString("low")+(char) 0x00B0+temperatureUnit+";";
	         description1+=forecast.getJSONObject(4).getString("day")+":"+forecast.getJSONObject(4).getString("text")+","+forecast.getJSONObject(4).getString("high")+(char) 0x00B0+temperatureUnit+"/"+forecast.getJSONObject(4).getString("low")+(char) 0x00B0+temperatureUnit+";";
	         
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         

//        JSONObject outerObj = new JSONObject();
//        JSONObject innerObj= new JSONObject();
//        try {
//			innerObj.put("text","here");
//			   innerObj.put("href",feed1);
//		        outerObj.put("Look at details", innerObj);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
     
		
		
		 if (FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
             FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                     .setName(city1)
                     .setCaption(caption1)
                     .setLink(feed1)
                     .setDescription(description1)
                     .setPicture(picture1)
                     .build();
             uiHelper.trackPendingDialogCall(shareDialog.present());
           
         }
         else {
             Log.d(TAG_LOG, "Success!");
             displayAlertDialog("message","Posted Successfully to facebook");
         }
	}
    	

	public void postToFb()
	{
		String jsonData = jsonText;
		String textCond = null,tempCond = null,city = null,region = null,country = null,link = null,image = null,feed = null,temperatureUnit= null;
		JSONObject obj;
		try {
			obj = new JSONObject(jsonData);
			
			JSONArray forecast = obj.getJSONObject("weather").getJSONArray("forecast");
	    	
	    	 textCond=obj.getJSONObject("weather").getJSONObject("condition").getString("text");
	    	 tempCond=obj.getJSONObject("weather").getJSONObject("condition").getString("temp");
	    	 city=obj.getJSONObject("weather").getJSONObject("location").getString("city");
	    	 region=obj.getJSONObject("weather").getJSONObject("location").getString("region");
	    	 country=obj.getJSONObject("weather").getJSONObject("location").getString("country");
	    	
	    	 link = obj.getJSONObject("weather").getString("link");
	    	 image = obj.getJSONObject("weather").getString("img");
	    	 feed = obj.getJSONObject("weather").getString("feed");
	    	
	    	 temperatureUnit=obj.getJSONObject("weather").getJSONObject("units").getString("temperature");
	    	
	    	int length= forecast.length();
	    	for(int i=0;i<length;i++)
	    	{
	    		String text=forecast.getJSONObject(i).getString("text");
	    		String high=forecast.getJSONObject(i).getString("high");
	    		String day=forecast.getJSONObject(i).getString("day");
	    		String low=forecast.getJSONObject(i).getString("low");
	    	}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
		
        String city1=city+","+region+","+country;
        String picture1=image;
        String caption1="The current condition for "+city+" is "+textCond+".";
        String description1="Temperature is "+tempCond+(char) 0x00B0+temperatureUnit;
        String feed1= feed;


        JSONObject outerObj = new JSONObject();
        JSONObject innerObj= new JSONObject();
        try {
			innerObj.put("text","here");
			   innerObj.put("href",feed1);
		        outerObj.put("Look at details", innerObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
		
		
		 if (FacebookDialog.canPresentShareDialog(this, FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
             FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                     .setName(city1)
                     .setCaption(caption1)
                     .setLink(feed1)
                     .setDescription(description1)
                     .setPicture(picture1)
                     .build();
             uiHelper.trackPendingDialogCall(shareDialog.present());
           
         }
         else {
             Log.d(TAG_LOG, "Success!");
         }
	}
    	
	
	 @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	    }

	    @Override
	    public void onResume() {
	        super.onResume();
	        uiHelper.onResume();
	    }

	    @Override
	    public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        uiHelper.onSaveInstanceState(outState);
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        uiHelper.onPause();
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        uiHelper.onDestroy();
	    }
	    
	
	    
	    
	    public void PostDialog( String btnText)
		{
			 LayoutInflater inflator = LayoutInflater.from(this);
			    final View DialogFBView = inflator.inflate(
			            R.layout.post_dialog_xml, null);
			    final AlertDialog DialogFB = new AlertDialog.Builder(this).create();
			    DialogFB.setView(DialogFBView);
			    Button btnPost = (Button)DialogFBView.findViewById(R.id.post);
			    btnPost.setText(btnText);
			    
			    btnPost.setOnClickListener(new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			        
			        	/*Session curSession = Session.openActiveSession(MainActivity.this, true, null);*/
			        	//publishFeedDialog();
			        	postToFb();
			        	DialogFB.dismiss();
			        }
			    });
			    DialogFBView.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			        	DialogFB.dismiss();
			        }
			    });
			    DialogFB.show();
		
		}
	    
	    public void PostForecastDialog( String btnText)
		{
			 LayoutInflater inflator = LayoutInflater.from(this);
			    final View DialogFBView = inflator.inflate(
			            R.layout.post_dialog_xml, null);
			    final AlertDialog DialogFB = new AlertDialog.Builder(this).create();
			    DialogFB.setView(DialogFBView);
			    Button btnPost = (Button)DialogFBView.findViewById(R.id.post);
			    btnPost.setText(btnText);
			    
			    btnPost.setOnClickListener(new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			        
			        	/*Session curSession = Session.openActiveSession(MainActivity.this, true, null);*/
			        	//publishFeedDialog();
			        	postToFbForecast();
			        	DialogFB.dismiss();
			        }
			    });
			    DialogFBView.findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			        @Override
			        public void onClick(View v) {
			        	DialogFB.dismiss();
			        }
			    });
			    DialogFB.show();
		
		}
	    
	public void showHide(int visibility)
	{
		TableLayout tableLayout1 = (TableLayout)findViewById(R.id.tableLayout1);
		tableLayout1.setVisibility(visibility);
		LinearLayout linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
		linearLayout1.setVisibility(visibility);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	private boolean isCityValid(String s) {
        return cityRegex.matcher(s).matches();
    }
	
	private boolean isZipValid(String s) {
        return zipRegex.matcher(s).matches();
    }
	
	private boolean isNumber(String s) {
        return iNumber.matcher(s).matches();
    }
	
	 private OnClickListener onSearch = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
			
			
			String invalidLocation="Invalid Location";
			SearchEditText1 = (EditText) findViewById(R.id.SearchEditText1); 
			String location=SearchEditText1.getText().toString();
			fahrenheit = (RadioButton) findViewById(R.id.fahrenheit);
			Celsius = (RadioButton) findViewById(R.id.Celsius);
			if(fahrenheit.isChecked()) tempUnit="f";
			else tempUnit="c";
			alertDisplayed=0;
			if(location == null || location.equals(""))
			{
				displayAlertDialog(invalidLocation,"Please enter a valid location or zip code");
				showHide(8);
			}
			 if(!isZipValid(location) && !isCityValid(location))
			 {
			    	if(!isCityValid(location)&& alertDisplayed!=1 && !isNumber(location))
			    	{
			    		displayAlertDialog(invalidLocation,"Please enter a valid location, must include state and country seperated by comma");
			    		showHide(8);
			    		
			    	}
			    	
			    	
			    	if(!isZipValid(location)&& alertDisplayed!=1)
			    	{
			    		displayAlertDialog(invalidLocation,"Please enter a valid zip code, must be 5 digits");
			    		showHide(8);
			    		
			    	}
			    	
			}
			 
			if(isCityValid(location))
			{
				type="city";
				try {
					location=URLEncoder.encode(location, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 url="http://cs-server.usc.edu:16177/SerchServ/Servlet?location="+location+"&type="+type+"&tempUnit="+tempUnit;
				
					try {
						jsonText=getStringContent(url);
						Log.d(tag, jsonText);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//test.setText(jsonText);
					
					
					
					parseJsonData(jsonText);
			}
			
				    
			if(isZipValid(location))
			{
				type="zip";
				url="http://cs-server.usc.edu:16177/SerchServ/Servlet?location="+location+"&type="+type+"&tempUnit="+tempUnit;
				
				try {
					jsonText=getStringContent(url);
					Log.d(tag, jsonText);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//test.setText(jsonText);
				parseJsonData(jsonText);
				//facebookLogin();
			}
			
			
			
		}
		
		
	};
	
	
	
	
	private void displayAlertDialog(String title,String mymessage)
	{
		alertDisplayed=1;
		
		Toast.makeText(getApplicationContext(), mymessage,
				   Toast.LENGTH_LONG).show();

	}
	
	private void SetDegreeSign()
	{
		fahrenheit = (RadioButton) findViewById(R.id.fahrenheit);
		Celsius = (RadioButton) findViewById(R.id.Celsius);
		Celsius.setText((char) 0x00B0+"C");
		fahrenheit.setText((char) 0x00B0+"F");
	}
	
	private void parseJsonData(String jsonData)
	{
		JSONObject obj=null;
	    try {
	    	if(jsonData!=null && !jsonData.equals("")){
	    	 obj = new JSONObject(jsonData);
	    	
	    	
	    	if(!obj.has("weather")) {
	    		displayAlertDialog("Error","No weather information found !!!");
	    		showHide(8);
	    	}
	    	else{
	    	showHide(0);
	    	JSONArray forecast = obj.getJSONObject("weather").getJSONArray("forecast");
	    	
	    	String textCond=obj.getJSONObject("weather").getJSONObject("condition").getString("text");
	    	String tempCond=obj.getJSONObject("weather").getJSONObject("condition").getString("temp");
	    	String city=obj.getJSONObject("weather").getJSONObject("location").getString("city");
	    	String region=obj.getJSONObject("weather").getJSONObject("location").getString("region");
	    	String country=obj.getJSONObject("weather").getJSONObject("location").getString("country");
	    	
	    	String link = obj.getJSONObject("weather").getString("link");
	    	String image = obj.getJSONObject("weather").getString("img");
	    	String feed = obj.getJSONObject("weather").getString("feed");
	    	
	    	String temperatureUnit=obj.getJSONObject("weather").getJSONObject("units").getString("temperature");
	    	
	    	int length= forecast.length();
	    	for(int i=0;i<length;i++)
	    	{
	    		String text=forecast.getJSONObject(i).getString("text");
	    		String high=forecast.getJSONObject(i).getString("high");
	    		String day=forecast.getJSONObject(i).getString("day");
	    		String low=forecast.getJSONObject(i).getString("low");
	    	}
			
			TextView cityView = (TextView) findViewById(R.id.cityView);
			cityView.setText(city);
			TextView stateView = (TextView) findViewById(R.id.stateTextView);
			String stateCountry=region+","+country;
			stateView.setText(stateCountry);
			TextView condView = (TextView) findViewById(R.id.CondTextView);
			TextView tempView = (TextView) findViewById(R.id.tempTextView);
			condView.setText(textCond);
			String temperature=tempCond+(char) 0x00B0+temperatureUnit;
			tempView.setText(temperature);
			ImageView imgView =(ImageView) findViewById(R.id.imageView1);
			Bitmap bimage=  getBitmapFromURL(image);
			imgView.setImageBitmap(bimage);
			TextView th0 = (TextView) findViewById(R.id.th0);
			TextView th1 = (TextView) findViewById(R.id.th1);
			TextView th2 = (TextView) findViewById(R.id.th2);
			TextView th3 = (TextView) findViewById(R.id.th3);
			
			TextView td10 = (TextView) findViewById(R.id.td10);
			TextView td11 = (TextView) findViewById(R.id.td11);
			TextView td12 = (TextView) findViewById(R.id.td12);
			TextView td13 = (TextView) findViewById(R.id.td13);
			
			TextView td20 = (TextView) findViewById(R.id.td20);
			TextView td21 = (TextView) findViewById(R.id.td21);
			TextView td22 = (TextView) findViewById(R.id.td22);
			TextView td23 = (TextView) findViewById(R.id.td23);
			
			TextView td30 = (TextView) findViewById(R.id.td30);
			TextView td31 = (TextView) findViewById(R.id.td31);
			TextView td32 = (TextView) findViewById(R.id.td32);
			TextView td33 = (TextView) findViewById(R.id.td33);
			
			TextView td40 = (TextView) findViewById(R.id.td40);
			TextView td41 = (TextView) findViewById(R.id.td41);
			TextView td42 = (TextView) findViewById(R.id.td42);
			TextView td43 = (TextView) findViewById(R.id.td43);
			
			TextView td50 = (TextView) findViewById(R.id.td50);
			TextView td51 = (TextView) findViewById(R.id.td51);
			TextView td52 = (TextView) findViewById(R.id.td52);
			TextView td53 = (TextView) findViewById(R.id.td53);
			
			th0.setText("Day");
			th1.setText("Weather");
			th2.setText("High");
			th3.setText("Low");
			
			td10.setText(forecast.getJSONObject(0).getString("day"));
			td11.setText(forecast.getJSONObject(0).getString("text"));
			td12.setText(forecast.getJSONObject(0).getString("high")+(char) 0x00B0+temperatureUnit);
			td13.setText(forecast.getJSONObject(0).getString("low")+(char) 0x00B0+temperatureUnit);
			
			td20.setText(forecast.getJSONObject(1).getString("day"));
			td21.setText(forecast.getJSONObject(1).getString("text"));
			td22.setText(forecast.getJSONObject(1).getString("high")+(char) 0x00B0+temperatureUnit);
			td23.setText(forecast.getJSONObject(1).getString("low")+(char) 0x00B0+temperatureUnit);
			
			td30.setText(forecast.getJSONObject(2).getString("day"));
			td31.setText(forecast.getJSONObject(2).getString("text"));
			td32.setText(forecast.getJSONObject(2).getString("high")+(char) 0x00B0+temperatureUnit);
			td33.setText(forecast.getJSONObject(2).getString("low")+(char) 0x00B0+temperatureUnit);
			
			td40.setText(forecast.getJSONObject(3).getString("day"));
			td41.setText(forecast.getJSONObject(3).getString("text"));
			td42.setText(forecast.getJSONObject(3).getString("high")+(char) 0x00B0+temperatureUnit);
			td43.setText(forecast.getJSONObject(3).getString("low")+(char) 0x00B0+temperatureUnit);
			
			td50.setText(forecast.getJSONObject(4).getString("day"));
			td51.setText(forecast.getJSONObject(4).getString("text"));
			td52.setText(forecast.getJSONObject(4).getString("high")+(char) 0x00B0+temperatureUnit);
			td53.setText(forecast.getJSONObject(4).getString("low")+(char) 0x00B0+temperatureUnit);
	    	}
	    	}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	 public Bitmap getBitmapFromURL(String src) {
	        try {
	            Log.e("src",src);
	            URL url = new URL(src);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setDoInput(true);
	            connection.connect();
	            InputStream input = connection.getInputStream();
	            Bitmap myBitmap = BitmapFactory.decodeStream(input);
	            Log.e("Bitmap","returned");
	            return myBitmap;
	        } catch (IOException e) {
	            e.printStackTrace();
	            Log.e("Exception",e.getMessage());
	            return null;
	        }
	    }
	
	public  String getStringContent(String uri) throws Exception {

	    try {
	        HttpClient client = new DefaultHttpClient();
	        HttpGet request = new HttpGet();
	        request.setURI(new URI(uri));
	        HttpResponse response = client.execute(request);
	        InputStream ips  = response.getEntity().getContent();
	        BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

	        StringBuilder sb = new StringBuilder();
	        String s;
	        while(true )
	        {
	            s = buf.readLine();
	            if(s==null || s.length()==0)
	                break;
	            sb.append(s);

	        }
	        buf.close();
	        ips.close();
	        return sb.toString();

	        } 
	    finally {
	              
	            }
	        } 
}
