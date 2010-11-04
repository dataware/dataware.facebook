package datasphere.shims.facebook.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class FBItem {
	
	private String name;
	private String id;
	private int size;
	
	public FBItem( String name, String id ) {
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getId() {
		return id;
	}
	
	public FBItem( JSONObject obj ) 
	throws JSONException {
		this.size = obj.toString().length();
		this.id = obj.getString( "id" );
		this.name = obj.has( "name" ) ? 
					obj.getString( "name" ) : 
					"item";
	}
	
	@Override
	public String toString() {
		return "FBItem [id=" + id + ", name=" + name + "]";
	}
}
