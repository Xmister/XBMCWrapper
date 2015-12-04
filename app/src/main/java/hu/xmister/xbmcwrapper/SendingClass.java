package hu.xmister.xbmcwrapper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.StringTokenizer;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;


/**
 * Every HTTP connection has an instance of this Thread.
 */
public class SendingClass extends Thread {

    /**
     * Helper class for better Exception management
     */
    private class MyException extends Exception {
        public String code;
        public MyException(String code, String message) {
            super(message);
            this.code=code;
        }
    };

    //Owner class to reach some fields
    private StreamOverHttp httpStream;
    private String
            _SmbUser=null,
            _SmbPass=null;

    private static final String
            HTTP_400 = "400 Bad Request",
            HTTP_416 = "416 Range not satisfiable",
            HTTP_500 = "500 Internal Server Error",
            HTTP_404 = "404 Not Found",
            HTTP_403 = "403 Not Allowed";

    private Socket _Socket;

    public SendingClass(Socket sock, StreamOverHttp hS) {
        _Socket=sock;
        httpStream =hS;
    }
    public SendingClass(Socket sock, StreamOverHttp hS, String username, String password) {
        _Socket=sock;
        httpStream =hS;
        _SmbUser=username;
        _SmbPass=password;
    }

    /**
     * Starting the thread with StartReceive().
     */
    @Override
    public void run() {
        StartReceive();
    }

    /**
     * Reads the client REQUEST and decides of the answer.
     */
    private void StartReceive() {
        try {
            InputStream inS = _Socket.getInputStream();

            if (inS == null) {
                Log.d("handleResponse", "Null Stream");
                return;
            }
            Properties pre = new Properties();
            Log.d("handleResponse", "Decode header");

            decodeHeader(_Socket, new BufferedReader(new InputStreamReader(inS)), pre);

            inS.close();
        }

        catch(Exception e){
            String code=HTTP_500;
            if ( e instanceof MyException ) {
                code=((MyException) e).code;
            }
            try{
                sendError(_Socket, code, e.getMessage());
                Log.d("handleResponse", "SERVER INTERNAL ERROR:  " + e.getMessage());
            }catch(Exception ee){
            }
        }

        try {
            _Socket.close();
        } catch (Exception e) {}

    }


    /**
     * Sends a Samba file through the stream
     * @param pre contains client headers
     * @param headers contains response headers so far
     * @param fileSize the file size
     * @param fileMimeType the file type
     * @param smbStream the input file stream
     * @throws MyException
     * @throws IOException
     */
    private void sendFile( Properties pre, Queue<String> headers, long fileSize, String fileMimeType, SmbFileInputStream smbStream) throws MyException,IOException {
        String range = pre.getProperty("range");

        Log.d("handleResponse","FileSize:"+fileSize);

        long sendCount=0;

        String status = "500";
        if(range==null) {
            status = "200 OK";
            headers.add("Content-Type: " + fileMimeType);
            headers.add("Content-Length: " + fileSize);
            headers.add("Accept-Ranges: bytes");
            sendCount = fileSize;
            Log.d("handleResponse", "SendCount:" + sendCount);
        }
        else {
            if(!range.startsWith("bytes=")){
                Log.d("handleResponse", "Wrong range specification");
                throw new MyException(HTTP_400,"Wrong range specification");
            }
            Log.d("Http","Range:" + range);

            //Parse the reqested range
            range = range.substring(6);
            long startFrom = 0, endAt = -1;
            String[] minus=range.split("-");
            if (minus.length>0) {
                startFrom = Long.parseLong(minus[0]);
            } else
                startFrom = 0;

            if (minus.length>1)
                endAt = Long.parseLong(minus[1]);

            if( startFrom >= fileSize )
                throw new MyException(HTTP_416,"Content out of range");

            if(endAt < 0)
                endAt = fileSize-1;

            Log.d("Http","From:" + startFrom+ "To:"+endAt);

            sendCount = endAt - startFrom + 1;
            if(sendCount < 0) {
                sendCount = 0;
                throw new MyException(HTTP_416, "Content out of range");
            }

            smbStream.skip(startFrom);

            status = "206 Partial Content";
            headers.add("Content-Type: " + fileMimeType);
            headers.add("Content-Length: " + sendCount);
            headers.add("Accept-Ranges: bytes");
            String rangeSpec = "bytes " + startFrom + "-" + endAt + "/" + fileSize;
            headers.add("Content-Range: " + rangeSpec);
        }

        //1MB buffer
        byte[] buf = new byte[1*1024*1024];
        sendResponse(_Socket, status, headers, smbStream, sendCount, buf, null);

        try {
            smbStream.close();
        } catch (Exception e) {
        }

        Log.d("Http", "Http stream finished");

    }

    /**
     * Sends a HTTP stream through our stream
     * @param pre contains client headers
     * @param headers contains response headers so far
     * @param fileMimeType the file type
     * @param stream the input stream
     * @throws MyException
     */
    private void sendHTTP( Properties pre, Queue<String> headers, String fileMimeType, InputStream stream) throws MyException {
        String range = pre.getProperty("range");

        Log.d("handleResponse","HTTP Stream");

        String status="500";
        if(range==null) {
            status = "200 OK";
            headers.add("Content-Type: " + fileMimeType);
            headers.add("Accept-Ranges: none");
            Log.d("handleResponse","No range");
        }
        else {
            if(!range.startsWith("bytes=")){
                Log.d("handleResponse", "Wrong range specification");
                throw new MyException(HTTP_400,"Wrong range specification");
            }
            Log.d("Http","Range:" + range);

            //Parse the reqested range
            range = range.substring(6);
            long startFrom = 0, endAt = -1;
            String[] minus=range.split("-");
            if (minus.length>0) {
                startFrom = Long.parseLong(minus[0]);
            } else
                startFrom = 0;

            if (minus.length>1)
                endAt = Long.parseLong(minus[1]);

            //Range is not really supported on Re-Stream
            if( startFrom > 0 )
                throw new MyException(HTTP_416,"Content out of range");

            //We said we don't accept ranges, and yet we got a request. Just act like nothing happened.
            status = "200 OK";
            headers.add("Accept-Ranges: none");
        }

        //1MB buffer
        byte[] buf = new byte[1*1024*1024];
        sendResponse(_Socket, status, headers, stream, -1, buf, null);

        try {
            stream.close();
        } catch (Exception e) {}

        Log.d("Http", "Http stream finished");

    }

    /**
     * Parses the client's requests. Can only handle GET requests.
     * @param socket the socket of the client connection
     * @param in BufferedReader of the input stream
     * @param pre the parsed headers sent by the client
     * @return error message or null.
     */
    @SuppressLint("DefaultLocale")
    private void decodeHeader(Socket socket, BufferedReader in, Properties pre) throws MyException, IOException {
        // Read the request line
        String inLine = in.readLine();
        Log.d("decodeHeader","Header line:"+inLine);

        if(inLine == null) {
            throw new MyException(HTTP_400,"Null input");
        }
        StringTokenizer st = new StringTokenizer(inLine);
        if(!st.hasMoreTokens()) {
            Log.d("decodeHeader","Protocol error");
            throw new MyException(HTTP_400,"Protocol error");
        }
        String method = st.nextToken();

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

        switch ( method ) {
            case "GET":
                getRequest(st,pre);
                break;
            default:
                throw new MyException(HTTP_400,"Unsupported request");
        }
    }

    /**
     * Handle GET Requests, decides what to send
     * @param st the other items of the request
     * @param pre the other headers sent by the client
     * @throws MyException
     * @throws IOException
     */
    private void getRequest(StringTokenizer st, Properties pre) throws MyException, IOException {
        if (!st.hasMoreTokens()) {
            Log.d("decodeHeader", "Missing URI");
            throw new MyException(HTTP_400,"Missing URI");
        }

        String URI = st.nextToken();

        URI = URI.substring(1);

        //FIFO List, so we can control the sequence
        Queue<String> headers=new LinkedList<String>();

        if (httpStream.getProtocol().equals("smb")) {
            //Stream Samba file
            SmbFile MainFile;
            if (_SmbUser != null)
                MainFile = new SmbFile("smb://" + Uri.decode(URI), new NtlmPasswordAuthentication(null, _SmbUser, _SmbPass));
            else
                MainFile = new SmbFile("smb://" + Uri.decode(URI));
            if (MainFile.isDirectory()) {
                Log.d("decodeHeader", "Directory not allowed");
                throw new MyException(HTTP_403,"Directory listing not allowed");
            }
            // 401
            if (!MainFile.exists()) {
                Log.d("decodeHeader", "File Not Found");
                throw new MyException(HTTP_404,"File not found");
            }
            SmbFileInputStream smbStream = new SmbFileInputStream(MainFile);
            long fileSize = MainFile.length();
            String fileMimeType = "video/x-" + getExtension(URI);
            sendFile(pre,headers,fileSize,fileMimeType,smbStream);
        } else {
            //Re-Stream web URL
            URL address = new URL(httpStream.getUrl());
            HttpURLConnection connection = (HttpURLConnection) address.openConnection();
            InputStream stream = connection.getInputStream();
            String fileMimeType;
            if (httpStream.getProtocol().equals("pvr")) {
                fileMimeType = "video/x-mpegts";
                Log.d("decodeHeader", "Overriding mime type to: " + fileMimeType);
            } else {
                fileMimeType = connection.getContentType();
            }
            sendHTTP(pre,headers,fileMimeType,stream);
        }
    }

    /**
     * Send an error message to the client, then closes the connection.
     * @param socket the socket of the client connection
     * @param status status code to send
     * @param msg the message
     * @throws InterruptedException
     */
    private void sendError(Socket socket, String status, String msg) throws InterruptedException{
        sendResponse(socket, status, null, null, 0, null, msg);
        try {
            _Socket.close();
        } catch (Exception e) {};
    }

    /**
     * Copies the given input stream to the given output stream
     * @param in the InputStream
     * @param out the BufferedOutputStream
     * @param tmpBuf the used buffer, parameter so it's size can be defined outside
     * @param maxSize how many bytes to copy
     */
    private void copyStream(InputStream in, BufferedOutputStream out, byte[] tmpBuf, long maxSize) {
        Log.d("copyStream", "Max Size:" + maxSize);
        int count;
        if ( maxSize < 0 ) {
            //Continous stream
            do {
                try {
                    //Log.d("copyStream","Read");
                    count = in.read(tmpBuf, 0, tmpBuf.length);
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

                //Log.d("copyStream"," Count:"+count+" Maxsize:"+maxSize);
            } while(count > 0 && (!httpStream.Stopping));
        }
        else {
            //File stream
            while( maxSize > 0 && (!httpStream.Stopping)){
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

    }

    /**
     * Sends given response to the socket, and closes the socket.
     * @param socket the socket of the client connection
     * @param status the status code
     * @param headers headers to send
     * @param isInput the input stream if we want to copy that, otherwise null
     * @param sendCount the byte count to send
     * @param buf the buffer, parameter so it's size can be defined outside
     * @param errMsg if we want to send an error message
     */
    private void sendResponse(Socket socket, String status, Queue<String> headers, InputStream isInput, long sendCount, byte[] buf, String errMsg){
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
            if(headers != null){
                for (String header: headers){
                    Log.d("sendResponse", header);
                    pw.print(header);
                    pw.print("\r\n");
                    pw.flush();
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


    /**
     * Gets the extension of a file
     * @param s the file name
     * @return the extension
     */
    private String getExtension(String s) {
        String ext = "video";
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}