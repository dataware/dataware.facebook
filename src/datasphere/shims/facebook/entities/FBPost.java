package datasphere.shims.facebook.entities;


import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import datasphere.shims.facebook.Facebook;


public class FBPost
extends FBItem
implements Comparable< FBPost > {
	
	private static final Logger log = Logger.getLogger( 
			FBPost.class.getName() 
	);
	String type;
	FBItem from;
	String message;
	String picture;
	String link;
	String icon;
	String privacy;

	Date createdTime;
	Date updatedTime;
	int comments;
	
	public FBPost( JSONObject obj ) 
	throws JSONException {
		
		super( obj );

		//log.warning("IN FBPOST:" + obj);
		//if ( obj.has("type") ) log.warning( "HAS TYOE!"); else log.warning( "noooooo!");
		this.type = obj.getString( "type" );
		
		//log.warning("GOT TYPE OBJECT");
		this.from = new FBItem( obj.getJSONObject( "from" ) );
		this.message = obj.has( "message" ) ? obj.getString( "message" ) : "";
		this.picture = obj.has( "picture" ) ? obj.getString( "picture" ) : "";
		this.link = obj.has( "link" ) ? obj.getString( "link" ) : "";
		this.icon = obj.has( "icon" ) ? obj.getString( "icon" ) : "";
		this.privacy = obj.has( "privacy" ) ? obj.getString( "privacy" ) : "";
		
		this.comments = obj.has( "comments" ) ? 
						( 
							obj.getJSONObject("comments").has( "count" ) ?
							obj.getJSONObject("comments").getInt( "count" ) 
							: 1 
						) : 0;
		
		try { this.createdTime = Facebook.timefmt.parse( obj.getString( "created_time" ) ); }
		catch ( ParseException e ) {throw new JSONException( e ); }

		try { this.updatedTime = Facebook.timefmt.parse( obj.getString( "updated_time" ) ); }
		catch ( ParseException e ) {throw new JSONException( e ); }
	}
	
	public String getType() {
		return type;
	}

	public FBItem getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}

	public String getPicture() {
		return picture;
	}

	public String getLink() {
		return link;
	}

	public String getIcon() {
		return icon;
	}

	public String getPrivacy() {
		return privacy;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getUpdatedTime() {
		return updatedTime;
	}

	public int getComments() {
		return comments;
	}

	@Override
	public int compareTo( FBPost c ) {
        return createdTime.compareTo( c.createdTime );
	}

	@Override
	public String toString() {
		return "FBPost [" + super.toString() + 
				"comments=" + comments + ", createdTime=" + createdTime
				+ ", from=" + from + ", icon=" + icon + ", link=" + link
				+ ", message=" + message + ", picture=" + picture
				+ ", privacy=" + privacy + ", type=" + type + ", updatedTime="
				+ updatedTime + "]";
	}
	
}
