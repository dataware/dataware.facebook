package datasphere.shims.facebook.entities;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FBObjectSet implements Iterable< FBObject > {
	
	private ArrayList< FBObject > objects = new ArrayList< FBObject >();

	public FBObjectSet( String json ) 
	throws JSONException {
	
		JSONObject wrapper = new JSONObject( json );
		JSONArray items = wrapper.getJSONArray( "data" );
		for ( int i = 0; i < items.length() ; i++ )
			 objects.add( new FBObject( items.getJSONObject( i ) ) );
	}

	@Override
	public Iterator< FBObject > iterator() {
		return objects.iterator();
	}

	public int size() {
		return objects.size();
	}
	
	@Override
	public String toString() {
		return "FBObjectSet [objects=" + objects + "]";
	}
}