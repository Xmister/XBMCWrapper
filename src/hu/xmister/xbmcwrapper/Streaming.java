package hu.xmister.xbmcwrapper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class Streaming extends Activity implements OnClickListener {

	private Button MonBt;
	private StreamOverFile sof;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Uri extras = getIntent().getData();
		String FileSmb="";

		if (extras != null) {
			FileSmb = extras.toString();
		} 
		Log.d("Streaming", "File:"+FileSmb);
		setContentView(R.layout.streaming);


		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		MonBt=(Button) findViewById(R.id.button1);
		MonBt.setOnClickListener(this);
		MonBt.setClickable(true);

		sof = new StreamOverFile(FileSmb);
		sof.start();

		String url="file://"+sof.getM3U(); 
		try {
			while ( !sof.getGood2Go() ) Thread.sleep(2000);
			//String url="http://localhost:8090/feed1.ffm";
			Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
			LaunchIntent.setPackage("com.mxtech.videoplayer.ad");
			Log.d("smbwrapper","Launch Player: "+url);
			LaunchIntent.setDataAndType(Uri.parse(url), "video/*");
			startActivityForResult(LaunchIntent,1);
		} catch (Exception e) {
			Log.e("PlayerView", "Yeah, I know.");
		}


	}

	@Override
	public void onClick(View v) {
		sof.stopIt();
		try {
			while ( sof.isAlive() ) Thread.sleep(2000);
		} catch (Exception e) {

		}
		finish();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		sof.stopIt();
		try {
			while ( sof.isAlive() ) Thread.sleep(2000);
		} catch (Exception e) {

		}
		finish();
	}
}