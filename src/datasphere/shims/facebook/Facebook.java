package datasphere.shims.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class Facebook {
	
	private static final Logger log = Logger.getLogger( 
			Facebook.class.getName() 
	);
	
	private final static String oauth_uri = "https://graph.facebook.com/oauth/authorize";
	private final static String access_uri = "https://graph.facebook.com/oauth/access_token";
	private final static String redirect_uri = "http://data-chant.appspot.com/chant";
	private final static String client_id = "108909989170383";
	private final static String api_key = "8b6e90ac3cc3ce3e9ddffe48d2440b23";
    private final static String client_secret = "3302e249ab13dda194db0fbb7583e400";
    private final static String CCtoken = "108909989170383|bJcalFe1zgDXYMmKne4jOzrB5r4";
    public  final static String fqlURL = "https://api.facebook.com/method/fql.query";
    private final static String userScope = 
		"offline_access," +				
		"read_stream," +
		"user_about_me," +	
		"user_activities," +			
		"user_birthday," +			
		"user_events," +
		"user_groups," +
		"user_hometown," +				
		"user_interests," +
		"user_likes," +					
		"user_location," +				
		"user_notes," +
		"user_online_presence," +
		"user_photos," +
		"user_photo_video_tags," +
		"user_religion_politics," +		
		"user_status," +				
		"user_videos," +				
		"user_website," +				
		"user_work_history," +
		"user_education_history," +
		"email," +
		"read_requests," +
		"user_checkins";
    
    private final static String subscribeScope = 
    	"id," +				
		"first_name," +
		"last_name," +	
		"name," +			
		"link," +			
		"about," +
		"birthday," +
		"work," +				
		"education," +
		"website," +					
		"email," +				
		"hometown," +
		"location," +
		"bio," +
		"quotes," +				
		"gender," +
		"interested_in," +	
		"meeting_for," +			
		"religion," +			
		"political," +
		"likes," +
		"feed," +				
		"friends," +
		"activities," +					
		"interests," +				
		"music," +
		"books," +
		"movies," +
		"television," +
		"checkins," +		
		"picture," +				
		"photos," +				
		"statuses," +				
		"links," +
		"albums," +
		"videos," +
		"friends," +
		"posts";
    	
    public static SimpleDateFormat timefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    public static String getClientId() {
    	return client_id;
    }
   
    public static String getAPIKey() {
        return api_key;
    }

    public static String getUserScope() {
        return userScope;
    }
    
    public static String getSubscribeScope() {
        return subscribeScope;
    }
    
    
    public static String getAppSecret() {
        return client_secret;
    }
    
    public static String authorisationURL() {
        return oauth_uri + 
        	"?client_id=" + client_id + 
        	"&redirect_uri=" + redirect_uri +
        	"&scope=" + userScope +
        	"&display=page";
    }
    
    public static String credentialsURL() 
    throws UnsupportedEncodingException {

    	return access_uri + 
    		"?client_id=" + client_id + 
    		"&client_secret=" + client_secret +
    		"&type=client_cred";
    }
    
    
    public static String subscriptionURL() 
    throws UnsupportedEncodingException {

    	return 
    		"https://graph.facebook.com/" + client_id + "/subscriptions" + 
    		"?access_token=" + CCtoken;
    }

	public static String getCookieName() {
		return "fbs_" + Facebook.getClientId() ;
	}


	public static String getURL( String address ) 
	throws IOException {
		
		String line = "";
		String result = "";
		
		URL url = new URL( address );    
		BufferedReader br = new BufferedReader( new InputStreamReader( url.openStream() ) );
		while ( ( line = br.readLine() ) != null ) result += line;
		br.close();
		return result;
	}

	/**
	 * 
	 * @param address
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static String postURL(String  address, String data ) 
	throws IOException {
		
		String line = "";
		String result = "";
		
		URL url = new URL( address );
		URLConnection conn = url.openConnection();
		conn.setReadTimeout( 10000 );
		conn.setDoOutput( true );
		
		OutputStreamWriter osw = new OutputStreamWriter( conn.getOutputStream() ); 
		if ( data != null ) osw.write( data ); 
		osw.flush(); 
		
		BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ); 
		while ( ( line = br.readLine() ) != null ) result += line;

		osw.close(); 
		br.close();
		
		return result; 
	}
		
}
	
