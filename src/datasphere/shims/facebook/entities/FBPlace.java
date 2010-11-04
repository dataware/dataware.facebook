package datasphere.shims.facebook.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class FBPlace 
extends FBItem {
	
	private double latitude;
	private double longitude; 
	
	public FBPlace( String name, String id, double longitude, double latitude ) {
		super( name, id );
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public FBPlace( JSONObject obj ) 
	throws JSONException {
		
		super( 
			obj.getString( "name" ),
			obj.getString( "id" )
		);
		
		JSONObject location = obj.getJSONObject( "location" );
		this.latitude = location.getDouble( "latitude" );
	    this.longitude = location.getDouble( "longitude" );
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return 
			super.toString() +
			"FBPlace [" + super.toString() + 
			"latitude=" + latitude + ", longitude=" + longitude + "]";
	}
	
	
}
