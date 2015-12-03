package hu.xmister.xbmcwrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import android.util.Log;

/**
 * The StreamOverHttp class is responsible for handling the HTTP server connections.
 */
public class StreamOverHttp extends Thread {

   public Boolean Stopping =false;
   private ServerSocket serverSocket=null;
   private int port=37689;
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

	/**
	 * Starts the listening on the default port, or if it's not available, then finds one that is.
	 * @throws IOException
	 */
   private void startStreaming() throws IOException {
			serverSocket = new ServerSocket();
			serverSocket.setSoTimeout(500);
			boolean suc=false;
			do {
				try {
					serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
					suc=true;
					break;
				} catch (Exception e) {
					Random r = new Random(System.currentTimeMillis());
					port=r.nextInt(32000);
					port+=10000;
				}
			} while (suc == false);
			
   }
   
   public StreamOverHttp(String protocol, String url) {
	   this.protocol=protocol;
	   this.url=url;
   }
   
   public StreamOverHttp(String protocol, String url, String sambaUser, String sambaPass) {
	   this.protocol=protocol;
	   this.url=url;
	   _SmbUser=sambaUser;
	   _SmbPass=sambaPass;
   }

	/**
	 * This Thread is responsible for accepting new connections, and creating a SendingClass for them.
	 */
   @Override
   public void run() {
	// Create socket
	
	try {
		startStreaming();
	} catch (IOException e) {
		Log.d("ServiceHttp", "Socket already in use , abort");
		return;
	}
	while (!Stopping) {
		Socket Sock = null;

		try {
			Sock = serverSocket.accept();
		} catch (IOException e)  {
				continue;
		}
		
		if (Sock != null) {
			SendingClass SendSock=new SendingClass(Sock,this,_SmbUser,_SmbPass);
			SendSock.start();
		}
		else
			break;
	} 
	if (serverSocket!=null) {
		   try {
			serverSocket.close();
			serverSocket=null;
		} catch (IOException e) {}
	   }
   }
   
   public void Stop() {
	   Log.d("ServiceHttp", "Request Stop");
	   Stopping =true;
	   if (serverSocket!=null) {
		   try {
			serverSocket.close();
		} catch (IOException e) {}
	   }
   }
}
 

