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
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

public class PlayerView extends Activity {
	private String FileSmb="";
	private StreamOverHttp Serv=null;
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		Uri extras = getIntent().getData();
		Log.d("smbwrapper","Launch");
		SharedPreferences sharedPreferences = getSharedPreferences("default", 0);
		if (extras != null) {
			FileSmb = extras.toString();
		}
		if (FileSmb.startsWith("smb://")) {
			
			// Demarre rockplayer
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
					//busybox mount -t cifs -o username=guest,user,rw,iocharset=utf8
					// Preform su to get root privledges  
					File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"xbmcwrapper");
					directory.mkdirs();
					FileSmb = FileSmb.replaceFirst("(?i)smb:", "");
					String smbfile=FileSmb;
					smbfile=smbfile.substring(2);
					smbfile=smbfile.substring(smbfile.indexOf('/')+1);
					smbfile=smbfile.substring(smbfile.indexOf('/')+1);
					String cmd="/system/bin/busybox mount -t cifs -o username=guest,user,rw,iocharset=utf8 "+FileSmb.substring(0, FileSmb.indexOf(smbfile)-1)+" "+Environment.getExternalStorageDirectory()+File.separator+"xbmcwrapper"+"\n";
					Log.d("Mounting CIFS", cmd);
					if (executeSu(cmd) != 0) throw new Exception();
					LaunchIntent = new Intent(Intent.ACTION_VIEW);
					Log.d("smbwrapper","Launch Player: "+FileSmb);
					LaunchIntent.setPackage(sharedPreferences.getString("samba", "com.mxtech.videoplayer.ad"));
					LaunchIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"xbmcwrapper"+File.separator+smbfile)), "video/*");
					startActivityForResult(LaunchIntent,1);
				} catch (Exception e) {
					// Failed, fall back to HTTP Stream
					startHTTPStreaming("smb", FileSmb);
				}
			}
		}
		else if (FileSmb.startsWith("http://")) {
			
			// Demarre le servceur
			Log.d("smbwrapper","Launch HTTP: "+FileSmb);
			/*Intent LaunchIntent = new Intent(this,Streaming.class);
			LaunchIntent.setDataAndType(Uri.parse(FileSmb), "video/*");
			startActivity(LaunchIntent);*/
			//startDLNAPlayer(FileSmb);
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
			/*switch (Integer.valueOf(id)+1) {
			case 9: 				//XBMC ID
				id="3";				//Real ID
				break;
			case 10:
				id="2";
				break;
			case 3:
				id="4";
				break;
			case 4:
				id="7";
				break;
			case 5:
				id="9";
				break;
			case 6:
				id="11";
				break;
			case 1:
				id="12";
				break;
			case 2:
				id="14";
				break;
			case 7:
				id="13";
				break;
			case 8:
				id="1";
				break;
				//
			}*/
			String url="http://"+sharedPreferences.getString("tvh", "localhost")+":9981/stream/channelid/"+id+"?mux=pass";
			//startDLNAPlayer(url);
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
				String cmd="/system/bin/busybox umount "+Environment.getExternalStorageDirectory().getPath()+File.separator+"xbmcwrapper"+"\n";
				Log.d("UnMounting CIFS", cmd);
				int r=executeSu(cmd);
				Log.d("UnMount Result", ""+r);
				if ( r == 0 ) break;
				else throw new Exception();
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (Exception ie) {}
			}
		}
		finish();
		System.exit(0);
	}
		
}

