package datasphere.shims.facebook.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FBCheckinSet 
implements Iterable< FBCheckin > {
	
	private ArrayList< FBCheckin > checkins = new ArrayList< FBCheckin >();

	public FBCheckinSet( String json ) 
	throws JSONException {
	
		JSONObject wrapper = new JSONObject( json );
		JSONArray items = wrapper.getJSONArray( "data" );
		for ( int i = 0; i < items.length() ; i++ )
			checkins.add( new FBCheckin( items.getJSONObject( i ) ) );
		
		Collections.sort( checkins ); 
	}

	@Override
	public Iterator< FBCheckin > iterator() {
		return checkins.iterator();
	}

	public int size() {
		return checkins.size();
	}
	
	@Override
	public String toString() {
		return "FBCheckinSet [checkins=" + checkins + "]";
	}
}