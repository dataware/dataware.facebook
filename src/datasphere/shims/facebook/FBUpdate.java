package datasphere.shims.facebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

public class FBUpdate 
implements Comparable< FBUpdate > {
	
	public static final int BIO = 0;
	public static final int LIKES = 1;
	public static final int PIC = 2;
	public static final int SOCIAL = 3;
	public static final int GEO = 4;
	public static final int FEED = 5;
	
	private static String[] fieldsBio = { 
		"location", 
		"hometown", 
		"birthday", 
		"religion", 
		"about", 
		"quotes", 
		"website", 
		"email", 
		"first_name", 
		"last_name", 
		"name", 
		"link", 
		"work", 
		"education" };
	
	private static String[] fieldsLikes = { 
		"music", 
		"activities", 
		"books", 
		"movies", 
		"television", 
		"interests" };
	
	private static String[] fieldsPic = { 
		"picture" };
	
	private static String[] fieldsSocial = { 
		"friends" };
	
	private static String[] fieldsGeo = { 
		"checkins" };
	
	private static String[] fieldsFeed = { 
		"feed", 
		"likes" };
	
	
	public String uid;
	public Date date;
	public Set< String > fields;
	
	
	/**
	 * 
	 * @param uid
	 * @param date
	 * @param fields
	 */
	public FBUpdate( String uid, Date date, Set< String > fields ) {
		this.uid = uid;
		this.date = date;
		this.fields = fields;
	}

	
	/**
	 * 
	 * @param uid
	 * @param date
	 * @param fields
	 * @throws JSONException
	 */
	public FBUpdate( String uid, Date date, JSONArray fields )
	throws JSONException {
		this.uid = uid;
		this.date = date;
		
		//-- convert JSONArray in to a list of fields
		this.fields = new HashSet< String >();
		for( int i = 0; i < fields.length(); i++ ) {
			
			String f = fields.getString( i );
			this.fields.add( f );
		}
	}
	
	
	/**
	 * 
	 */
	public String getUid() {
		return uid;
	}

	
	/**
	 * 
	 * @return
	 */
	public Date getDate() {
		return date;
	}

	
	/**
	 * 
	 * @return
	 */
	public Set< String > getFields() {
		return fields;
	}

	
	/**
	 * 
	 * @param uid
	 * @param time
	 * @param field
	 */
	public FBUpdate( String uid, Date date, String field ) {
		this( uid, date );
		this.fields.add( field );
	}
	
	
	/**
	 * 
	 * @param uid
	 * @param time
	 */
	public FBUpdate( String uid, Date date ) {
		this.uid = uid;
		this.date = date;
		this.fields = new HashSet< String >();
	}

	
	/**
	 * 
	 * @param newFields
	 */
	public void add ( ArrayList< String > newFields ) {
		this.fields.addAll( newFields );
	}

	/**
	 * 
	 * @param newFields
	 * @throws JSONException
	 */
	public void add ( JSONArray newFields ) 
	throws JSONException {

		for( int i = 0; i < newFields.length(); i++ ) {
			this.fields.add( newFields.getString( i ) );
		}
	}
	
	
	/**
	 * 
	 * @param field
	 * @param type
	 * @return
	 */
	public static boolean matches( String field, int type ) {
		
		List< String > fields;
		if ( type == FBUpdate.BIO ) fields = Arrays.asList( fieldsBio );
		else if ( type == FBUpdate.LIKES ) fields = Arrays.asList( fieldsLikes );
		else if ( type == FBUpdate.PIC ) fields = Arrays.asList( fieldsPic );
		else if ( type == FBUpdate.SOCIAL ) fields = Arrays.asList( fieldsSocial );
		else if ( type == FBUpdate.GEO ) fields = Arrays.asList( fieldsGeo );
		else if ( type == FBUpdate.FEED ) fields = Arrays.asList( fieldsFeed );
		else return false;
		
		return fields.contains( field );
	}

	
	public static boolean hasAllLikes( Set< String > fields ) {
		
		List< String > likes = Arrays.asList( fieldsLikes );
		for ( String s : likes ) {
			if( !fields.contains( s ) ) 
				return false;
		}
			
		return true; 
	}
	
	public static void removeLikes( Set< String > fields ) {
		fields.removeAll( Arrays.asList( fieldsLikes ) );
	}
	
	@Override
	public String toString() {
		return "FBUpdate [fields=" + fields + ", date=" + date + ", uid=" + uid + "]";
	}

	@Override
	public int compareTo( FBUpdate f ) {
        return date.compareTo( f.date );
	}


	public void compressFields() {
		
		Set< String > s = getFields();
		if ( s.size() > 1 && s.contains( "feed" ) ) { s.remove( "feed" ); }
		if ( s.size() > 1 && s.contains( "likes" ) )  
			for( String f : Arrays.asList( fieldsLikes ) ) 
				s.remove( f );
	}
}
