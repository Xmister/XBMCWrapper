package hu.xmister.xbmcwrapper;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The StartPlayer Activity is repsonsible for taking care of the input URL, and starting the needed player.
 */
public class StartPlayer extends android.support.v4.app.FragmentActivity {
	private String inputURL ="";
	private StreamOverHttp Serv=null;
	private final String BB_BINARIES[]={"/system/bin/busybox", "/system/xbin/busybox", "/xbin/busybox", "/bin/busybox", "/sbin/busybox", "busybox", ""};
	private int BB_BINARY=0;
	private String MOUNT_PATH=null;
	private boolean chosen = false;
	private Handler mHandler;
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private static final String BASE64_PUBLIC_KEY = License.getKey();
    private static final byte[] SALT = License.getSalt();
	private String CHARSET;
    public int retryCount=0;
	private boolean cleaning=false;
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
	 * The app continues here after a successful license check. URL parsing happens here
	 */
    public void licenseOK() {
		SharedPreferences se = getSharedPreferences("default",0);
    		new Thread((new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
				if (inputURL.startsWith("smb://")) {
					Log.d("xbmcwrapper","Launch SMB: "+ inputURL);
					setStatus("Samba URL detected...",0);
					//URL replace
					if (sharedPreferences.getBoolean("r1", false))
						inputURL = inputURL.replaceFirst(sharedPreferences.getString("rfrom", "(?i)smb://10.0.1.4/2tb"), sharedPreferences.getString("rto", "file:///mnt/sata"));
					if (sharedPreferences.getBoolean("r2", false))
						inputURL = inputURL.replaceFirst(sharedPreferences.getString("rfrom2", "(?i)smb://cubie/2tb"), sharedPreferences.getString("rto2", "file:///mnt/sata"));
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);

					//If the replaced URL became a local file, then just play it
					if (inputURL.startsWith("file://")) {
						String pkg=sharedPreferences.getString("file", "system");
						setStatus("Launching " + pkg + " with local file...");
						if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
						inputURL = inputURL.replaceFirst("(?i)file://", "");
						LaunchIntent.setDataAndType(Uri.fromFile(new File(inputURL)), "video/*");
						startActivityForResult(LaunchIntent, 1);
					}
					else {
						if ( sharedPreferences.getInt("method", 2) == 3 ) {
							//Show a dialog, if the user didn't set a specifig method
							ChoiceDialog md = new ChoiceDialog("Choose Streaming Method",new String[]{"MiniDLNA","CIFS","HTTP"},di,dc,dd);
							md.show(getSupportFragmentManager(),"method");
						}
						else di.onClick(null, sharedPreferences.getInt("method", 2));
					}
				}
				else if (inputURL.startsWith("file://")) {
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					String pkg=sharedPreferences.getString("file", "system");
					setStatus("Launching " + pkg + " with local file...");
					if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
					inputURL = inputURL.replaceFirst("(?i)file://", "");
					LaunchIntent.setDataAndType(Uri.fromFile(new File(inputURL)), "video/*");
					startActivityForResult(LaunchIntent,1);
				}
				else if (inputURL.startsWith("dav://")) {
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					String pkg=sharedPreferences.getString("http", "system");
					setStatus("Launching " + pkg + " with local DAV->HTTP URL...");
					if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
					inputURL = inputURL.replaceFirst("(?i)dav://", "http://");
					LaunchIntent.setDataAndType(Uri.parse(inputURL), "video/*");
					startActivityForResult(LaunchIntent, 1);
				}
				else if (inputURL.startsWith("http://")) {
					Log.d("xbmcwrapper","Launch HTTP: "+ inputURL);
					setStatus("Starting HTTP...",0);
					//If we should re-stream it, then start the server
					if (sharedPreferences.getBoolean("rehttp", true)) {
						startHTTPStreaming("http", inputURL);
					}
					//Otherwise just pass the URL
					else {
						String pkg=sharedPreferences.getString("http", "system");
						setStatus("Launching "+pkg+" with URL");
						Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
						if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
						LaunchIntent.setDataAndType(Uri.parse(inputURL), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
				}
				else if (inputURL.startsWith("pvr://")) {
					if ( sharedPreferences.getInt("backend", 1) == 0 ) {
					    setStatus("PVR Disabled");
					    finish();
					    return;
					}
					setStatus("Starting PVR...",0);
					Log.d("xbmcwrapper","Launch PVR: "+ inputURL);
					//Get the sent channel number
					Pattern pattern1 = Pattern.compile("([^/]+$)");
					Pattern pattern2 = Pattern.compile("([0-9]*?)\\.pvr$");
					setStatus("Mapping channel...",0);
					Matcher matcher1 = pattern1.matcher(inputURL);
					String id=null;
					if (matcher1.find())
					{
					    id=matcher1.group(1);
					    Matcher matcher2 = pattern2.matcher(id);
					    if (matcher2.find())
					    	id=matcher2.group(1);
					}
					String kodid=id;
					setStatus("Mapping channel "+kodid+"->",0);
					String pvrmap=sharedPreferences.getString("pvrmap", null);
					if ( pvrmap != null ) {
						StringTokenizer st = new StringTokenizer(pvrmap, ";");
						while (st.hasMoreTokens()) {
							String token=st.nextToken();
							StringTokenizer sts=new StringTokenizer(token, ",");
							//if ( sts.nextToken().equals(String.valueOf((Integer.valueOf(id)+1))) ) {
							if ( sts.nextToken().equals(String.valueOf((Integer.valueOf(id)))) ) {
								id=sts.nextToken();
								break;
							}
						}
					}
					setStatus("Mapping channel "+kodid+" -> "+id,1000);
					String url=null;
					String host=sharedPreferences.getString("tvh", "localhost");
			    	String port=null;
			    	int pp=host.indexOf(':');
			    	if ( pp > -1 ) {
			    		port=host.substring(pp+1);
			    		host=host.substring(0, pp);
			    	}
					switch ( sharedPreferences.getInt("backend", 1) ) {
					    case 1:
							if (port == null) {
								port="9981";
							}
							url="http://"+host+":"+port+"/stream/channelid/"+id+"?mux=pass";
							break;
					    case 2:
					    	if (port == null) {
								port="7522";
							}
							url="http://"+host+":"+port+"/upnp/channelstream/"+id+".ts";
							break;
						case 3:
							//http://10.0.1.10:8866/live?channel=2
							if (port == null) {
								port="8866";
							}
							url="http://"+host+":"+port+"/live?channel="+id;
							break;
					    default:
					    	url=null;
					}
					if (url == null) {
						finish();
					    return;
					}
					if (sharedPreferences.getBoolean("restream", true)) {
						startHTTPStreaming("pvr",url);
					}
					else {
						String pkg=sharedPreferences.getString("pvr", "system");
						setStatus("Launching "+pkg+" with URL");
						Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
						if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
						LaunchIntent.setDataAndType(Uri.parse(url), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
					
				}
				else  {
					String pkg=sharedPreferences.getString("file", "system");
					setStatus("Launching " + pkg + " with URL");
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
					LaunchIntent.setDataAndType(Uri.parse(inputURL), "video/*");
					startActivityForResult(LaunchIntent, 1);
				}
				
			}
		})).start();
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
		new Thread((new Runnable() {

			@Override
			public void run() {
				setStatus("License check failed!", 3000);
				finish();
				//System.exit(0);
			}
		})).start();
	}
    
	private DialogInterface.OnDismissListener dd = new DialogInterface.OnDismissListener() {
		
		@Override
		public void onDismiss(DialogInterface dialog) {
			if (!chosen) cleanup(1);
		}
	};
	private DialogInterface.OnCancelListener dc = new DialogInterface.OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			cleanup(1);
		}
	};
	private DialogInterface.OnClickListener di = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			final String FileSmbWrap= inputURL;
			switch (which) {
			case 0:
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							miniDLNAPlay(FileSmbWrap);
						}
						catch (Exception e) {
							Log.e("miniDLNA", e.getMessage());
							cleanup(1);
						}
					}
				}).start();
				break;
			case 1:
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							cifsMountPlay(FileSmbWrap);
						}
						catch (Exception e) {
							Log.e("CIFS", e.getMessage());
							cleanup(1);
						}
					}
				}).start();
				break;
			case 2:
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							startHTTPStreaming("smb", FileSmbWrap);
						}
						catch (Exception e) {
							Log.e("HTTP", e.getMessage());
							cleanup(1);
						}
					}
				}).start();
				break;
			default:
				cleanup(1);
				return;
			}
			chosen=true;
		}
	};

	/**
	 * Executes command in SuperUser Shell
	 * @param cmd the command to execute
	 * @return the result of the execution
	 */
	private int executeSu(String cmd) {
		try {
			Process p = Runtime.getRuntime().exec("su");   
			DataOutputStream os = new DataOutputStream(p.getOutputStream()); 
			os.writeBytes(cmd);
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			p.waitFor();
			return p.exitValue();
		}
		catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Finds the busybox binary
	 */
	private void findBB() {
		for (BB_BINARY=0; BB_BINARY<BB_BINARIES.length; BB_BINARY++) {
			if ( new File(BB_BINARIES[BB_BINARY]).exists() ) break;
		}
		BB_BINARY=Math.min(BB_BINARY, BB_BINARIES.length-1);
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
	 * Shows the user a status message to the user for 1500ms
	 * @param msg the message to show
	 */
	private void setStatus(final String msg) {
		setStatus(msg, 1500);
	}

	/**
	 * Called by the system when the app has started. Parses the input, defines the content, and checks for license.
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.streaming);
		SharedPreferences se = getSharedPreferences("default",0);
		if ( se.getInt("theme",0) == 0) {

		}
		else {
			((LinearLayout)findViewById(R.id.LinearLayout1)).setBackgroundColor(Color.BLACK);
			((TextView)findViewById(R.id.tv_status)).setTextColor(Color.WHITE);
		}
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		mHandler = new Handler();
		Uri extras = getIntent().getData();
		Log.d("xbmcwrapper", "Launch");
		findBB();
		if (extras != null) {
			inputURL = extras.toString();
			Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_LONG).show();
		}
		//Charset settings for mounting. Not used right now.
		switch ( se.getInt("charset",0) ) {
			case 0:
				CHARSET="UTF-8";
				break;
			case 1:
				CHARSET="UTF-16";
				break;
			case 2:
				CHARSET="UTF-16LE";
				break;
			case 3:
				CHARSET="UTF-16BE";
				break;
		}

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
	 * Plays the given Samba file through a MiniDLNA server
	 * @param FileSmb the file to play
	 * @throws Exception usually I/O or connection exception can happen
	 */
	private void miniDLNAPlay(final String FileSmb) throws Exception {
		setStatus("Trying miniDLNA...",0);
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		String dburl=sharedPreferences.getString("mddb", "smb://10.0.1.4/2TB/minidlna/files.db");
		InputStream dbfile;
		if ( dburl.startsWith("smb://") ) {
			dbfile=new SmbFileInputStream(new SmbFile(dburl));
		}
		else {
			if (dburl.startsWith("file://") ) {
				dburl=dburl.replaceFirst("(?i)file://", "");
			}
			dbfile=new FileInputStream(new File(dburl));
		}
		FileOutputStream localdbo=new FileOutputStream( new File(getExternalFilesDir(null).getPath()+File.separator+"files.db"));
		byte[] buf = new byte[1024*1024];
		setStatus("Transfering miniDLNA database...", 0);
		int count;
		try {
			while ( (count=dbfile.read(buf)) > 0) localdbo.write(buf, 0, count);
		} catch (Exception e) {}
		localdbo.flush();
		localdbo.close();
		dbfile.close();
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getExternalFilesDir(null).getPath()+File.separator+"files.db", null, SQLiteDatabase.CONFLICT_NONE);
		String smbpath=FileSmb.replaceFirst("(?i)smb:", "");
		String smbfile=smbpath.substring(2);
		for (int x=0; x<sharedPreferences.getInt("mdcut", 2); x++) {
			smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		}
		Cursor c=db.rawQuery("SELECT ID FROM DETAILS WHERE PATH LIKE ?", new String[]{"%"+smbfile});
		c.moveToFirst();
		int id=c.getInt(0);
		String mediaURL="http://10.0.1.4:8203/MediaItems/"+id+".mkv";
		Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("miniDLNA","Launch Player: "+mediaURL);
		String pkg=sharedPreferences.getString("samba", "system");
		setStatus("Launching "+pkg+" with miniDLNA...");
		if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
		LaunchIntent.setDataAndType(Uri.parse(mediaURL), "video/*");
		startActivityForResult(LaunchIntent,1);
	}

	/**
	 * Returns the already found busybox binary if any, otherwise an empty string
	 * @return the binary
	 */
	private String getBB() {
		if (BB_BINARIES[BB_BINARY].isEmpty()) return "";
		else return BB_BINARIES[BB_BINARY]+" ";
	}

	/**
	 * Parses a custom mount command and replaces the special words with data
	 * @param cmd the command to parse
	 * @param smbpath samba share path
	 * @return the parsed command
	 */
	private String parseCommand(String cmd, String smbpath) {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		return cmd.replaceAll("(?i)%smbuser%",sharedPreferences.getString("smbuser",""))
				.replaceAll("(?i)%smbpass%", sharedPreferences.getString("smbpass", ""))
				.replaceAll("(?i)%smbpath%", smbpath)
				.replaceAll("(?i)%mount_path%", sharedPreferences.getString("cifs", "/mnt/xbmcwrapper"));
	}

	/**
	 * Mounts the samba share using cifs, and calls the player to play from there.
	 * Needs root and busybox.
	 * Can use default commands or custom ones by the user.
	 * @param FileSmb
	 * @throws Exception
	 */
	private void cifsMountPlay(final String FileSmb) throws Exception {
		setStatus("Trying CIFS Mount...",0);
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		MOUNT_PATH=sharedPreferences.getString("cifs","/mnt/xbmcwrapper");
		String smbpath=FileSmb.replaceFirst("(?i)smb:", "");
		String smbfile=smbpath.substring(2);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		String smbuser=sharedPreferences.getString("smbuser","");
		String smbpass=sharedPreferences.getString("smbpass","");
		ArrayList<String> commands = new ArrayList<String>();
		String cmd=null;
		if (sharedPreferences.getBoolean("cb_mount",false)) {
			StringTokenizer st = new StringTokenizer(sharedPreferences.getString("tb_mount",""),";");
			while (st.hasMoreTokens()) {
				commands.add(parseCommand(st.nextToken(), smbpath.substring(0, smbpath.indexOf(smbfile) - 1)));
			}
			int i=0,res=0;
			do {
				cmd=getBB()+commands.get(i++)+"\n";
				if (sharedPreferences.getBoolean("cb_debug",false))
					setStatus(cmd,5000);
				Log.d("Mounting CIFS", cmd);
				res=executeSu(cmd);
				Log.d("Result code", ""+res);
			} while (i<commands.size());
			if (res != 0) throw new Exception("Unable to mount"); //Give up
		}
		else {
			// Perform su to get root privileges
			cmd=getBB()+"mount -o remount,rw /\n";
			executeSu(cmd);
			cmd=getBB()+"mkdir -p "+MOUNT_PATH+"\n";
			executeSu(cmd);
			cmd=getBB()+"chmod 777 "+MOUNT_PATH+"\n";
			executeSu(cmd);
			cmd=getBB()+"umount "+MOUNT_PATH+"\n";
			executeSu(cmd); //Just to make sure there is no stuck mount.
			cmd=getBB()+"mount -o remount,ro /\n";
			executeSu(cmd);
			//Try some commands that should work with most versions of the cifs module.
			if (!smbuser.equals("")) {
				//User login
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
			} else {
				//Anonymus
				commands.add("mount -t cifs -o username=guest,ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=guest,ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=guest,ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
			}
			int i=0,res=0;
			//Execute the commands
			do {
				cmd=getBB()+commands.get(i++)+"\n";
				//Show some dbug if needed
				if (sharedPreferences.getBoolean("cb_mount",false)) {
					setStatus(cmd,1000);
				}
				Log.d("Mounting CIFS", cmd);
				res=executeSu(cmd);
			} while ( res!= 0 && i<commands.size());
			if (res != 0) throw new Exception("Unable to mount"); //Give up
		}
		final Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("xbmcwrapper","Launch Player: "+MOUNT_PATH+File.separator+smbfile);
		String pkg=sharedPreferences.getString("samba", "system");
		setStatus("Launching "+pkg+" with CIFS Mounted path...");
		if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
		LaunchIntent.setDataAndType(Uri.fromFile(new File(MOUNT_PATH+File.separator+smbfile)), "video/*");
				startActivityForResult(LaunchIntent,25);
	}

	/**
	 * Starts the HTTP server with either a Samba share file, or another http stream
	 * @param protocol can be smb, http, or pvr
	 * @param fileUrl this is the path for the file or the other stream
	 */
	private void startHTTPStreaming(final String protocol, final String fileUrl) {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		
		Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("xbmcwrapper", "Launch Player: " + fileUrl);
		if ( protocol.equals("smb") ) {
			String smbuser=sharedPreferences.getString("smbuser", "");
			String smbpass=sharedPreferences.getString("smbpass", "");
			if (Serv == null) {
				if ( !smbuser.equals(""))
					Serv=new StreamOverHttp(protocol,fileUrl,smbuser,smbpass);
				else
					Serv=new StreamOverHttp(protocol,fileUrl);
				Serv.start();
			}
			
			try {
				//Wait for the server to find a port to listen on
				while (Serv.getPort() == 0) Thread.sleep(500);
			} catch (Exception e) {}
			//Read the player settings
			String pkg=sharedPreferences.getString("samba", "system");
			setStatus("Launching " + pkg + " with HTTP Stream from Samba...");
			if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
			try {
				LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:" + Serv.getPort() + "/" + fileUrl.substring(6)), "video/*");
			} catch (Exception e) {
				Log.e("urlencode",e.getMessage());
			}
		}
		else if ( protocol.equals("http") || protocol.equals("pvr") ) {
			if (Serv == null) {
				Serv=new StreamOverHttp("http",fileUrl);
				Serv.start();
			}
			
			try {
				while (Serv.getPort() == 0) Thread.sleep(500);
			} catch (Exception e) {}
			String pkg=sharedPreferences.getString(protocol, "system");
			setStatus("Launching " +pkg+" with HTTP Re-Stream");
			if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
			LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:"+Serv.getPort()+"/video"+System.currentTimeMillis()+".mpeg"), "video/*");
		}
		startActivityForResult(LaunchIntent, 1);
		
	}
	
	
	
	@Override
	protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		cleanup(requestCode);
	}
	
	@Override
	public void onBackPressed() {
			cleanup(1);
	}

	/**
	 * Cleans up the system and kills the players
	 * @param res defines what should be cleaned up
	 */
	private void cleanup(final int res) {
		if ( cleaning ) {

		}
		else {
			cleaning=true;
			setStatus("Cleaning up...", 500);
			new Thread(new Runnable() {

				@Override
				public void run() {
					if (Serv != null)
						Serv.Stop();
					Serv = null;
					if (res == 25 && MOUNT_PATH != null) {
						setStatus("Unmounting CIFS Shares...", 500);
						//We should kill the players, otherwise unmounting is not possible
						SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
						String cmd = getBB() + "killall " + sharedPreferences.getString("samba", "system") + "\n";
						executeSu(cmd);
						cmd = getBB() + "killall " + sharedPreferences.getString("http", "system") + "\n";
						executeSu(cmd);
						cmd = getBB() + "killall " + sharedPreferences.getString("pvr", "system") + "\n";
						executeSu(cmd);
						cmd = getBB() + "killall " + sharedPreferences.getString("file", "system") + "\n";
						executeSu(cmd);
						int i = 1;
						//The file handlers may not be free right away
						for (i = 1; i <= 10; i++) {
							try {
								cmd = getBB() + "umount " + MOUNT_PATH + "\n";
								Log.d("UnMounting CIFS", cmd);
								int r = executeSu(cmd);
								Log.d("UnMount Result", "" + r);
								if (r == 0) {
									setStatus("Success.", 1000);
									break;
								} else throw new Exception("ABC");
							} catch (Exception e) {
								try {
									Thread.sleep(2000);
								} catch (Exception ie) {
								}
							}
						}
						if (i > 10) setStatus("FAILED!", 1000);
					}
					finish();
					//System.exit(0);
				}
			}).start();
		}
	}
		
}

