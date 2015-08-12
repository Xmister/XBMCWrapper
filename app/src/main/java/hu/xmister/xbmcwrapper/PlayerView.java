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

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerView extends android.support.v4.app.FragmentActivity {
	private String FileSmb="";
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
    
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
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
	    public void allow(int reason) {
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        // Should allow user access.
	        resultHandler(0);
	    }

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

		@Override
		public void applicationError(int errorCode) {
			// TODO Auto-generated method stub
			resultHandler(1);
		}
	}
    
    public void licenseOK() {
		SharedPreferences se = getSharedPreferences("default",0);
    		new Thread((new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
				if (FileSmb.startsWith("smb://")) {
					Log.d("smbwrapper","Launch SMB: "+FileSmb);
					setStatus("Samba URL detected...",0);
					if (sharedPreferences.getBoolean("r1", false))
						FileSmb = FileSmb.replaceFirst(sharedPreferences.getString("rfrom", "(?i)smb://10.0.1.4/2tb"), sharedPreferences.getString("rto", "file:///mnt/sata"));
					if (sharedPreferences.getBoolean("r2", false))
						FileSmb = FileSmb.replaceFirst(sharedPreferences.getString("rfrom2", "(?i)smb://cubie/2tb"), sharedPreferences.getString("rto2", "file:///mnt/sata"));
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					if (FileSmb.startsWith("file://")) {
						String pkg=sharedPreferences.getString("file", "system");
						setStatus("Launching " + pkg + " with local file...");
						if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
						FileSmb = FileSmb.replaceFirst("(?i)file://", "");
						LaunchIntent.setDataAndType(Uri.fromFile(new File(FileSmb)), "video/*");
						startActivityForResult(LaunchIntent, 1);
					}
					else {
						if ( sharedPreferences.getInt("method", 3) == 3 ) {
							ChoiceDialog md = new ChoiceDialog("Choose Streaming Method",new String[]{"MiniDLNA","CIFS","HTTP"},di,dc,dd);
							md.show(getSupportFragmentManager(),"method");
						}
						else di.onClick(null, sharedPreferences.getInt("method", 2));
					}
				}
				else if (FileSmb.startsWith("file://")) {
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					String pkg=sharedPreferences.getString("file", "system");
					setStatus("Launching " + pkg + " with local file...");
					if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
					FileSmb = FileSmb.replaceFirst("(?i)file://", "");
					LaunchIntent.setDataAndType(Uri.fromFile(new File(FileSmb)), "video/*");
					startActivityForResult(LaunchIntent,1);
				}
				else if (FileSmb.startsWith("http://")) {
					Log.d("smbwrapper","Launch HTTP: "+FileSmb);
					setStatus("Starting HTTP...",0);
					if (sharedPreferences.getBoolean("rehttp", true)) {
						startHTTPStreaming("http",FileSmb);
					}
					else {
						String pkg=sharedPreferences.getString("http", "system");
						setStatus("Launching "+pkg+" with URL");
						Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
						if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
						LaunchIntent.setDataAndType(Uri.parse(FileSmb), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
				}
				else if (FileSmb.startsWith("pvr://")) {
					if ( sharedPreferences.getInt("backend", 1) == 0 ) {
					    setStatus("PVR Disabled");
					    finish();
					    return;
					}
					setStatus("Starting PVR...",0);
					Log.d("smbwrapper","Launch PVR: "+FileSmb);
					Pattern pattern1 = Pattern.compile("([^/]+$)");
					Pattern pattern2 = Pattern.compile("([0-9]*?)\\.pvr$");
					setStatus("Mapping channel...",0);
					Matcher matcher1 = pattern1.matcher(FileSmb);
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
							if ( sts.nextToken().equals(String.valueOf((Integer.valueOf(id)+1))) ) {
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
							url="http://"+host+":"+port+":/upnp/channelstream/"+id+".ts";
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
					LaunchIntent.setDataAndType(Uri.parse(FileSmb), "video/*");
					startActivityForResult(LaunchIntent, 1);
				}
				
			}
		})).start();
	}
	
	public void licenseCheck() {
		mChecker.checkAccess(mLicenseCheckerCallback);
	}
	
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
			final String FileSmbWrap=FileSmb;
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
	
	private void findBB() {
		for (BB_BINARY=0; BB_BINARY<BB_BINARIES.length; BB_BINARY++) {
			if ( new File(BB_BINARIES[BB_BINARY]).exists() ) break;
		}
		BB_BINARY=Math.min(BB_BINARY, BB_BINARIES.length-1);
	}
	
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
	
	private void setStatus(final String msg) {
		setStatus(msg, 1500);
	}
	
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
		Log.d("smbwrapper", "Launch");
		findBB();
		if (extras != null) {
			FileSmb = extras.toString();
			Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_LONG).show();
		}
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
		// Construct the LicenseCheckerCallback. The library calls this when done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();

        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
            this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), License.getID(this))),
            BASE64_PUBLIC_KEY 
            );
        licenseCheck();
		
	}
	
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

	private String getBB() {
		if (BB_BINARIES[BB_BINARY].isEmpty()) return "";
		else return BB_BINARIES[BB_BINARY]+" ";
	}

	private String parseCommand(String cmd, String smbpath) {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		return cmd.replaceAll("(?i)%smbuser%",sharedPreferences.getString("smbuser",""))
				.replaceAll("(?i)%smbpass%", sharedPreferences.getString("smbpass", ""))
				.replaceAll("(?i)%smbpath%", smbpath)
				.replaceAll("(?i)%mount_path%", sharedPreferences.getString("cifs", "/mnt/xbmcwrapper"));
	}
	
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
			if (!smbuser.equals("")) {
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=" + smbuser + (!smbpass.equals("") ? ",password=" + smbpass : "") + ",ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
			} else {
				commands.add("mount -t cifs -o username=guest,ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=guest,ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o username=guest,ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro,iocharset=utf16 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro,iocharset=utf8 " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
				commands.add("mount -t cifs -o user=guest,ro " + smbpath.substring(0, smbpath.indexOf(smbfile) - 1) + " " + MOUNT_PATH);
			}
			int i=0,res=0;
			do {
				cmd=getBB()+commands.get(i++)+"\n";
				if (sharedPreferences.getBoolean("cb_mount",false)) {
					setStatus(cmd,1000);
				}
				Log.d("Mounting CIFS", cmd);
				res=executeSu(cmd);
			} while ( res!= 0 && i<commands.size());
			if (res != 0) throw new Exception("Unable to mount"); //Give up
		}
		final Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("smbwrapper","Launch Player: "+MOUNT_PATH+File.separator+smbfile);
		String pkg=sharedPreferences.getString("samba", "system");
		setStatus("Launching "+pkg+" with CIFS Mounted path...");
		if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
		LaunchIntent.setDataAndType(Uri.fromFile(new File(MOUNT_PATH+File.separator+smbfile)), "video/*");
				startActivityForResult(LaunchIntent,25);
	}
	
	private void startHTTPStreaming(final String protocol, final String FileSmb) {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		
		Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("smbwrapper", "Launch Player: " + FileSmb);
		if ( protocol.equals("smb") ) {
			String smbuser=sharedPreferences.getString("smbuser", "");
			String smbpass=sharedPreferences.getString("smbpass", "");
			if (Serv == null) {
				if ( !smbuser.equals(""))
					Serv=new StreamOverHttp(protocol,FileSmb,smbuser,smbpass);
				else
					Serv=new StreamOverHttp(protocol,FileSmb);
				Serv.start();
			}
			
			try {
				while (Serv.getPort() == 0) Thread.sleep(500);
			} catch (Exception e) {}
			String pkg=sharedPreferences.getString("samba", "system");
			setStatus("Launching " + pkg + " with HTTP Stream from Samba...");
			if (!pkg.equals("system")) LaunchIntent.setPackage(pkg);
			LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:"+Serv.getPort()+"/"+Uri.encode(FileSmb.substring(6).replaceAll("\\+", "%20"), CHARSET)), "video/*");
		}
		else if ( protocol.equals("http") || protocol.equals("pvr") ) {
			if (Serv == null) {
				Serv=new StreamOverHttp("http",FileSmb);
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

