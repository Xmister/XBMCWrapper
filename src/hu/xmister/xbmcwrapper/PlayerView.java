package hu.xmister.xbmcwrapper;

import java.io.DataOutputStream;
import java.io.File;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

public class PlayerView extends Activity {
	private String FileSmb="";
	private StreamOverHttp Serv=null;
	private final String BB_BINARIES[]={"/system/bin/busybox", "/system/xbin/busybox", "/xbin/busybox", "/bin/busybox", "/sbin/busybox", "busybox"};
	private int BB_BINARY=0;
	private final String SD_PATH="/mnt/sdcard";
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		System.exit(0);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		System.exit(0);
	}
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		Uri extras = getIntent().getData();
		Log.d("smbwrapper","Launch");
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		findBB();
		if (extras != null) {
			FileSmb = extras.toString();
		}
		if (FileSmb.startsWith("smb://")) {
			Log.d("smbwrapper","Launch SMB: "+FileSmb);
			if (sharedPreferences.getBoolean("r1", false))
				FileSmb = FileSmb.replaceFirst(sharedPreferences.getString("rfrom", "(?i)smb://10.0.1.4/2tb"), sharedPreferences.getString("rto", "file:///mnt/sata"));
			if (sharedPreferences.getBoolean("r2", false))
				FileSmb = FileSmb.replaceFirst(sharedPreferences.getString("rfrom2", "(?i)smb://cubie/2tb"), sharedPreferences.getString("rto2", "file:///mnt/sata"));
			Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
			if (FileSmb.startsWith("file://")) {
				LaunchIntent.setPackage(sharedPreferences.getString("file", "com.mxtech.videoplayer.ad"));
				FileSmb = FileSmb.replaceFirst("(?i)file://", "");
				LaunchIntent.setDataAndType(Uri.fromFile(new File(FileSmb)), "video/*");
				startActivityForResult(LaunchIntent,1);
			}
			else {
				try {
					//Try to mount it using cifs for best performance
					cifsMountPlay(FileSmb);
				} catch (Exception e) {
					// Failed, fall back to HTTP Stream
					startHTTPStreaming("smb", FileSmb);
				}
			}
		}
		else if (FileSmb.startsWith("http://")) {
			Log.d("smbwrapper","Launch HTTP: "+FileSmb);
			startHTTPStreaming("http",FileSmb);
		}
		else if (FileSmb.startsWith("pvr://")) {
			Log.d("smbwrapper","Launch PVR: "+FileSmb);
			Pattern pattern1 = Pattern.compile("([^/]+$)");
			Pattern pattern2 = Pattern.compile("(.*?)\\.pvr$");
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
			startHTTPStreaming("pvr",url);
			
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
	
	private void cifsMountPlay(final String FileSmb) throws Exception {
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		// Perform su to get root privileges
		File directory = new File(SD_PATH+File.separator+"xbmcwrapper");
		directory.mkdirs();
		String cmd=BB_BINARIES[BB_BINARY]+" umount "+SD_PATH+File.separator+"xbmcwrapper"+"\n";
		executeSu(cmd); //Just to make sure there is no stuck mount.
		String smbpath=FileSmb.replaceFirst("(?i)smb:", "");
		String smbfile=smbpath.substring(2);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		smbfile=smbfile.substring(smbfile.indexOf('/')+1);
		cmd=BB_BINARIES[BB_BINARY]+" mount -t cifs -o username=guest,ro,iocharset=utf8 "+smbpath.substring(0, smbpath.indexOf(smbfile)-1)+" "+SD_PATH+File.separator+"xbmcwrapper"+"\n";
		Log.d("Mounting CIFS", cmd);
		if (executeSu(cmd) != 0) {
			//Some device doesn't support UTF8
			cmd=BB_BINARIES[BB_BINARY]+" mount -t cifs -o username=guest,ro "+smbpath.substring(0, smbpath.indexOf(smbfile)-1)+" "+SD_PATH+File.separator+"xbmcwrapper"+"\n";
			Log.d("Mounting CIFS", cmd);
			if (executeSu(cmd) != 0) throw new Exception(); //Give up
		}
		Intent LaunchIntent = new Intent(Intent.ACTION_VIEW);
		Log.d("smbwrapper","Launch Player: "+SD_PATH+File.separator+"xbmcwrapper"+File.separator+smbfile);
		LaunchIntent.setPackage(sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad"));
		LaunchIntent.setDataAndType(Uri.fromFile(new File(SD_PATH+File.separator+"xbmcwrapper"+File.separator+smbfile)), "video/*");
		startActivityForResult(LaunchIntent,1);
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
			LaunchIntent.setPackage(sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad"));
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
			LaunchIntent.setPackage(sharedPreferences.getString(protocol, "com.softwinner.TvdVideo"));
			LaunchIntent.setDataAndType(Uri.parse("http://127.0.0.1:"+Serv.getPort()+"/video"+System.currentTimeMillis()+".mpeg"), "video/*");
		}
		startActivityForResult(LaunchIntent,1);
		
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		cleanup();
	}
	
	@Override
	public void onBackPressed() {
		cleanup();
	}
	
	private void cleanup() {
		if (Serv != null)
			Serv.Stop();
		Serv=null;
		//The file handlers may not be free right away
		for (int i=1; i<=10; i++) {
			try {
				String cmd=BB_BINARIES[BB_BINARY]+" umount "+SD_PATH+File.separator+"xbmcwrapper"+"\n";
				Log.d("UnMounting CIFS", cmd);
				int r=executeSu(cmd);
				Log.d("UnMount Result", ""+r);
				if ( r == 0 ) break;
				else throw new Exception();
			} catch (Exception e) {
				try {
					Thread.sleep(2000);
				} catch (Exception ie) {}
			}
		}
		finish();
		System.exit(0);
	}
		
}

