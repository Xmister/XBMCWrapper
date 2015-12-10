package hu.xmister.xbmcwrapper;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.google.android.vending.licensing.AESObfuscator;
import hu.xmister.xbmcwrapper.License;

/**
 * The MainGUI activity contains all the fragments for configuration.
 */
public class MainGUI extends FragmentActivity {

	static final int ITEMS = 4;
	MyAdapter mAdapter;
	ViewPager mPager;
	private Handler mHandler;
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private static final String BASE64_PUBLIC_KEY = License.getKey();
    private static final byte[] SALT = License.getSalt();
    public int retryCount=0;

	/**
	 * The MyLicenseCheckerCallback class is the one that checks the License response of Google Play.
	 */
	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		/**
		 * Decides what to do with the given License result
		 * @param res the license result.
		 */
		private void resultHandler(final int res) {
	        mHandler.post(new Runnable() {
	            public void run() {
	                switch (res) {
	                case 1:
	                	licenseFail();
	                	break;
	                case 2:
	                	retryCount++;
	                	if ( retryCount > 3)
	                		licenseFail();
	                	else
	                		licenseCheck();
	                	break;
	                default:
	                	licenseOK();
	                }
	            }
	        });
	    }

		/**
		 * The License is legit.
		 * @param reason Policy.LICENSED or Policy.RETRY typically.
		 */
	    public void allow(int reason) {
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        // Should allow user access.
	        resultHandler(0);
	    }

		/**
		 * Called when the license check failed
		 * @param reason Policy.NOT_LICENSED or Policy.RETRY.
		 */
	    public void dontAllow(int reason) {
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        
	        if (reason == Policy.RETRY) {
	            // If the reason received from the policy is RETRY, it was probably
	            // due to a loss of connection with the service, so we should give the
	            // user a chance to retry. So show a dialog to retry.
	        	resultHandler(2);
	        } else {
	            // Otherwise, the user is not licensed to use this app.
	            // Your response should always inform the user that the application
	            // is not licensed, but your behavior at that point can vary. You might
	            // provide the user a limited access version of your app or you can
	            // take them to Google Play to purchase the app.
	        	resultHandler(1);
	        }
	    }

		/**
		 * Called when an error happens in license checking
		 * @param errorCode
		 */
		@Override
		public void applicationError(int errorCode) {
			resultHandler(1);
		}
	}

	/**
	 * The app continues here after a successful license check. Builds up the GUI
	 */
	public void licenseOK() {
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

	/**
	 * Calls the license checker library
	 */
	public void licenseCheck() {
		mChecker.checkAccess(mLicenseCheckerCallback);
	}

	/**
	 * Handles failed licenses
	 */
	public void licenseFail() {
				Toast.makeText(getApplicationContext(),
						"License check failed!", Toast.LENGTH_LONG).show();
		new Thread((new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				}
				catch (Exception e) {};
				finish();
			}
		})).start();
	}

	/**
	 * Shows the user a status message
	 * @param msg the message to show
	 * @param sleep how long the messages should be displayed in ms
	 */
	private void setStatus(final String msg, long sleep) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView s = (TextView) findViewById(R.id.tv_status);
				s.setText(msg);
			}
		});
		if (sleep > 0) {
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {}
		}
	}

	/**
	 * Called by the system when the app has started. Defines the content, and checks for license.
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.streaming);
		setStatus("Please wait! Checking license...", 500);

		mHandler = new Handler();
		/*
		// Construct the LicenseCheckerCallback. The library calls this when done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();

        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), License.getID(this))),
            BASE64_PUBLIC_KEY 
            );
        licenseCheck();*/
		licenseOK();

	}

	/**
	 * The FragmentAdapter that handles the switching between the fragments.
	 */
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

	/**
	 * Creates a menu for the applications. The only item in it is Save.
	 * @param menu The menu that should be inflated with our layout.
	 * @return Always true, because we always inflate the menu.
	 */
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

	/**
	 * Central save function, that calls all the fragments save functions.
	 */
	public void save() {
		SmbFragment.init(1).save();
		PvrFragment.init(2).save();
		OtherFragment.init(3).save();
		MainFragment.init(0).save();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        //mChecker.onDestroy();
    }
}