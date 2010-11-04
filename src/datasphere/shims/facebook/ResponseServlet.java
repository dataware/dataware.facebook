package datasphere.shims.facebook;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datanucleus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookException;
import com.restfb.types.User;

import datasphere.dataware.DSUpdate;
import datasphere.shims.facebook.entities.FBCheckin;
import datasphere.shims.facebook.entities.FBCheckinSet;
import datasphere.shims.facebook.entities.FBObject;
import datasphere.shims.facebook.entities.FBObjectSet;
import datasphere.shims.facebook.entities.FBPost;
import datasphere.shims.facebook.entities.FBPostSet;

@SuppressWarnings("serial")
public class ResponseServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger( 
			ResponseServlet.class.getName() 
	);
	
	//-- fields used to store info to reduce FB api calls
	private JSONObject bio = null;
	private int bioSize;
	private int bioNoElements;
	private FBObjectSet likes = null;
	private FBCheckinSet checkins = null;
	private FBPostSet posts = null;
	private Set< String > processedFields = null;
	
	
	/**
	 * 
	 */
	public void doGet(
		HttpServletRequest req, 
		HttpServletResponse resp )
	throws IOException {

		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		String sub = req.getParameter( "subscribe" );
		if  ( sub != null  ) {
			
			String data = "object=user" +
			  "&callback_url=http://data-chant.appspot.com/response" +
			  "&verify_token=hubba" +
			  "&fields=" + URLEncoder.encode( Facebook.getSubscribeScope(), "UTF-8" );
	       	String result = Facebook.postURL( Facebook.subscriptionURL(), data );
			out.println( "result = (" + result + ")" );
		}
		else {	
			log.warning( "Facebook is checking the subscription callback for (" 
				+ req.getParameter( "hub.challenge" ) + ")" );
			out.println( req.getParameter( "hub.challenge" ) );
		}
	}
	
	
	
	/**
	 *
	 */
	public void doPost(	HttpServletRequest req, HttpServletResponse resp )
	throws IOException {
		
		String receivedUpdate = "";
		processedFields = new HashSet< String >();
		try {	
			
			//-- aggregate the JSON real-time update into a single string
			BufferedReader br = req.getReader();
			String line = "";
			while ( ( line = br.readLine() ) != null ) receivedUpdate += line;
			
			//-- break it apart into a sorted set of relvent, individual updates
			FBUpdateSet parsedUpdate = new FBUpdateSet( receivedUpdate );
			PersistenceManager pm = PMF.get().getPersistenceManager();
			log.warning( "RECVD: " + receivedUpdate );
			log.warning( "UPDATE-SET: " + parsedUpdate );
					
			//-- process each individual update message (which has a 
			//-- distinct user/time key to a set of fields.
			for ( FBUpdate entry : parsedUpdate ) { 
				
				//-- locate the user whose update it is.
				//-- TODO: this is currently inefficient
				FBUser user = FBUser.fetch( pm, entry.getUid() );
				
				//-- process each facebook field that has been changed
				for ( String field : entry.getFields() ) 
					processField( user, field, entry.getDate().getTime() );
				
				//-- and register that the time of this latest udpate,
				//-- according to its timestamp. There is a good chance
				//-- facebook will send it again...
				user.setLastUpdated(  entry.getDate().getTime() ); 
			}
			
			pm.close();
			
		} catch( JSONException e ) {
			log.severe( "JSON EXCEPTION: " + receivedUpdate );
		}
		catch ( JDOObjectNotFoundException e )  {
			log.severe( "DATASTORE EXCEPTION: " + receivedUpdate );
		}
		finally {
			likes = null;
			bio = null;
			checkins = null;
			posts = null;
			processedFields = null;
		}
	}
		
	

	/**
	 * @param pm 
	 * @throws IOException 
	 **/
	private void processField( FBUser user, String field, long time )
	throws IOException {
		
		try {
			if ( field.equals( "likes" ) ) 
				processLikesUpdate( user, time );
			
			else if ( FBUpdate.matches( field, FBUpdate.SOCIAL ) ) 
				processSocialUpdate( user, field, time );
			
			else if ( FBUpdate.matches( field, FBUpdate.BIO ) )
				processBioUpdate( user, field, time );
			
			else if ( FBUpdate.matches( field, FBUpdate.GEO ) )
				processGeoUpdate( user, field, time );
			
			else if ( FBUpdate.matches( field, FBUpdate.FEED ) )
				processFeedUpdate( user, field, time );
			
			else if ( FBUpdate.matches( field, FBUpdate.PIC ) ) 
				processPicUpdate( user, field, time );
				
			else {
			
				DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:update" )
					.setMtime( time )
					.setDesc( "An unrecognized \"" + field + "\"update has been received from facebook." )
					.addTag( "ds:unrecognized" );
				
				AppLog.log( time, "FB-UPDATE", d.getDesc(), user.getUid() );
				send( user.getJID(), d.toString() );
			}

		} catch ( ParseException e ) {
			log.warning( "PARSE EXCEPTION : [" + e.getMessage() + "] " + e.toString() );
			throw new IOException( e );
			
		} catch ( JSONException e ) {
			log.warning( "JSON EXCEPTION : [" + e.getMessage() + "] " + e.toString() );
			throw new IOException( e );
		}
	
		processedFields.add( field );
	}
	
	
	
	/**
	 * It seems impossible to get information about /which/ new friend has been
	 * added by the FB API. Connections hold no timestamp, and friends hold no
	 * creation_time. This would only leave storing the friends list and comparing
	 * which is forbidden by Facebook terms and conditions. As such the best we 
	 * can do is point out that a new friend has been established, and supply the
	 * total no. of friends that the user now has.
	 * @param user
	 * @param field
	 * @param date 
	 **/
	private void processSocialUpdate( FBUser user, String field, long time )
	throws IOException {
		try {
			FacebookClient facebookClient = 
				new DefaultFacebookClient( user.getUAtoken() );
			
			Connection< User > myFriends = 
				facebookClient.fetchConnection( user.getUid() + "/friends", User.class );

			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:social" )
				.setMtime( time )
				.setDesc( "Facebook friend-list update. You now have " + myFriends.getData().size() + "friends." )
				.addTag( "ds:social" )
				.setTotal( myFriends.getData().size() );
			AppLog.log( time, "FB-UPDATE [SOCIAL]", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
			
			user.setLastSocial( time );
			user.incrementNoSocial();
		}
		catch ( FacebookException e ){ 
			log.severe( "FBUPDATE - REST FB EXCEPTION [COULDN'T PARSE UPDATE]:");
		}
	}
	
	
	
	/**
	 * All of the items that can be liked (and detected, because actions such as
	 * liking someone else's comment/picture cannot) are stored in the same 
	 * format by facebook (represent by FBObject here). Any addition or removal
	 * in Activities, Interests, Music, Books, Movies, Television, etc. are picked
	 * up here. If you delete a like then an update is sent for ALL categories.
	 * However if you add a like only an update for the relevent category is sent.
	 * Genius facebook.
	 * @param user
	 * @param date 
	 * @throws IOException 
	 * @throws ParseException 
	 **/
	private void processLikesUpdate( FBUser user, long time ) 
	throws JSONException, IOException, ParseException {
		
		//-- have we already checked for likes in this updateList? If
		//-- so then just head back, as any updates will have already
		//-- been collected.
		if ( likes != null ) return;
		
		//-- is the time of this update before the last new "like" that 
		//-- we detected? If so we have already covered the content 
		//-- of this update and so can discard it
		if ( time <= user.getLastLike() ) return;
		
		//-- TODO: PREVIOUS ACCESS or LAST LIKE!?
		
		//-- if we have got here we need to find out what has happened 
		//-- since we last checked the like lists.
		likes = new FBObjectSet( 
			Facebook.getURL( user.dataURL( "likes" ) )
		);

		long lastLikeTime = user.getLastLike();
		long mostRecentTime = 0;
		
		Map< String, Integer > sums = 
			new HashMap< String, Integer >();
		
		Map< String, Set< FBObject > > hits = 
			new HashMap< String, Set< FBObject > >();
		
		//-- now we need to scan through the objects looking for
		//-- the new ones (i.e. since we last checked).
		for ( FBObject o : likes ) {
			
			//-- is this object a new one?
			long itemCreated = o.getCreatedTime().getTime();
			String cat = o.getCategory();
			
			if ( itemCreated > lastLikeTime ) {
				
				//-- have we seen this category before
				if ( !hits.containsKey( cat ) ) {
					Set< FBObject > s = new HashSet< FBObject >();
					s.add( o );
					hits.put( cat, s );
				}
				else 
					hits.get( cat ).add( o );
				
				//-- and is the latest item we have seen
				if ( itemCreated > mostRecentTime ) 
					mostRecentTime = itemCreated;
			}
			
			//-- keep a running total of category matches
			int c = sums.containsKey( cat ) ? sums.get( cat ) : 0;
			sums.put( cat, c + 1 );
		}
		
		//-- if there are no updates then we must have a deletion. There is
		//-- no way of detecting what category this is in so we must return 
		//-- a generic message, and the lastLike time remains unchanged.
		if ( hits.size() == 0 ) {
			String msg = 
				"One or more Facebook likes removed." +
				" There are " + likes.size() + " left.";
			
			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:like", "delete" )
				.setMtime( time )
				.setDesc( msg )
				.setTotal( likes.size() );
			AppLog.log( time, "FB-UPDATE", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
			
		}
		//-- otherwise we have fresh likes and need to process them.
		else {
			
			user.setLastLike( mostRecentTime );
			for( Map.Entry< String, Set< FBObject > > e : hits.entrySet() ) {
				
				String selection = StringUtils.collectionToString( e.getValue() ); 
				String totalLikes = Integer.toString( sums.get ( e.getKey() ) ); 
				String msg = 
					e.getValue().size() + " Facebook likes added: \"" + selection +
					"\". You now have " + totalLikes + " " 
					+ e.getKey() + " items in" +
					" your \"" + likes.size() + "\" likes.";
				
				DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:like", "create" )
					.setMtime( time )
					.setDesc( msg )
					.setTotal( likes.size() )
					.addMetadata( "field", e.getKey() )
					.addMetadata( "fieldTotal", totalLikes )
					.addMetadata( "likesTotal", Integer.toString( likes.size() ) )
					.addMetadata( "selection", selection );
				
				AppLog.log( time, "FB-UPDATE [LIKES]", d.getDesc(), user.getUid() );
				send( user.getJID(), d.toString() );
			}
		}
		
		user.setLastLike( time );
		user.incrementNoLike();
	}
	
	
	
	/**
	 * There are a lot of changes that can be made to the user object in Facebook.
	 * These are collated here as they can all be obtained from obtained from the 
	 * graph api via /me. The bio handles: location, hometown, birthday, quotes, 
	 * about (bio), religion, website, email, link, work & education. 
	 * 
	 * Items in the FB user object but which do not produce updates are:  
	 * gender, politics, relationship_status, IM, verified, significant_other, 
	 * timezone, mobiles, address, contact information.
	 * 
	 * Looking_for & interested_in do produce an update but only indicate "feed",
	 * so are almost impossible to detect given a "feed" graph API query only
	 * actually returns "posts at the moemnt.
	 * 
	 * @param user
	 * @param field
	 * @param date
	 * @throws JSONException
	 * @throws IOException
	 */
	private void processBioUpdate( FBUser user, String field, long time ) 
	throws JSONException, IOException {
		
		if ( bio == null ){
			String bioString = Facebook.getURL( user.meURL() );
			bio = new JSONObject( bioString );
			bioSize = bioString.getBytes("UTF-8").length;
			bioNoElements = bio.names().length();
		}
		
		//-- if we have already processed this bio field there is no
		//-- point in doing it again. We already have the latest data
		if ( processedFields.contains( field ) ) return;

		//-- convert "about" to "bio". Annoying fix required
		//-- to adjust between the terminology the real time
		//-- api and graph api use.
		field = ( field.equals( "about" ) ) ? "bio": field;
		
		DSUpdate d = null;
		
		String result = "In your Facebook bio, your ";
		if ( bio.has( field ) ) {
			
			d = new DSUpdate( "ds:facebook", "ds:fb:bio", "create" );
			
			//-- check if Timezone information has changed
			if ( field.equals( "timezone" ) ) 
				result += "\"timezone\" is now " + bio.getInt( "timezone" );
	
			//-- check if locational information has changed
			else if ( field.equals( "hometown" ) ||
					  field.equals( "location" ) ) {
	
				JSONObject place = bio.getJSONObject( field ); 
				String id = place.getString( "id" ); 
				String name = place.getString( "name" );
				result += "\"" + field +	"\" has changed to " + name + " (" + id + ")";
			}
			
			//-- check if employment or education info has changed
			else if ( field.equals( "work" ) || field.equals( "education" ) ) {
	
				int count = ( bio.has( field ) ) ? bio.getJSONArray( field ).length() : 0;
				d.setTotal( count );			
				result += 
					"\"" + field + "\" details have " +	"changed. " +
					"You now have " + count + " \"" + field + "\" records.";
			}
			
			//-- otherwise we are left with general updates.
			else {
				String v = bio.getString( field );
				d.setTotal( v.length() );
				v = ( v.length() > 300 ) ? v.substring( 0, 300 ) : v;
				result +=  "\"" + field +	"\" has changed to \"" + v + "\"";
			}
		}
		else {
			result += "\"" + field + "\" has been removed.";
			d = new DSUpdate( "ds:facebook", "ds:fb:bio", "delete" );
		}
		
		user.setLastBio( time );
		user.incrementNoBio();

		//-- and finally log the changes
		d.addMetadata( "field", field )
			.setMtime( time )
			.addTag( "ds:bio" )
			.setDesc( result )
			.setTotal( 20 )
			.addMetadata( "size", bioSize );
			
		AppLog.log( time, "FB-UPDATE [BIO]",  d.getDesc(), user.getUid() );
		send( user.getJID(), d.toString() );
	}
	
	
	
	/**
	 * Feed and Posts Graph API calls generate the same response - this means 
	 * that the only detectable items which generate a "feed" udpate are:
	 * photo, status, checkin and link. However, it could also be caused by a 
	 * "like" (of a post), a language change, an event attendence, adding a 
	 * family member or partner...
	 * @throws IOException 
	 * @throws JSONException 
	 * 
	 **/
	private void processFeedUpdate( FBUser user, String field, long time )
	throws JSONException, IOException {
	
		//-- have we already queried for checkins in this updateList? If
		//-- so then just head back, as any updates will have already
		//-- been collected.
		if ( posts != null ) return;
		
		//-- is the time of this update before the last new "checkin" that 
		//-- we detected? If so we have already covered the content 
		//-- of this update and so can discard it
		if ( time <= user.getLastPost() ) return;
		
		//-- if we have got here we need to find out what has happened 
		//-- since we last checked the like lists.
		posts = new FBPostSet( 
			Facebook.getURL( user.dataURL( "posts", user.getLastPost() )	)
		);

		//-- now we need to scan through the objects looking for
		//-- the new ones (i.e. since we last checked).
		if ( posts.size() > 0 ) {
			
			long mostRecentTime = 0; 
			String msg = "";
			
			for ( FBPost p : posts ) {
				
				msg = "New Facebook \"" + p.getType() + 
					"\" post: \"" + p.getMessage() + "\""; 	
				
				long itemCreated = p.getCreatedTime().getTime();
				
				//-- and is this the latest post we have seen?
				if ( itemCreated > mostRecentTime ) 
					mostRecentTime = itemCreated;
			
				DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:post", "create" )
					.setMtime( time )
					.setDesc( msg )
					.addTag( "ds:communication" )
					.addMetadata( "message", p.getMessage() )
					.addMetadata( "category", p.getType() );				
				AppLog.log( time, "FB-UPDATE [FEED]", d.getDesc(), user.getUid() );
				send( user.getJID(), d.toString() );
				user.incrementNoPost();
			}
			
			user.setLastPost( mostRecentTime );
		}
		else {
			String msg = 
				"An unknown item has been added to your feed - probably a comment " +
				"(we can't tell until facebook fix their api).";
			
			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:feed", "create" )
				.setMtime( time )
				.setDesc( msg );
			AppLog.log( time, "FB-UPDATE [FEED]", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
			user.setLastFeed( time );
			user.incrementNoFeed();
		}
	}

	
	/**
	 * @throws IOException 
	 * @throws JSONException 
	 * WHAT HAPPENS IF:
	 * - YOU DELETE A CHECKIN
	 * - SOMEONE ELSE CHECKS YOU IN
	 * - YOU REMOVE A TAG?
	 **/
	private void processGeoUpdate( FBUser user, String field, long time ) 
	throws IOException, JSONException {
		
		//-- have we already queired for checkins in this updateList? If
		//-- so then just head back, as any updates will have already
		//-- been collected.
		if ( checkins != null ) return;
		
		//-- is the time of this update before the last new "checkin" that 
		//-- we detected? If so we have already covered the content 
		//-- of this update and so can discard it
		if ( time <= user.getLastCheckin() ) return;
		
		//-- if we have got here we need to find out what has happened 
		//-- since we last checked the like lists.
		checkins = new FBCheckinSet( 
			Facebook.getURL( user.dataURL( "checkins", user.getLastCheckin() )	)
		);

		//-- and finally process each checkin. If we have no new checkins then
		//-- we assume one has been deleted.
		if ( checkins.size() == 0 ) {
		
			String msg = "A Facebook checkin has been deleted.";
			
			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:checkin", "delete" )
				.setMtime( time )
				.setDesc( msg )
				.addTag( "ds:geo" );
			AppLog.log( time, "FB-UPDATE [GEO]", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
		}
		else {
			
			long mostRecentTime = 0; 
			String msg = "";
			
			for ( FBCheckin c : checkins ) {
				
				msg = "New Facebook checkin at "; 	
				
				long itemCreated = c.getCreatedTime().getTime();
				
				//-- and is this the latest checkin we have seen?
				if ( itemCreated > mostRecentTime ) 
					mostRecentTime = itemCreated;

				double lon = c.getPlace().getLongitude();
				double lat = c.getPlace().getLatitude();
				
				msg += c.getCreatedTime().toString();
				msg += " - " + c.getPlace().getName();
				msg += " (" + lon+ 
						  "," + c.getPlace().getLatitude() + 
						  ")";

				DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:checkin", "delete" )
					.setMtime( time )
					.setDesc( msg )
					.setLocation( lon, lat )
					.addTag( "ds:geo" );
				AppLog.log( time, "FB-UPDATE [GEO]", d.getDesc(), user.getUid() );
				send( user.getJID(), d.toString() );
				user.incrementNoCheckin();
			}
			
			user.setLastCheckin( mostRecentTime );
		}
	}
	
	
	
	/**
	 * @throws IOException 
	 * @throws JSONException 
	 **/
	private void processPicUpdate( FBUser user, String field, long time ) 
	throws JSONException, IOException {
		
		String query = "SELECT pic_big FROM user WHERE uid=" + user.getUid();
		String url = 
			Facebook.fqlURL +
			"?access_token=" + user.getUAtoken() +
			"&format=JSON" +
			"&query=" + URLEncoder.encode( query, "UTF-8" );

		JSONArray result = new JSONArray( Facebook.getURL( url ) );
		
		if ( result.length() > 0 ) {
			
			String picurl = result.getJSONObject( 0 ).getString( "pic_big" );
			String msg = "Your Facebook profile picture has been updated.";
			
			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:avatar", "delete" )
				.setMtime( time )
				.setDesc( msg )
				.addTag( "ds:avatar" )
				.addTag( "ds:photo" )
				.addMetadata( "picurl", picurl );
			AppLog.log( time, "FB-UPDATE", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
		}
		else {

			String msg = "Your Facebook profile picture has been removed";
			
			DSUpdate d = new DSUpdate( "ds:facebook", "ds:fb:avatar", "delete" )
				.setMtime( time )
				.setDesc( msg )
				.addTag( "ds:avatar" )
				.addTag( "ds:photo" );
			AppLog.log( time, "FB-UPDATE", d.getDesc(), user.getUid() );
			send( user.getJID(), d.toString() );
		}
		
		//-- and finally log the changes
		user.setLastMedia( time );
		user.incrementNoMedia();
	}

	
	private void send( String recipient, String msgBody ) {
    
		if ( recipient == null ) {
			log.warning( "XMPP ERROR **** [" + recipient + "]: " + msgBody );
			return;
		}
		JID jid = new JID( recipient + "/datasphere" );
		Message msg = new MessageBuilder()
            .withRecipientJids(jid)
            .withBody(msgBody)
            .build();
        boolean messageSent = false;
        XMPPService xmpp = XMPPServiceFactory.getXMPPService();
        SendResponse status = xmpp.sendMessage( msg );
        messageSent = ( status.getStatusMap().get(jid) == SendResponse.Status.SUCCESS );
        
        //JID jid2 = new JID( recipient );
        //xmpp.sendMessage( msg );
        
        if (!messageSent) 
        	log.warning( "XMPP ERROR **** [" + recipient + "]: " + msgBody );
        else 
        	log.warning( "XMPP [" + recipient + "]: " + msgBody );
	}

}
