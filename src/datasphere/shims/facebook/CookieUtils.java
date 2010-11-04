package datasphere.shims.facebook;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;


public class CookieUtils {

	private CookieUtils(){}

	/*
	 * 
	 */
	public static Map<String, String> fetch( Cookie[] cookies, String name ) {
		
		if ( cookies == null ) 
			return null;
	    
		Cookie cookie = null;		    
	    for ( Cookie c : cookies ) 
	    	if ( c.getName().equals( name ) )
	    		cookie = c;
	    
	    if ( cookie == null ) 
	    	return null;
	    
		Map< String, String > results = new HashMap< String, String >(); 
	  	String[] params = cookie.getValue().split( "&" );
    	for( String p : params ) {
	   		String[] parts = p.split( "=" );
	   		results.put( parts[ 0 ], parts[ 1 ] );
    	}
	    
	    return results;
	}
	  
}
