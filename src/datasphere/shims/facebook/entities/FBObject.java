package datasphere.shims.facebook.entities;


import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import datasphere.shims.facebook.Facebook;


public class FBObject 
extends FBItem {
	
	private String category;
	private Date createdTime;
	
	public FBObject( String name, String category, String id, Date createdTime ) {
		super( name, id );
		this.category = category;
		this.createdTime = createdTime;
	}

	public String getCategory() {
		return category;
	}


	public Date getCreatedTime() {
		return createdTime;
	}

	public FBObject( JSONObject obj ) 
	throws JSONException {
		
		super( obj );
		
		this.category = obj.getString( "category" );
		try {
			this.createdTime = Facebook.timefmt.parse( 
				obj.getString( "created_time" ) 
			);
		} catch ( ParseException e ) {
			throw new JSONException( e );
		}
	}

	@Override
	public String toString() {
		return "FBObject [" + super.toString() + 
			"category=" + category + ", createdTime="
			+ createdTime + ", " + super.toString() + "]";
	}
}	
 
