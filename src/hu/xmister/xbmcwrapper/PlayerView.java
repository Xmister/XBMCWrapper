package hu.xmister.xbmcwrapper;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerView extends android.support.v4.app.FragmentActivity {
	private String FileSmb="";
	private StreamOverHttp Serv=null;
	private final String BB_BINARIES[]={"/system/bin/busybox", "/system/xbin/busybox", "/xbin/busybox", "/bin/busybox", "/sbin/busybox", "busybox"};
	private int BB_BINARY=0;
	private String MOUNT_PATH=null;
	private boolean chosen = false;
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
				TextView s= (TextView) findViewById(R.id.tv_status);
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
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Uri extras = getIntent().getData();
		Log.d("smbwrapper","Launch");
		findBB();
		if (extras != null) {
			FileSmb = extras.toString();
			Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_LONG).show();
		}
		setContentView(R.layout.streaming);
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
						String pkg=sharedPreferences.getString("file", "com.mxtech.videoplayer.ad");
						setStatus("Launching "+pkg+" with local file...");
						LaunchIntent.setPackage(pkg);
						FileSmb = FileSmb.replaceFirst("(?i)file://", "");
						LaunchIntent.setDataAndType(Uri.fromFile(new File(FileSmb)), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
					else {
						if ( sharedPreferences.getInt("method", 3) == 3 ) {
							MethodDialog md = new MethodDialog(new String[]{"MiniDLNA","CIFS","HTTP"},di,dc,dd);
							md.show(getSupportFragmentManager(),"method");
						}
						else di.onClick(null, sharedPreferences.getInt("method", 3));
					}
				}
				else if (FileSmb.startsWith("http://")) {
					Log.d("smbwrapper","Launch HTTP: "+FileSmb);
					setStatus("Starting HTTP...",0);
					if (sharedPreferences.getBoolean("rehttp", true)) {
						startHTTPStreaming("http",FileSmb);
					}
					else {
						String pkg=sharedPreferences.getString("http", "com.mxtech.videoplayer.ad");
						setStatus("Launching "+pkg+" with URL");
						Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
						LaunchIntent.setPackage(pkg);
						LaunchIntent.setDataAndType(Uri.parse(FileSmb), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
				}
				else if (FileSmb.startsWith("pvr://")) {
					setStatus("Starting PVR...",0);
					Log.d("smbwrapper","Launch PVR: "+FileSmb);
					Pattern pattern1 = Pattern.compile("([^/]+$)");
					Pattern pattern2 = Pattern.compile("(.*?)\\.pvr$");
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
					String url="http://"+sharedPreferences.getString("tvh", "localhost")+":9981/stream/channelid/"+id+"?mux=pass";
					if (sharedPreferences.getBoolean("restream", true)) {
						startHTTPStreaming("pvr",url);
					}
					else {
						String pkg=sharedPreferences.getString("pvr", "com.mxtech.videoplayer.ad");
						setStatus("Launching "+pkg+" with URL");
						Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
						LaunchIntent.setPackage(pkg);
						LaunchIntent.setDataAndType(Uri.parse(url), "video/*");
						startActivityForResult(LaunchIntent,1);
					}
					
				}
				else  {
					Log.d("PlayerView","Not a smb -"+FileSmb+"-");	
					Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
					
					if (FileSmb.startsWith("/")) {
						LaunchIntent.setDataAndType(Uri.fromFile(new File(FileSmb)),"video/*");
					} else {
						// Youtube
						if (FileSmb.contains("youtube.com") && FileSmb.startsWith("http://")) {
							Log.d("Open youtube","Yes");
							String[] Ur=FileSmb.split(" ");
							if (Ur.length>1) {
								Log.d("Open youtube",Ur[0]);
								LaunchIntent.setDataAndType(Uri.parse(Ur[0]),"video/*");
							}
						} else
							LaunchIntent.setDataAndType(Uri.parse(FileSmb),"video/*");
					}
					startActivityForResult(LaunchIntent,1);
					finish();
					return;
				}
				
			}
		})).start();
		
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
		setStatus("Transfering miniDLNA database...",0);
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
		String pkg=sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad");
		setStatus("Launching "+pkg+" with miniDLNA...");
		LaunchIntent.setPackage(pkg);
		LaunchIntent.setDataAndType(Uri.parse(mediaURL), "video/*");
		startActivityForResult(LaunchIntent,1);
	}
	
	private void cifsMountPlay(final String FileSmb) throws Exception {
		setStatus("Trying CIFS Mount...",0);
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		MOUNT_PATH=sharedPreferences.getString("cifs","/mnt/xbmcwrapper");
		// Perform su to get root privileges
		String cmd=BB_BINARIES[BB_BINARY]+" mkdir -p "+MOUNT_PATH+"\n";
		executeSu(cmd);
		cmd=BB_BINARIES[BB_BINARY]+" chmod 777 "+MOUNT_PATH+"\n";
		executeSu(cmd);
		cmd=BB_BINARIES[BB_BINARY]+" umount "+MOUNT_PATH+"\n";
		executeSu(cmd); //Just to make sure there is no stuck mount.
		String smbpath=FileSmb.replaceFirst("(?i)smb:", "");
		String smbfile=smbpath.substring(2);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		cmd=BB_BINARIES[BB_BINARY]+" mount -t cifs -o username=guest,ro,iocharset=utf8 "+smbpath.substring(0, smbpath.indexOf(smbfile)-1)+" "+MOUNT_PATH+"\n";
		Log.d("Mounting CIFS", cmd);
		if (executeSu(cmd) != 0) {
			//Some devices don't support UTF8
			cmd=BB_BINARIES[BB_BINARY]+" mount -t cifs -o username=guest,ro "+smbpath.substring(0, smbpath.indexOf(smbfile)-1)+" "+MOUNT_PATH+"\n";
			Log.d("Mounting CIFS", cmd);
			if (executeSu(cmd) != 0) throw new Exception("ABC"); //Give up
		}
		final Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("smbwrapper","Launch Player: "+MOUNT_PATH+File.separator+smbfile);
		String pkg=sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad");
		setStatus("Launching "+pkg+" with CIFS Mounted path...");
		LaunchIntent.setPackage(pkg);
		LaunchIntent.setDataAndType(Uri.fromFile(new File(MOUNT_PATH+File.separator+smbfile)), "video/*");
				startActivityForResult(LaunchIntent,25);
	}
	
	private void startHTTPStreaming(final String protocol, final String FileSmb) {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		
		Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("smbwrapper","Launch Player: "+FileSmb);
		if ( protocol.equals("smb") ) {
			if (Serv == null) {
				Serv=new StreamOverHttp(protocol,FileSmb);
				Serv.start();
			}
			
			try {
				while (Serv.getPort() == 0) Thread.sleep(500);
			} catch (Exception e) {}
			String pkg=sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad");
			setStatus("Launching "+pkg+" with HTTP Stream from Samba...");
			LaunchIntent.setPackage(pkg);
			LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:"+Serv.getPort()+"/"+Uri.encode(FileSmb.substring(6), "UTF-8")), "video/*");
		}
		else if ( protocol.equals("http") || protocol.equals("pvr") ) {
			if (Serv == null) {
				Serv=new StreamOverHttp("http",FileSmb);
				Serv.start();
			}
			
			try {
				while (Serv.getPort() == 0) Thread.sleep(500);
			} catch (Exception e) {}
			String pkg=sharedPreferences.getString(protocol, "com.mxtech.videoplayer.ad");
			setStatus("Launching "+pkg+" with HTTP Re-Stream");
			LaunchIntent.setPackage(pkg);
			LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:"+Serv.getPort()+"/video"+System.currentTimeMillis()+".mpeg"), "video/*");
		}
		startActivityForResult(LaunchIntent,1);
		
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
		setStatus("Cleaning up...", 500);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (Serv != null)
					Serv.Stop();
				Serv=null;
				if ( res == 25 && MOUNT_PATH != null ) {
					setStatus("Unmounting CIFS Shares...", 500);
					//We should kill the players, otherwise unmounting is not possible
					SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
					String cmd=BB_BINARIES[BB_BINARY]+" killall "+sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad")+"\n";
					executeSu(cmd);
					cmd=BB_BINARIES[BB_BINARY]+" killall "+sharedPreferences.getString("http", "com.mxtech.videoplayer.ad")+"\n";
					executeSu(cmd);
					cmd=BB_BINARIES[BB_BINARY]+" killall "+sharedPreferences.getString("pvr", "com.mxtech.videoplayer.ad")+"\n";
					executeSu(cmd);
					cmd=BB_BINARIES[BB_BINARY]+" killall "+sharedPreferences.getString("file", "com.mxtech.videoplayer.ad")+"\n";
					executeSu(cmd);
					int i=1;
					//The file handlers may not be free right away
					for (i=1; i<=10; i++) {
						try {
							cmd=BB_BINARIES[BB_BINARY]+" umount "+MOUNT_PATH+"\n";
							Log.d("UnMounting CIFS", cmd);
							int r=executeSu(cmd);
							Log.d("UnMount Result", ""+r);
							if ( r == 0 ) {
								setStatus("Success.", 1000);
								break;
							}
							else throw new Exception("ABC");
						} catch (Exception e) {
							try {
								Thread.sleep(2000);
							} catch (Exception ie) {}
						}
					}
					if (i>10) setStatus("FAILED!", 1000);
				}
				finish();
				System.exit(0);
			}
		}).start();
	}
		
}

