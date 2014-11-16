package hu.xmister.xbmcwrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import android.util.Log;

public class StreamOverHttp extends Thread {

   public Boolean Stoping=false;
   private ServerSocket serverSocket=null;
   private int port=0;
   private String protocol = "smb";
   private String url;
   private String _SmbUser=null;
   private String _SmbPass=null;
   
   public synchronized int getPort() {
	   return port;
   }
   
   public synchronized String getUrl() {
	   return url;
   }
   
   public synchronized String getProtocol() {
	   return protocol;
   }
   
   private void startStreaming() {
	   port=37689;
	   try {
		   serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
	   } catch (Exception e) {
		   port=0;
	   }
   }
   
   public StreamOverHttp(String protocol, String url) {
	   this.protocol=protocol;
	   this.url=url;
	   startStreaming();
   }
   
   public StreamOverHttp(String protocol, String url, String sambaUser, String sambaPass) {
	   this.protocol=protocol;
	   this.url=url;
	   _SmbUser=sambaUser;
	   _SmbPass=sambaPass;
	   startStreaming();
   }
   
   @Override
   public void run() {
	// Create socket
	
	try {
		serverSocket = new ServerSocket();
		serverSocket.setSoTimeout(500);
		while (port == 0) {
			try {
				Random r = new Random(System.currentTimeMillis());
				port=r.nextInt(32000);
				port+=10000;
				serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
				break;
			} catch (Exception e) {
				port=0;
				//TODO
			}
		}
		
		
	} catch (IOException e) {
		Log.d("ServiceHttp", "Socket already in use , abort");
		return;
	}
	while (!Stoping) {
		Socket Sock = null;

		try {
			Sock = serverSocket.accept();
		} catch (IOException e)  {
				continue;
		}
		
		if (Sock != null) {
			Sending SendSock=new Sending(Sock,this,_SmbUser,_SmbPass);
			SendSock.start();
		}
		else
			break;
	} 
   }
   
   public void Stop() {
	   Log.d("ServiceHttp", "Request Stop");
	   Stoping=true;
	   if (serverSocket!=null) {
		   try {
			serverSocket.close();
		} catch (IOException e) {}
	   }
   }
}
 

