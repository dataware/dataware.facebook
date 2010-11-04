package datasphere.shims.facebook.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FBPostSet 
implements Iterable< FBPost > {
	
	private ArrayList< FBPost > posts = new ArrayList< FBPost >();

	public FBPostSet( String json ) 
	throws JSONException {
	
		JSONObject wrapper = new JSONObject( json );
		JSONArray items = wrapper.getJSONArray( "data" );
		for ( int i = 0; i < items.length() ; i++ )
			posts.add( new FBPost( items.getJSONObject( i ) ) );
		
		Collections.sort( posts ); 
	}

	@Override
	public Iterator< FBPost > iterator() {
		return posts.iterator();
	}

	public int size() {
		return posts.size();
	}
	
	@Override
	public String toString() {
		return "FBPost [posts=" + posts + "]";
	}
}