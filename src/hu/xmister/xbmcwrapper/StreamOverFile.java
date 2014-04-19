package hu.xmister.xbmcwrapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public class StreamOverFile extends Thread {
	private InputStream in;
	private OutputStream out;
	private String path="/tmp";
	private int fileCount=4;
	private long fileSize=32*1024*1024;
	private int maxFileCount=9999;
	private String m3uFile;
	private boolean good2Go=false;
	private String url;
	private HttpURLConnection connection;
	private boolean stopped=false;

	public StreamOverFile(String url) {
		this.url=url;
		m3uFile=path+"/stream.m3u";
			
	}
	
	private void createM3U() throws IOException {
		OutputStreamWriter f = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(m3uFile)));
		int currentFile=1;
		for (int i=1; i <= maxFileCount; i++) {
			f.write(path+"/stream_"+(currentFile++)+".mkv\n");
			if ( currentFile > fileCount ) currentFile=1;
		}
		f.close();
	}
	
	public String getM3U() {
		return m3uFile;
	}
	
	public boolean getGood2Go() {
		return good2Go;
	}
	
	public synchronized void stopIt() {
		stopped=true;
	}

	private void StartStreaming() {
		byte[] buffer=new byte[128000];
		int count=0;
		int read=0;
		int currentFile=1;
		String fileName; 
		try {
			URL address = new URL(url); 
	        //byte[] header = EBMLReader.getEBMLCodeAsBytes(new InputStreamDataSource(in));
			for (int i=1; i <= maxFileCount && !stopped; i++) {
				fileName=path+"/stream_"+(currentFile++)+".mkv";
				connection = (HttpURLConnection)address.openConnection();
				in=connection.getInputStream();
				if ( new File(fileName).exists() ) new File(fileName).delete();
				read=0;
		        out = new FileOutputStream(fileName, false);
				while ( (count=in.read(buffer,0,buffer.length)) > 0 && read < fileSize && !stopped ) {
					out.write(buffer, 0, count);
					read+=count;
				}
				try {
					out.close();
					connection.disconnect();
				} catch (Exception e) {
					//Don't care
				}
				new File(fileName).setReadable(true, false);
				if (!good2Go) good2Go=true;
				try {
					if (currentFile > fileCount) {
						new File(path+"/stream_"+(currentFile-fileCount)+".mkv").delete();
					}
				} catch (Exception e) {
					//Don't care
				}
			}
		} catch (Exception e) {
			Log.e("StreamOverFile","Streaming: "+e.getMessage());
		}
	}
	
	public void run() {
	try {
		if ( !(new File(path).exists()) ) new File(path).mkdirs();
		createM3U();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				StartStreaming();
			}
		}).start();
		
	} catch (Exception e) {
		Log.e("StreamOverFile","Construct: "+e.getMessage());
	}
	}
}
