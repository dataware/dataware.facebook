package datasphere.shims.facebook.entities;


import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import datasphere.shims.facebook.Facebook;


//-- currently does not include comments
public class FBCheckin 
extends FBItem
implements Comparable< FBCheckin > {
	
	private FBItem from;
	private FBPlace place;
	private FBItem application;
	private Date createdTime;
	private String message = "";
	private Set< FBItem > tags = new HashSet< FBItem >();

	public FBCheckin( JSONObject obj ) 
	throws JSONException {
		
		super( obj );
		
		this.from = new FBItem( obj.getJSONObject( "from" ) );
		this.place = new FBPlace( obj.getJSONObject( "place" ) );
		
		this.application = new FBItem( obj.getJSONObject( "application" ) );

		try {
			this.createdTime = Facebook.timefmt.parse( 
				obj.getString( "created_time" ) 
			);
		} catch ( ParseException e ) {
			throw new JSONException( e );
		}
		
		if ( obj.has( "message" ) ) {
			this.message = obj.getString( "message" );
		}
		
		if ( obj.has( "tags" ) ) {
			JSONArray data = obj.getJSONObject( "tags" ).getJSONArray( "data" );
			for ( int i = 0; i < data.length(); i++ )
				tags.add( new FBItem( data.getJSONObject( i ) ) );
		}
	}

	public FBItem getFrom() {
		return from;
	}

	public FBPlace getPlace() {
		return place;
	}

	public FBItem getApplication() {
		return application;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public String getMessage() {
		return message;
	}

	public Set<FBItem> getTags() {
		return tags;
	}

	
	@Override
	public String toString() {
		return "FBCheckin [" + super.toString() + 
				"application=" + application + ", createdTime="
				+ createdTime + ", from=" + from + ", message="
				+ message + ", place=" + place + ", tags=" + tags + "]";
	}
	

	@Override
	public int compareTo( FBCheckin c ) {
        return createdTime.compareTo( c.createdTime );
	}
}	
 
