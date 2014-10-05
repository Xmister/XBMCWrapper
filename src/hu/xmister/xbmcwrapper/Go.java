package hu.xmister.xbmcwrapper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class Go extends FragmentActivity {

	static final int ITEMS = 10;
	MyAdapter mAdapter;
	ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_go);
		mAdapter = new MyAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		Button button = (Button) findViewById(R.id.smbFragment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(0);
			}
		});
		button = (Button) findViewById(R.id.pvrFragment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(1);
			}
		});

	}

	public static class MyAdapter extends FragmentPagerAdapter {
		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return ITEMS;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 1:
				return SmbFragment.init(1);
			case 2: 
				return PvrFragment.init(2);
			case 3:
				return OtherFragment.init(3);
			default:
				return new DetailFragment();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
		/* menu; this adds items to the action bar if it is present. */
		getMenuInflater().inflate(R.menu.activity_go, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // Handle
		/* item selection */
		switch (item.getItemId()) {
		case R.id.menu_save:
			save();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void save() {
		SmbFragment.init(1).save();
		PvrFragment.init(2).save();
		OtherFragment.init(3).save();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
}

/*
 * public class Go extends android.support.v4.app.FragmentActivity implements
 * OnClickListener {
 * 
 * private TextView Montext; private Button MonBt, MetBt; private
 * 
 * 
 * @SuppressLint("HandlerLeak")
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState); Uri extras = getIntent().getData();
 * String FileSmb="";
 * 
 * if (extras != null) { FileSmb = extras.toString(); } Log.d("Start",
 * "Fichier:"+FileSmb); setContentView(R.layout.activity_go);
 * 
 * StrictMode.ThreadPolicy policy = new
 * StrictMode.ThreadPolicy.Builder().permitAll().build();
 * StrictMode.setThreadPolicy(policy);
 * 
 * 
 * Montext=(TextView) findViewById(R.id.status); MonBt=(Button)
 * findViewById(R.id.button1); MonBt.setOnClickListener(this);
 * 
 * 
 * File PlayerC=new File(Environment.getExternalStorageDirectory().getPath()+
 * "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml"); if
 * (PlayerC.exists()) Montext.setText("playercorefactory.xml already exist");
 * else Montext.setText("playercorefactory.xml not found");
 * 
 * File XbmcC=new File(Environment.getExternalStorageDirectory().getPath()+
 * "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata"); if
 * (!XbmcC.isDirectory()) { Montext.setText("XBMC not found !!");
 * MonBt.setClickable(false); } else MonBt.setClickable(true);
 * 
 * 
 * 
 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
 * menu; this adds items to the action bar if it is present.
 * getMenuInflater().inflate(R.menu.activity_go, menu); return true; }
 * 
 * @Override public boolean onOptionsItemSelected(MenuItem item) { // Handle
 * item selection switch (item.getItemId()) { case R.id.menu_save: save();
 * return true; default: return super.onOptionsItemSelected(item); } }
 * 
 * @Override public void onClick(View v) { AssetManager assetManager =
 * this.getAssets(); InputStream is; try { is =
 * assetManager.open("playercorefactory"); } catch (IOException e1) {
 * Montext.setText("Is error"); return; } if (is == null) {
 * Montext.setText("Is null"); return; }
 * 
 * OutputStream os; try { os = new
 * FileOutputStream(Environment.getExternalStorageDirectory().getPath()+
 * "/Android/data/org.xbmc.xbmc/files/.xbmc/userdata/playercorefactory.xml");
 * byte[] buffer = new byte[4096]; int length; while ((length = is.read(buffer))
 * > 0) { os.write(buffer, 0, length); } os.close(); is.close(); } catch
 * (Exception e) { Montext.setText("Error !!"+e.getMessage() ); return; }
 * 
 * Montext.setText("Install ok :-)"); }
 * 
 * private void save() { SharedPreferences sharedPreferences =
 * getSharedPreferences("default", 0); Editor editor = sharedPreferences.edit();
 * editor.putString("samba",
 * ((EditText)findViewById(R.id.samba)).getText().toString());
 * editor.putString("http",
 * ((EditText)findViewById(R.id.http)).getText().toString());
 * editor.putString("pvr",
 * ((EditText)findViewById(R.id.pvr)).getText().toString());
 * editor.putString("file",
 * ((EditText)findViewById(R.id.file)).getText().toString());
 * editor.putString("rfrom",
 * ((EditText)findViewById(R.id.rfrom)).getText().toString());
 * editor.putString("rto",
 * ((EditText)findViewById(R.id.rto)).getText().toString());
 * editor.putString("rfrom2",
 * ((EditText)findViewById(R.id.rfrom2)).getText().toString());
 * editor.putString("rto2",
 * ((EditText)findViewById(R.id.rto2)).getText().toString());
 * editor.putString("pvrmap",
 * ((EditText)findViewById(R.id.pvrmap)).getText().toString());
 * editor.putString("tvh",
 * ((EditText)findViewById(R.id.tvh)).getText().toString());
 * editor.putBoolean("r1", ((CheckBox)findViewById(R.id.r1)).isChecked());
 * editor.putBoolean("r2", ((CheckBox)findViewById(R.id.r2)).isChecked());
 * editor.putString("mddb",
 * ((EditText)findViewById(R.id.mddb)).getText().toString());
 * editor.putInt("mdcut",
 * Integer.valueOf(((EditText)findViewById(R.id.mdcut)).getText().toString()));
 * editor.putInt("method", (Integer)MetBt.getTag());
 * editor.putBoolean("restream",
 * ((CheckBox)findViewById(R.id.ch_restream)).isChecked());
 * editor.putBoolean("rehttp",
 * ((CheckBox)findViewById(R.id.ch_rehttp)).isChecked());
 * editor.putString("cifs",
 * ((EditText)findViewById(R.id.cifs)).getText().toString()); editor.commit(); }
 * 
 * @Override protected void onStop() { super.onStop(); save(); }
 * 
 * @Override protected void onStart() { super.onStart();
 * setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 * SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
 * ((EditText
 * )findViewById(R.id.samba)).setText(sharedPreferences.getString("samba",
 * ((EditText)findViewById(R.id.samba)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.http)).setText(sharedPreferences.getString("http",
 * ((EditText)findViewById(R.id.http)).getText().toString()));
 * ((EditText)findViewById(R.id.pvr)).setText(sharedPreferences.getString("pvr",
 * ((EditText)findViewById(R.id.pvr)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.file)).setText(sharedPreferences.getString("file",
 * ((EditText)findViewById(R.id.file)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.rfrom)).setText(sharedPreferences.getString("rfrom",
 * ((EditText)findViewById(R.id.rfrom)).getText().toString()));
 * ((EditText)findViewById(R.id.rto)).setText(sharedPreferences.getString("rto",
 * ((EditText)findViewById(R.id.rto)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.rfrom2)).setText(sharedPreferences.getString("rfrom2",
 * ((EditText)findViewById(R.id.rfrom2)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.rto2)).setText(sharedPreferences.getString("rto2",
 * ((EditText)findViewById(R.id.rto2)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.pvrmap)).setText(sharedPreferences.getString("pvrmap",
 * ((EditText)findViewById(R.id.pvrmap)).getText().toString()));
 * ((EditText)findViewById(R.id.tvh)).setText(sharedPreferences.getString("tvh",
 * ((EditText)findViewById(R.id.tvh)).getText().toString()));
 * ((CheckBox)findViewById
 * (R.id.r1)).setChecked(sharedPreferences.getBoolean("r1",
 * ((CheckBox)findViewById(R.id.r1)).isChecked()));
 * ((CheckBox)findViewById(R.id.
 * r2)).setChecked(sharedPreferences.getBoolean("r2",
 * ((CheckBox)findViewById(R.id.r2)).isChecked()));
 * ((EditText)findViewById(R.id.
 * mddb)).setText(sharedPreferences.getString("mddb",
 * ((EditText)findViewById(R.id.mddb)).getText().toString()));
 * ((EditText)findViewById
 * (R.id.mdcut)).setText(String.valueOf(sharedPreferences.getInt("mdcut",
 * Integer
 * .valueOf(((EditText)findViewById(R.id.mdcut)).getText().toString()))));
 * di.onClick(null, sharedPreferences.getInt("method", 3));
 * ((CheckBox)findViewById
 * (R.id.ch_restream)).setChecked(sharedPreferences.getBoolean("restream",
 * ((CheckBox)findViewById(R.id.ch_restream)).isChecked()));
 * ((CheckBox)findViewById
 * (R.id.ch_rehttp)).setChecked(sharedPreferences.getBoolean("rehttp",
 * ((CheckBox)findViewById(R.id.ch_rehttp)).isChecked()));
 * ((EditText)findViewById
 * (R.id.cifs)).setText(sharedPreferences.getString("cifs",
 * ((EditText)findViewById(R.id.cifs)).getText().toString())); } }
 */