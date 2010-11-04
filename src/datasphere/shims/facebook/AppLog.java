package datasphere.shims.facebook;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;


@PersistenceCapable
public class AppLog {

    @SuppressWarnings("unused")
	@PrimaryKey
    @Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
    private Key key;
    
    @Persistent
    private String level;
    
    @Persistent
    private String text;
    
    @Persistent
    private String owner;
    
    @Persistent
    private long timestamp;
    
    @Persistent
    private long eventTime;
	
    @SuppressWarnings("unused")
	@Persistent
    private String status;
    
    private AppLog( String level, String text, String owner, long eventTime ) {
		this.level = level;
		this.text = text;
		this.owner = owner;
		this.eventTime = eventTime;
		this.status = "PENDING";
		this.timestamp = System.currentTimeMillis();
	}
    
	@Override
	public String toString() {
		
		SimpleDateFormat fmt = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		SimpleDateFormat smallfmt = new SimpleDateFormat( "MM-dd HH:mm:ss" );
		
		return fmt.format( timestamp ) +
			" [" + level + "]" +
			" " + owner + " -> " +
			" " + text + 
			" (" + smallfmt.format( eventTime ) + ")";
	}
	
	public static void log( PersistenceManager pm, long eventTime, String ... params  ) {
		
		AppLog appLog = new AppLog( 
			params.length > 0 ? params[ 0 ] : "INFO",
			params.length > 1 ? params[ 1 ] : "no text supplied",
			params.length > 2 ? params[ 2 ] : "APP",
			eventTime
		);
		
		pm.makePersistent( appLog );
	}
	
	public static void log( long eventTime, String ... params ) {
	    PersistenceManager pm = PMF.get().getPersistenceManager();
		log( pm, eventTime, params );
		pm.close();
	}
}
