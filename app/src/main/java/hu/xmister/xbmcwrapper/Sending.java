package hu.xmister.xbmcwrapper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

public class Sending extends Thread {
	   
	   private StreamOverHttp _Del; 
	   private String _fileMimeType;
	   private long _FileSize;
	   private SmbFileInputStream _SmbStream=null;
	   private InputStream _Stream=null;
	   private InputStream inS=null;
	   private String _SmbUser=null;
	   private String _SmbPass=null;
	   
	   private static final String 
	      HTTP_BADREQUEST = "400 Bad Request",
	      HTTP_416 = "416 Range not satisfiable",
	      HTTP_INTERNALERROR = "500 Internal Server Error",
	   	  HTTP_404 = "404 Not Found",
	   	  HTTP_401 = "401 Not Allowed";
	   
	   private Socket _Socket;
	   
	   public Sending(Socket sock,StreamOverHttp Del) {
		   _Socket=sock;
		   _Del=Del;
	   }
	   public Sending(Socket sock,StreamOverHttp Del,String username, String password) {
		   _Socket=sock;
		   _Del=Del;
		   _SmbUser=username;
		   _SmbPass=password;
	   }

	@Override
	   public void run() {
		   StartReceive();
	   }
	
	 private void StartReceive() {

	
	   try{
         inS = _Socket.getInputStream();
         
         if(inS == null) {
         	Log.d("handleResponse","Null Stream");
         	sendError(_Socket, HTTP_404, null);
         	return;
         }
         
         byte[] buf = new byte[320];
         int rlen = inS.read(buf, 0, buf.length);
         if(rlen <= 0) {
         	Log.d("handleResponse","Null Size");
         	sendError(_Socket, HTTP_404, null);
            return;
         }
         // Create a BufferedReader for parsing the header.
         ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
         BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
         Properties pre = new Properties();
         Log.d("handleResponse","Decode header");
         
         // Decode the header into params and header java properties
         String R=decodeHeader(_Socket, hin, pre);  
         
         if (R != null) {
         	if (R.equals("404"))
         			sendError(_Socket, HTTP_404, "File not found");
         		else if (R.equals("401"))
         			sendError(_Socket, HTTP_401, "Listing directory not allowed");
         		else
         			sendError(_Socket, HTTP_BADREQUEST, R);
            return;
         }
         
         String range = pre.getProperty("range");

         Properties headers = new Properties();
         
         Log.d("handleResponse","FileSize:"+_FileSize);
         
         if(_FileSize!=-1) {
            headers.put("Content-Length", String.valueOf(_FileSize));
            headers.put("Accept-Ranges", "bytes");
         }
         else
         headers.put("Accept-Ranges", "none");

         long sendCount;

         String status;
         if(range==null) {
            status = "200 OK";
            sendCount = _FileSize;
            Log.d("handleResponse","SendCount:"+sendCount);
         }else {
            if(!range.startsWith("bytes=")){
         	  Log.d("handleResponse","Error HTTP 416");
               sendError(_Socket, HTTP_416, null);
               return;
            }
            Log.d("Http","Range:" + range);
            
            range = range.substring(6);
            long startFrom = 0, endAt = -1;
            String[] minus=range.split("-");
            if (minus.length>0) {
         	   startFrom = Long.parseLong(minus[0]);
            } else
         	   startFrom = 0;
            
            if (minus.length>1)
         	   endAt = Long.parseLong(minus[1]);

            if( (_Del.getProtocol().equals("smb") && startFrom >= _FileSize) || (_Del.getProtocol().equals("http") && startFrom > 0)){
               sendError(_Socket, HTTP_416, null);
               Log.d("handleResponse","Error HTTP 416 b");
               return;
            }
            if (_FileSize > -1) {
	            if(endAt < 0)
	               endAt = _FileSize - 1;
	            
	            Log.d("Http","From:" + startFrom+ "To:"+endAt);
	            
	            sendCount = endAt - startFrom + 1;
	            if(sendCount < 0)
	               sendCount = 0;
	            if (_Del.getProtocol().equals("smb"))
            		_SmbStream.skip(startFrom);

            	status = "206 Partial Content";
            	String rangeSpec = "bytes " + startFrom + "-" + endAt + "/"+_FileSize;
	            headers.put("Content-Range", rangeSpec);
	
	            headers.put("Content-Length", "" + sendCount);
            }
            else {
            	status = "200 OK";
            	sendCount=-1;
            }
         }
         buf = new byte[100*1024];
         if (_Del.getProtocol().equals("smb")) {
        	 sendResponse(_Socket, status, _fileMimeType, headers, _SmbStream, sendCount, buf, null);
         }
         else if (_Del.getProtocol().equals("http")) {
        	 //buf = new byte[1000000];
        	 sendResponse(_Socket, status, _fileMimeType, headers, _Stream, sendCount, buf, null);
         }
         
         Log.d("Http","Http stream finished");
      }catch(Exception ioe){
         try{
            sendError(_Socket, HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            Log.d("handleResponse","SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
         }catch(Throwable t){
         }
      }
	   
	   CloseAll();
	   
   }

   @SuppressLint("DefaultLocale")
	private String decodeHeader(Socket socket, BufferedReader in, Properties pre) {
      try{
         // Read the request line
         String inLine = in.readLine();
         Log.d("decodeHeader","Header line:"+inLine);
         
         if(inLine == null) {
         	return "No Data";
         }
         StringTokenizer st = new StringTokenizer(inLine);
         if(!st.hasMoreTokens()) {
         	Log.d("decodeHeader","Syntax error");
         	return "Syntax error";
         }
         String method = st.nextToken();
         if(!method.equals("GET")) {
         	Log.d("decodeHeader","Not Get Method");
         	return "No get method";
         }
         
         if(!st.hasMoreTokens()) {
            Log.d("decodeHeader","Missing URI");
            return "Missing URI";
         }
        
         String URI = st.nextToken();

         URI=URI.substring(1);
         
         if (_Del.getProtocol().equals("smb")) {
        	 SmbFile MonFichier;
        	 if ( _SmbUser != null )
        		 MonFichier=new SmbFile("smb://"+Uri.decode(URI),new NtlmPasswordAuthentication(null, _SmbUser, _SmbPass));
        	 else
        		 MonFichier=new SmbFile("smb://"+Uri.decode(URI));
             if (MonFichier.isDirectory()) {
             	Log.d("decodeHeader","Directory not allowed");
                 return "401";
             }
             // 401
             if (!MonFichier.exists()) {
             	Log.d("decodeHeader","File Not Found");
                 return "404";
             }
	         _SmbStream = new SmbFileInputStream(MonFichier);
	         _FileSize= MonFichier.length();
	         _fileMimeType="video/x-"+getExtension(URI);
	         //_fileMimeType=MonFichier.getContentType();
         } else {
        	 URL address = new URL(_Del.getUrl()); 
        	 HttpURLConnection connection = (HttpURLConnection)address.openConnection();
        	 _Stream=connection.getInputStream();
        	 _fileMimeType=connection.getContentType();
        	 //_fileMimeType="Video/x-matroska";
        	 _FileSize=-1;
        	 //_FileSize=-1;
         }
         
         		
         while(true) {
            String line = in.readLine();
            
            if(line.equals(""))
              break;

            int p = line.indexOf(':');
            if(p<0)
               continue;
            final String atr = line.substring(0, p).trim().toLowerCase();
            final String val = line.substring(p + 1).trim();
            pre.put(atr, val);
         }
      }catch(Exception ioe){
         return "Internal ERROR";
      }
      return null;
  }

// Error
private void sendError(Socket socket, String status, String msg) throws InterruptedException{
   sendResponse(socket, status, "text/plain", null, null, 0, null, msg);
   CloseAll();
}

private void copyStream(InputStream in, BufferedOutputStream out, byte[] tmpBuf, long maxSize) {
	  Log.d("copyStream","Max Size:"+maxSize);
	  if (maxSize < 0) maxSize=999999999;
	  int count;
  while(maxSize>0 && (!_Del.Stoping)){
     count = (int)Math.min(maxSize, tmpBuf.length);
     try {
     	//Log.d("copyStream","Read");
			count = in.read(tmpBuf, 0, count);
		} catch (Exception e) {
			Log.d("copyStream","Error "+e.getMessage());
			return;
		}
     if(count<0) {
     	Log.d("copyStream","Finished");
        break;
     }
     try {
     	//Log.d("copyStream","Write");
			out.write(tmpBuf, 0, count);
		} catch (Exception e) {
			Log.d("copyStream","Error "+e.getMessage());
			return;
		}
     
     maxSize -= count;
     //Log.d("copyStream"," Count:"+count+" Maxsize:"+maxSize);
  } 
}
/**
 * Sends given response to the socket, and closes the socket.
 */
private void sendResponse(Socket socket, String status, String mimeType, Properties header, InputStream isInput, long sendCount, byte[] buf, String errMsg){
	   BufferedOutputStream out=null;
	   
	   try{
      out = new BufferedOutputStream(socket.getOutputStream());
      PrintWriter pw = new PrintWriter(out);

      {
         String retLine = "HTTP/1.1 " + status + " \r\n";
         Log.d("sendResponse", retLine);
         pw.print(retLine);
         out.flush();
      }
      if(mimeType!=null) {
         String mT = "Content-Type: " + mimeType + "\r\n";
         Log.d("sendResponse", mT);
         pw.print(mT);
         out.flush();
      }
      if(header != null){
         Enumeration<?> e = header.keys();
         while(e.hasMoreElements()){
            String key = (String)e.nextElement();
            String value = header.getProperty(key);
            String l = key + ": " + value + "\r\n";
            Log.d("sendResponse", l);
            pw.print(l);
            out.flush();
         }
      }
      pw.print("\r\n");
      pw.flush();
      if(isInput!=null) {
     	 Log.d("sendResponse", "copystream");
         copyStream(isInput, out, buf, sendCount);
      } else if(errMsg!=null) {
         pw.print(errMsg);
         Log.d("sendResponse", "Error:"+errMsg);
         pw.flush();
      }
      out.flush();
      out.close();
   }catch(Exception e){}
   
   try {
 	  if (out != null)
 		  out.close();
   } catch (Exception e) {}
}

// CleanUp
private void CloseAll() {
	   // Clear
	   try {
	   _Socket.close();
	   } catch (Exception e) {}
	   
	   try {
		   if (inS != null)
			   inS.close();
	   } catch (Exception e) {}
	   
	   try {
		   if (_SmbStream != null)
			   _SmbStream.close();
	   } catch (Exception e) {}
}

	private String getExtension(String s) {
	    String ext = "video";
	    int i = s.lastIndexOf('.');

	    if (i > 0 &&  i < s.length() - 1) {
	        ext = s.substring(i+1).toLowerCase();
	    }
	    return ext;
	}
}