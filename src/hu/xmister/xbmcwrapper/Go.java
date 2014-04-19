package hu.xmister.xbmcwrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Go extends Activity implements OnClickListener {
	
	private TextView Montext;
	private Button MonBt;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Uri extras = getIntent().getData();
		String FileSmb="";
		
		if (extras != null) {
			FileSmb = extras.toString();
		} 
		Log.d("Start", "Fichier:"+FileSmb);
		setContentView(R.layout.activity_go);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		

		Montext=(TextView) findViewById(R.id.status);
		MonBt=(Button) findViewById(R.id.button1);
		MonBt.setOnClickListener(this);
		
		File PlayerC=new File("/mnt/sdcard/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
		if (PlayerC.exists())
			Montext.setText("playercorefactory.xml already exist");	
		else
			Montext.setText("playercorefactory.xml not found");
		
		File XbmcC=new File("/mnt/sdcard/Android/data/org.xbmc.xbmc/files/.xbmc/userdata");
		if (!XbmcC.isDirectory()) {
			Montext.setText("XBMC not found !!");
			MonBt.setClickable(false);
		}
		else
			MonBt.setClickable(true);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_go, menu);
		return true;
	}
	
	@Override
	public void onClick(View v) {
		AssetManager assetManager = this.getAssets();
		InputStream is;
		try {
			is = assetManager.open("playercorefactory");
		} catch (IOException e1) {
			Montext.setText("Is error");
			return;
		}
		if (is == null) {
			Montext.setText("Is null");
			return;
		}
			
		OutputStream os;
		try {
			os = new FileOutputStream("/mnt/sdcard/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
		byte[] buffer = new byte[4096];
		int length;
		while ((length = is.read(buffer)) > 0) {
		    os.write(buffer, 0, length);
		}
		os.close();
		is.close();
		} catch (Exception e) {
			Montext.setText("Error !!"+e.getMessage() );
			return;
		}
		
		Montext.setText("Install ok :-)");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		Editor editor = sharedPreferences.edit();
		editor.putString("samba", ((EditText)findViewById(R.id.samba)).getText().toString());
		editor.putString("http", ((EditText)findViewById(R.id.http)).getText().toString());
		editor.putString("pvr", ((EditText)findViewById(R.id.pvr)).getText().toString());
		editor.putString("file", ((EditText)findViewById(R.id.file)).getText().toString());
		editor.putString("rfrom", ((EditText)findViewById(R.id.rfrom)).getText().toString());
		editor.putString("rto", ((EditText)findViewById(R.id.rto)).getText().toString());
		editor.putString("rfrom2", ((EditText)findViewById(R.id.rfrom2)).getText().toString());
		editor.putString("rto2", ((EditText)findViewById(R.id.rto2)).getText().toString());
		editor.putString("pvrmap", ((EditText)findViewById(R.id.pvrmap)).getText().toString());
		editor.putString("tvh", ((EditText)findViewById(R.id.tvh)).getText().toString());
		editor.putBoolean("r1", ((CheckBox)findViewById(R.id.r1)).isChecked());
		editor.putBoolean("r2", ((CheckBox)findViewById(R.id.r2)).isChecked());
		editor.commit();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		((EditText)findViewById(R.id.samba)).setText(sharedPreferences.getString("samba", ((EditText)findViewById(R.id.samba)).getText().toString()));
		((EditText)findViewById(R.id.http)).setText(sharedPreferences.getString("http", ((EditText)findViewById(R.id.http)).getText().toString()));
		((EditText)findViewById(R.id.pvr)).setText(sharedPreferences.getString("pvr", ((EditText)findViewById(R.id.pvr)).getText().toString()));
		((EditText)findViewById(R.id.file)).setText(sharedPreferences.getString("file", ((EditText)findViewById(R.id.file)).getText().toString()));
		((EditText)findViewById(R.id.rfrom)).setText(sharedPreferences.getString("rfrom", ((EditText)findViewById(R.id.rfrom)).getText().toString()));
		((EditText)findViewById(R.id.rto)).setText(sharedPreferences.getString("rto", ((EditText)findViewById(R.id.rto)).getText().toString()));
		((EditText)findViewById(R.id.rfrom2)).setText(sharedPreferences.getString("rfrom2", ((EditText)findViewById(R.id.rfrom2)).getText().toString()));
		((EditText)findViewById(R.id.rto2)).setText(sharedPreferences.getString("rto2", ((EditText)findViewById(R.id.rto2)).getText().toString()));
		((EditText)findViewById(R.id.pvrmap)).setText(sharedPreferences.getString("pvrmap", ((EditText)findViewById(R.id.pvrmap)).getText().toString()));
		((EditText)findViewById(R.id.tvh)).setText(sharedPreferences.getString("tvh", ((EditText)findViewById(R.id.tvh)).getText().toString()));
		((CheckBox)findViewById(R.id.r1)).setChecked(sharedPreferences.getBoolean("r1", false));
		((CheckBox)findViewById(R.id.r2)).setChecked(sharedPreferences.getBoolean("r2", false));
	}
}
