//
// Multithreaded Java WebServer
// (C) 2001 Anders Gidenstam
// (based on a lab in Computer Networking: ..)
//
/*
	Axel Olsson, 9408142058
	Dennis Jönsson, 8706238337
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.InetAddress.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public final class WebServer
{
    public static void main(String argv[]) throws Exception
    {
    // Set port number
    int port = 0;
    
    // Establish the listening socket
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("Port number is: "+serverSocket.getLocalPort());

    
    // Wait for and process HTTP service requests
    while (true) {

        // Wait for TCP connection
        Socket requestSocket = serverSocket.accept();

        requestSocket.setSoLinger(true, 5);
        
        // Create an object to handle the request
        Request request  = new Request(requestSocket);
        
        //request.run()

        // Create a new thread for the request
        Thread thread = new Thread(request);

        // Start the thread
        thread.start();
    }
  }
}

final class Request implements Runnable
{
    // Constants
    //   Recognized HTTP methods
    final static class HTTP_METHOD
    {
    final static String GET  = "GET";
    final static String HEAD = "HEAD";
    final static String POST = "POST";
    }
	// contains all request, response and general headers
	final static class HEADER
	{
	 	final static String DATE  = "Date:";
	 	final static String SERVER  = "Server:";
	 	final static String CONTENT_TYPE = "Content-Type:";
		final static String LAST_MODIFIED = "Last-Modified:";
		final static String CONTENT_LENGTH = "Content-Length:";
		final static String CONTENT_ENCODING = "Content-Encoding:";
		final static String IF_MODIFIED_SINCE = "If-Modified-Since:";
		final static String ALLOW = "Allow:"; 
	}

	// lists the order of the response headers
	final static String [] RESPONSE_FIELDS = {
				HEADER.DATE,
				HEADER.SERVER,
				HEADER.ALLOW,
				HEADER.CONTENT_LENGTH,	
				HEADER.CONTENT_TYPE,
				HEADER.LAST_MODIFIED	
			};

	// contains all necessary response statuses
	final static class RESPONSE_STATUS
	{
	 	final static String OK = "200 OK";
		final static String NOT_MODIFIED = "304 Not Modified";
	 	final static String BAD_REQUEST  = "400 Bad Request";
	 	final static String NOT_FOUND = "404 Not Found";
		final static String INTERNAL_SERVER_ERROR = "500 Internal Server Error";
		final static String NOT_IMPLEMENTED = "501 Not Implemented";
		final static String VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported";
	}

    final static String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    final static String SERVER = "WebServer/1.0";
    final static String HTTPVERSION = "HTTP/1.0";
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public Request(Socket socket) throws Exception
    {
    this.socket = socket;
    }

    // Implements the run() method of the Runnable interface
    public void run()
    {
    try {
        processRequest();
    } catch (Exception e) {
        System.out.println(e);
    }
    }

    // Process a HTTP request
    
    private void processRequest() throws Exception
    {
        // Get the input and output streams of the socket.
    	InputStream ins       = socket.getInputStream();
   	DataOutputStream outs = new DataOutputStream(socket.getOutputStream());

   	 // Set up input stream filters
    	BufferedReader br	 = new BufferedReader(new InputStreamReader(ins));

	// create empty response 
	HttpResponse response = new HttpResponse();
	
	// get date
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat rfc822date =
        new SimpleDateFormat(DATE_FORMAT);
    	rfc822date.setTimeZone(TimeZone.getTimeZone("GMT"));

	// attach general-headers
	response.attachHeader(HEADER.DATE,rfc822date.format(cal.getTime()));
	
	// attach HttpResponse-Headers
	response.attachHeader(HEADER.SERVER,SERVER);

	String responseText = "";

	try{
		// read request line
		String request 	= br.readLine();

		// check if the request line is malformed
		boolean malFormed = !request.toUpperCase().
			matches("(GET|POST|HEAD) (/([A-Z]|[0-9])+)+.[A-Z]+ (HTTP/[0-9]{1}.[0-9]{1})");

	    	// collect all request headers in a hashmap 
		HashMap<String,String> requestHeaders = new HashMap<String,String>();

	    	String header = br.readLine();
		while( !header.equalsIgnoreCase("") && !malFormed){
			// check if the the request headers is malformed
			malFormed = !header.toUpperCase().matches("([a-zA-Z_0-9]|-|_)+: .*");
			if(!malFormed){
				requestHeaders.put(header.substring(0,header.indexOf(":")),
						   header.substring(header.indexOf(":"),header.length()).trim());
				header = br.readLine();
			}
		}

		// Reply with a bad request if the request is malformed.
		if(malFormed){
			responseText = response.build(RESPONSE_STATUS.BAD_REQUEST);
		}else{
		
			responseText = assembleResponse(response, request, requestHeaders);
		}


	}catch(Exception e){
		responseText = response.build(RESPONSE_STATUS.INTERNAL_SERVER_ERROR);
	}finally{
	    // write response and Close streams and socket
		outs.writeBytes(responseText);
		outs.close();
    		br.close();
    		socket.close();
	}

    }


	/* 
		creates a response from a given http request
		requires: request and headers are in valid format
		ensures a valid response given the request data
	 
	*/

	private String assembleResponse(HttpResponse response, String request, 
			HashMap<String,String> headers ) throws ParseException, IOException {
		
		// split up sections of the request line
		String [] req = request.split(" ");
		String method = req[0];
		String path = req[1].substring(1); // remove first occurance of '/'
		String version = req[2];
		
		// check version
		if(!version.equalsIgnoreCase(HTTPVERSION)){
			return response.build(RESPONSE_STATUS.VERSION_NOT_SUPPORTED);
		}

		//check method
		if(!method.toUpperCase().matches("GET|HEAD")){
			return response.build(RESPONSE_STATUS.NOT_IMPLEMENTED);
		}

		// check path
		File f = new File(path);
		if(!f.exists()){
			System.out.println("path: "+path+" does not exist");
			response.attachHeader(HEADER.CONTENT_LENGTH,"0");
			return response.build(RESPONSE_STATUS.NOT_FOUND);
		}

		/*
		  Entity-Header  = 
		      | Allow         		 ; Section 10.1
                      | Content-Encoding         ; Section 10.3
                      | Content-Length           ; Section 10.4
                      | Content-Type             ; Section 10.5
                      | Last-Modified            ; Section 10.10
		*/
		
		// attach entity-headers
		response.attachHeader(HEADER.ALLOW, "GET, HEAD");
		response.attachHeader(HEADER.CONTENT_LENGTH, f.length()+"");
		response.attachHeader(HEADER.CONTENT_TYPE, contentType(path));
		response.attachHeader(HEADER.LAST_MODIFIED, 
			new SimpleDateFormat(DATE_FORMAT).format(new Date(f.lastModified()))+"");
		

		 //check if modified since
		String since;
		if( (since = headers.get(HEADER.IF_MODIFIED_SINCE)) != null){
			for(String str : headers.values()){
			    System.out.println(str);
			}
			System.out.println(since);
			Date modSince = new SimpleDateFormat(DATE_FORMAT).parse(since);
			if(f.lastModified() - modSince.getTime() < 0){
				System.out.println("check if modified!");
				return response.build(RESPONSE_STATUS.NOT_MODIFIED);	
			}
		}

		// Attaches response body if it is a GET request
		if(method.equalsIgnoreCase(HTTP_METHOD.GET)){
			String res =  readFile(path);
			response.attachBody(res);
			response.attachHeader(HEADER.CONTENT_LENGTH, res.length()+"");
		}
		
		return response.build(RESPONSE_STATUS.OK);
	}

	// reads a file from a given path
	
	String readFile(String path) throws IOException{
		
		String line;
		BufferedReader br = new BufferedReader(new FileReader(path));
		String result = "";
		while ((line = br.readLine()) != null) {
			result += line;
		}
		br.close();
		
		return result;
	}
    
	/**
	*	Representation of a http response
	*/
	private class HttpResponse{
		// headers
		private HashMap<String,String> headers;

		// body
		//private String body = "";
		private String body = "";

		public HttpResponse(){
			headers = new HashMap<String,String>();
		}
		
		// builds a response from given status and headers. 
		public String build(String status){
			return HTTPVERSION+" "+status+CRLF+
			printHeaders()+
			CRLF+
			body;
		}
		
		// attach body to the response
		public void attachBody(String body){
			this.body = body;
		}
		
		// attach header to the response
		public void attachHeader(String header,String value){
			headers.put(header,value);
		}

		// remove header from the response
		public void removeHeader(String header){
			headers.remove(header);
		}
		
		// assembles attached headers in the right order 
		public String printHeaders(){
			String headerField = "";
			String val;
			for(int i = 0; i < RESPONSE_FIELDS.length; i ++){
				headerField +=
				((val = headers.get(RESPONSE_FIELDS[i])) != null) ? 
					RESPONSE_FIELDS[i]+" "+val+CRLF : "";
			}
			return headerField;
		}

	}

	// reads a file from a given path.
    private static void sendBytes(FileInputStream  fins,
                  OutputStream     outs) throws Exception
    {
	    // Coopy buffer
	    byte[] buffer = new byte[1024];
	    int    bytes = 0;

	    while ((bytes = fins.read(buffer)) != -1) {
		outs.write(buffer, 0, bytes);
	    }
    }

    private static String contentType(String fileName)
    {
    if (fileName.toLowerCase().endsWith(".htm") ||
        fileName.toLowerCase().endsWith(".html")) {
        return "text/html";
    } else if (fileName.toLowerCase().endsWith(".gif")) {
        return "image/gif";
    } else if (fileName.toLowerCase().endsWith(".jpg")) {
        return "image/jpeg";
    } else {
        return "application/octet-stream";
    }
    }
}

