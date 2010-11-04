package datasphere.shims.facebook;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Email;

@PersistenceCapable
public class FBUser {
	
    @PrimaryKey
    @Persistent
    private String uid;
    
    @Persistent
    private long joined;
    
	@Persistent
    private String UAtoken;
    
	@Persistent
    private boolean authenticated;
    
    @Persistent
    private Email JID;
    
    //-- BefriendStatus can be NONE/FAILED/PRESENT/PENDING/COMPLETE
    @Persistent
    private String BefriendStatus;
    
    @Persistent
    private long BefriendStatusSent;
    
    @Persistent
    private boolean validPermissions;
    
    @Persistent
    private long lastUpdated;
    
    @Persistent
	private long lastLike;
  
    @Persistent
    private long lastCheckin;
    
    @Persistent
    private long lastPost;
    
    @Persistent
    private long lastMedia;
    
    @Persistent
    private long lastBio;
    
    @Persistent
    private long lastFeed;
    
    @Persistent
    private long lastSocial;
    
    @Persistent
    private long noUpdated;
    
    @Persistent
	private long noLike;
  
    @Persistent
    private long noCheckin;
    
    @Persistent
    private long noPost;
    
    @Persistent
    private long noMedia;
    
    @Persistent
    private long noBio;

    @Persistent
    private long noFeed;

    @Persistent
    private long noSocial;
    
    public FBUser( String uid, String UAtoken ) {
        this.uid = uid;
        this.UAtoken = UAtoken;
        this.BefriendStatus = "NONE";
    }
    
    public String getUid() { return uid; }
	public void setJoined( long joined ) { this.joined = joined; }
	public String getUAtoken() { return UAtoken; }
	public void setUatoken( String uatoken ) { this.UAtoken = uatoken;	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated( boolean authenticated ) {
		this.authenticated = authenticated;
	}

	public String getJID() {
		return ( JID == null ) ? null : JID.getEmail();
	}

	public void setJID( String JID )
	throws Exception {
		if ( JIDValidator.validate( JID ) ) {
			this.JID = new Email( JID ) ;	
		} else {
			throw new Exception( "JID supplied is in an incorrect format." );
		}
	}
	
	public String getBefriendStatus() {
		return BefriendStatus;
	}

	public void setBefriendStatus(String befriendStatus) {
		BefriendStatus = befriendStatus;
	}

	public boolean hasValidPermissions() {
		return validPermissions;
	}

	public void setValidPermissions( boolean b ) {
		this.validPermissions = b;
	}

	public void setBefriendStatusSent(long befriendStatusSent) {
		BefriendStatusSent = befriendStatusSent;
	}

	public long getBefriendStatusSent() {
		return BefriendStatusSent;
	}

	public static FBUser fetch( PersistenceManager pm, String targetUid ) {
	    return pm.getObjectById( FBUser.class, targetUid );
	}

	public void setLastUpdated( long time ) { this.lastUpdated = time; }	
	public void setLastLike( long time ) 	{ this.lastLike = time; }
	public void setLastCheckin( long time )	{ this.lastCheckin = time; }
	public void setLastPost( long time ) 	{ this.lastPost = time;	}
	public void setLastMedia( long time ) 	{ this.lastMedia = time; }
	public void setLastBio( long time ) 	{ this.lastBio = time; }
	public void setLastFeed( long time ) 	{ this.lastFeed = time; }
	public void setLastSocial( long time )  { this.lastSocial = time; }
	
	//-- GETTERS
	public long getJoined() 	 { return joined; }
	public long getLastUpdated() { return lastUpdated; }
	public long getLastLike() 	 { return lastLike; }
	public long getLastCheckin() { return lastCheckin; }	
	public long getLastPost() 	 { return lastPost; }
	public long getLastMedia() 	 { return lastMedia; }
	public long getLastBio() 	 { return lastBio; }
	public long getLastFeed() 	 { return lastFeed; }
	public long getLastSocial()  { return lastSocial; }
	
	public long getNoUpdate() 	{ return noUpdated; }
	public long getNoPost()		{ return noPost; }	
	public long getNoLike()    	{ return noLike; }
	public long getNoCheckin() 	{ return noCheckin; }
	public long getNoMedia()   	{ return noMedia; }
	public long getNoBio() 	   	{ return noBio; }
	public long getNoFeed() 	{ return noFeed; }
	public long getNoSocial()   { return noSocial; }
	
	public void incrementNoPost()    { noPost++;    noUpdated++; }
	public void incrementNoLike()    { noLike++;    noUpdated++; }
	public void incrementNoCheckin() { noCheckin++; noUpdated++;  }
	public void incrementNoMedia()   { noMedia++;   noUpdated++;  }
	public void incrementNoBio() 	 { noBio++;     noUpdated++; }
	public void incrementNoFeed() 	 { noFeed++;    noUpdated++; }
	public void incrementNoSocial()  { noSocial++;  noUpdated++; }

	
	public void resetStats()    {
		noPost = 0;
		noLike = 0;
		noCheckin = 0;
		noMedia = 0;
		noBio = 0;
		noUpdated = 0;
	}
	
	
	public static String format( Date d ) {
		if ( d == null ) {
			return "never";
		} else {
			SimpleDateFormat fmt = new SimpleDateFormat( "dd MMM yyyy HH:mm" );
			return fmt.format( d );
		}
	}
	
	
	public static String format( long time ) {
		return ( time == 0 ) ? "never" : format ( new Date( time ) );
	}
	
	
	public String dataURL( String tag ) {	
		return "https://graph.facebook.com/" 
			+ getUid() + "/" + tag + 
			"?access_token=" + getUAtoken();
	}
	
	
	public String dataURL( String tag, long since ) {
		
		try {
			Date l = new Date( since );
			return dataURL( tag ) + "&since=" + 
			 	URLEncoder.encode( Facebook.timefmt.format( l ), "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			return dataURL( tag );
		}
	}
	
	
	public String dataURL( String tag, long since, long until ) {
		
		Date u = new Date( until );
		
		try {
			return dataURL( tag, since ) + "&until=" + 
			 	URLEncoder.encode( Facebook.timefmt.format( u ), "UTF-8" );
		} catch (UnsupportedEncodingException e) {
			return dataURL( tag, since );
		}
	}

	
	public String meURL() {
		return "https://graph.facebook.com/" 
			+ getUid() + "?access_token=" + getUAtoken();
	}
	

	public boolean hasNewJID( String jid ) {
		
		if ( jid == null ) 
			return false;
		else if ( this.JID == null ) 
			return true;
		else 
			return 
				( JID.getEmail().equalsIgnoreCase( jid ) ) 
				? false : true;
	}

	
	public static long getAsTime( Date d ) {
		return ( d == null ) ? 0 : d.getTime();
	}
}
