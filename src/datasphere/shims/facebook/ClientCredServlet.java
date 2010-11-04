package datasphere.shims.facebook;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ClientCredServlet extends HttpServlet {
	
public void doGet(
		HttpServletRequest req, 
		HttpServletResponse resp )
	throws IOException {
			
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		String result = Facebook.getURL( Facebook.credentialsURL() );
		Pattern p = Pattern.compile("^access_token=(.*)$");
		Matcher m = p.matcher( result );
		
		if ( m.find() ) {
        	String token = URLEncoder.encode( m.group(1), "UTF-8" );
    		out.println( "CLIENT_CREDENTIAL = " + token );
		}
		else {
			out.println( "No Client Credentials token obtained for application" );
		}
		
	}
}