package datasphere.shims.facebook;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;


/*
 * This is the Controller Servlet in the site's MVC.
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	
	public static String LOGIN_PAGE = "/login.jsp";
	public static String CONNECT_PAGE = "/connect.jsp";
	public static String LINKER_PAGE = "/linker.jsp";
	public static String REMIND_PAGE = "/remind.jsp";
	public static String ADMIN_PAGE = "/main.jsp";
	
	private static final Logger log = Logger.getLogger( 
			MainServlet.class.getName() 
	);
	
	/*
	 * 
	 */
	public void doGet(
		HttpServletRequest request, 
		HttpServletResponse response )
	throws IOException {

		response.setContentType( "text/html" );

		//-- harvest cookie information
	    Map< String, String > cookie = 
	    	CookieUtils.fetch( 
	    		request.getCookies(), 
	    		Facebook.getCookieName() );

		//-- If someone has just logged out destroy the previous session
		if ( request.getParameter( "logout" ) != null ) {
			request.getSession( true ).invalidate();
		}
		
	    //-- authentication check
	    if ( cookie == null ) {
	    	response.sendRedirect( LOGIN_PAGE );
	    } else {
			//-- and pass on the request for processing
	    	processRequest( request, response, cookie );
	    }
	}

	
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param cookie
	 * @throws IOException
	 */	 
	private void processRequest(		
		HttpServletRequest request, 
		HttpServletResponse response, 
		Map< String, String > cookie ) 
	throws IOException {

		//-- Are we being asked to make a datasphere connection attempt?
		boolean connect = ( request.getParameter( "connect" ) != null ) ? true : false;
		boolean link = ( request.getParameter( "link" ) != null ) ? true : false;
		
		//-- Have we been delivered a catalog address? If so store it in the session..
	    HttpSession session = request.getSession( true );
		if ( request.getParameter( "jid" ) != null ) 
        	session.setAttribute( "jid", request.getParameter( "jid" ) );
	    
	    //-- grab the user details
	    PersistenceManager pm = PMF.get().getPersistenceManager();
	    FBUser user = null;

	    //-- load the user details class, or create it if it doesn't exist
	    try {
	    	user = pm.getObjectById( FBUser.class,  cookie.get( "uid" ) );
	    }
	    catch ( JDOObjectNotFoundException e ) {
	    	String access_token = cookie.get( "access_token" );
			user = new FBUser(  cookie.get( "uid" ), access_token );
	        user.setValidPermissions( true );
	        user.setJoined( System.currentTimeMillis() );
        	pm.makePersistent( user );
        	//-- because this is the first time we have seen the user
        	//-- we will also try and connect them.
        	connect = true;
	    }
	   
    	log.warning( "LAST CHECKIN = " + user.getLastCheckin() ); 
    	
	    //-- if the user has just logged in, grab their facebook details -
	    //-- if we can't do that we have a problem and skip everything else.
	    try {
	    	if ( session.getAttribute( "name" ) == null ) 
	    		loadFBDetails( session, user );
	    }
	    catch ( JSONException e ) {
		    pm.close();
		   	log.warning( "COULDN'T GET FB DETAILS" );
		   	return;
		}

	    String status = user.getBefriendStatus();
	    
		//-- finally check to see if we have connected to the datasphere.
		if ( connect && !status.equals( "COMPLETE" ) )
			response.sendRedirect( CONNECT_PAGE );
		
	    //-- if we have we need to link them fully
	    else if ( link && !status.equals( "COMPLETE" ) ) {

			//-- do we have new JID in the session? If so try and use it...
			String jid = (String) session.getAttribute( "jid" );	
			if ( user.hasNewJID( jid ) ) 
				validateJID( user, jid, session, response );
			
			//-- if we don't have one stored we need to grab one
			else if ( user.getJID() == null ) 
				response.sendRedirect( CONNECT_PAGE + "?cause=missing" );

			//-- but if we do then lets try using it again...
			else 
				validateJID( user, user.getJID().toString(), session, response );
    		
	    }
		else { 
			response.sendRedirect( ADMIN_PAGE );
		}
		
		pm.close();
    }


	
	/**
	 * 
	 * @param user
	 * @param jid
	 * @param session
	 * @param response
	 * @throws IOException
	 */
	private void validateJID(
			FBUser user, 
			String jid,
			HttpSession session,
			HttpServletResponse response
	) throws IOException {
		
		try {
			user.setJID( jid );
        	session.removeAttribute( "jid" );
        	
        	XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        	if ( xmpp.getPresence( new JID( jid ) ).isAvailable() ) {
        		
        		//-- the user does exist so its time to try and link
        		//-- to them. This basically will involve us waiting
        		//-- for them to respond.
        		user.setBefriendStatus( "PRESENT" );
        		response.sendRedirect( LINKER_PAGE );
        	}
        	else {
        		//-- the supplied JID did not show as online. We have to
        		//-- take this to assume that we cannot connect given
        		//-- the limitations of GAE's XMPP interface
        		user.setBefriendStatus( "FAILED" );
        		response.sendRedirect( CONNECT_PAGE + "?cause=failed" );
            }
			
		} catch ( Exception e ) {
			//-- the supplied jid is invalid. Get another...
			response.sendRedirect( CONNECT_PAGE + "?cause=invalid" );
		}
		
	}



	/**
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws Exception 
	 * 
	 */
	private void loadFBDetails( HttpSession session, FBUser user ) 
	throws JSONException, IOException 
	{
		
		JSONObject bio = new JSONObject( Facebook.getURL( user.meURL() ) );
		
		if ( !bio.has( "name" ) ) {
			throw new JSONException( "Can't get bio. Probably an access token error.");
		}
		else {
			session.setAttribute( "name", bio.get( "name" ) );
			session.setAttribute( "link", bio.get( "link" ) );
			session.setAttribute( "email", bio.get( "email" ) );
		}
			
	}
	
}