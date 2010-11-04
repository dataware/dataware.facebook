package datasphere.shims.facebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class required to sort out the mess that is the Facebook update response.
 * It folds identical updates together, and collates updates of different 
 * fields, but which are for the same person and timestamp together.
 * @author psxjog
 *
 */

public class FBUpdateSet 
implements Iterable< FBUpdate > {

	private ArrayList< FBUpdate > updateList = new ArrayList< FBUpdate >();
	
	/**
	 * 
	 * @param input
	 * @throws JSONException
	 */
	public FBUpdateSet( String input ) 
	throws JSONException {
		JSONObject json = null;
		json = new JSONObject( input );
		JSONArray entries = json.getJSONArray( "entry" );
		parse( entries );
		compressAllFields();	
		Collections.sort( updateList ); 
	}
	
	
	/**
	 * 
	 * @param entries
	 * @throws JSONException
	 */
	private void parse( JSONArray entries )
	throws JSONException {

		//-- break down each update entry
		for( int i = 0; i < entries.length(); i++ ) {
			
			JSONObject nextEntry = entries.getJSONObject( i );
			String uid = nextEntry.getString( "uid" );
			Date time = new Date( nextEntry.getLong( "time" ) * 1000 );
			JSONArray fields = nextEntry.getJSONArray( "changed_fields" );
			
			//-- does this person already exist in our updateList?
			FBUpdate update = null;

			//-- if we already have an update in the set for the same person
			//-- and timestamp, add to it rather than creating a new one.
			for ( FBUpdate f : updateList ) {
				if ( f.getUid().equals( uid ) &&  f.getDate().equals( time ) ) {
					update = f;
				}
			}
			
			//-- if an update didn't already exist then create one, and fill it
			if ( update == null ) { 
				update = new FBUpdate( uid, time, fields );
				updateList.add( update );
			}
			//-- otherwise add the new fields to the pre-existing update
			else { 
				update.add( fields );
			}
		}
	
	}
	
	
	@Override
	public String toString() {
		return "FBUpdateSet [updateList=" + updateList + "]";
	}


	/**
	 * Function that condenses the fields down into useful items.
	 * if the a map entry contains anything and a feed we can dump the feed
	 * because the information is already represented somewhere else
	 * If the map now contains a likes we can dump the individual category,
	 * and pick up everything that has changed.			
	 */
	private void compressAllFields() {
		for ( FBUpdate u : updateList ) {
			u.compressFields();
		}
	}
	
	/**
	 * 
	 */
	@Override
    public Iterator< FBUpdate > iterator() {
        return updateList.iterator(); 
    }

}
