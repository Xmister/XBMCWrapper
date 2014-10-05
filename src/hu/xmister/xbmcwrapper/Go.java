package hu.xmister.xbmcwrapper;

import android.os.Bundle;
import android.os.StrictMode;
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

	static final int ITEMS = 4;
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
				mPager.setCurrentItem(1);
			}
		});
		button = (Button) findViewById(R.id.pvrFragment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(2);
			}
		});
		button = (Button) findViewById(R.id.otherFragment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(3);
			}
		});
		button = (Button) findViewById(R.id.mainFragment);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(0);
			}
		});
		StrictMode.ThreadPolicy policy = new
			StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
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
			case 0:
				return MainFragment.init(0);
			case 1:
				return SmbFragment.init(1);
			case 2: 
				return PvrFragment.init(2);
			case 3:
				return OtherFragment.init(3);
			default:
				return MainFragment.init(0);
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
		MainFragment.init(0).save();
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
 * , MetBt; private
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
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
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