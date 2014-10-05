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