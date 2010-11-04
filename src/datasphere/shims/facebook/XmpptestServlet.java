package datasphere.shims.facebook;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

/**
 * Invitations can be sent - however what is received is merely the JID
 * Thus the datasphere (to automatically accept) needs to already know 
 * that the request is coming to send an immediate response. Hence a hack is
 * required that 
 * 
 * SendInvitation/SendMessage/GetPresence will throw an error for an invalid id,
 * but just say that the person is "offline" for a non-existent jid.
 * 
 * GAE accepts all invitations. It does not support IQ or presence stanzas.
 * However we can get at the raw XML of the incoming request.
 * There also seems no way in GAE to detect that an "accept" has arrived.
 *  
 * @author psxjog
 *
 */
@SuppressWarnings("serial")
public class XmpptestServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger ( 
			XmpptestServlet.class.getName() 
	);
	
		
	/**
	 * 
	 */
	public void doGet(
		HttpServletRequest request, 
		HttpServletResponse response )
	throws IOException {

		response.setContentType( "text/html" );
		PrintWriter out = response.getWriter();
		
		String action = request.getParameter( "action" );
		if ( action == null ) {
			write( out, action, false );
			return;
		}
		
		try {
			//-- harvest cookie information
		    Map< String, String > cookie = 
		    	CookieUtils.fetch( 
		    		request.getCookies(), 
		    		Facebook.getCookieName()
		    	);
		  
		    //-- obtain user information
		    PersistenceManager pm = PMF.get().getPersistenceManager();
		    FBUser user = pm.getObjectById( FBUser.class, cookie.get( "uid" ) );

		    JID jid = new JID( user.getJID() );
			 
		    //-- the first step in liking is to confirm (again) the
		    //-- existence of the datsphere.
			if ( action.equalsIgnoreCase( "detect" ) ) {
			
				XMPPService xmpp = XMPPServiceFactory.getXMPPService();
				if ( xmpp.getPresence( jid ).isAvailable() ) {
					user.setBefriendStatus( "PRESENT" );	
					write( out, action, true );
				} else {
					user.setBefriendStatus( "UNDETECTED" );	
					write( out, action, true );
				}
			}	
			
			//-- the second step is to make sure that a befriend request 
			//-- has been sent to the datasphere.
			else if ( action.equalsIgnoreCase( "invite" ) ) {
				XMPPService xmpp = XMPPServiceFactory.getXMPPService();
				xmpp.sendInvitation( jid );
				user.setBefriendStatus( "PENDING" );
				write( out, action, true );
			}
			
			//-- finally we need to see if the datasphere has accepted 
			//-- the befriend request. 
			else if ( action.equalsIgnoreCase( "confirm" ) ) {
				
				if ( user.getBefriendStatus().equals( "COMPLETE" ) )
					write( out, action, true );
				else 
					write( out, action, false );
			}
			else {
				write( out, action, false );
			}
			
			pm.close();
		}
		catch ( Exception e ) {
			write( out, action, false );
		}
	}
	
	
	private void write( PrintWriter out, String action, boolean result ) {
		out.write( 
			"{" +
				"\"action\":\"" + action + "\", " +
				"\"success\":" + result + 
			"}" );
		
	}


	@Override
	  public void doPost(
		HttpServletRequest req,
	    HttpServletResponse resp
	  ) throws IOException {
	    
	    // Parse incoming message
	    XMPPService xmpp = XMPPServiceFactory.getXMPPService();
	    Message msg = xmpp.parseMessage(req);
	    JID jid = msg.getFromJid();
	    String body = msg.getBody();
	    log.info(jid.getId() + " --> JEliza: " + body);
	    
	    /*
	    // Get a response from Eliza
	    HorizonBot parser = new HorizonBot();
	    String response = parser.handleLine( body );
	    log.info( jid.getId() + " <-- JEliza: " + response );
	    */
	    String response = msg.getStanza().toString();
	    
	    // Send out response
	    msg = new MessageBuilder()
	        .withRecipientJids( jid )
	        .withBody( response )
	        .build();
	    xmpp.sendMessage( msg );

	  }
		
	
}
