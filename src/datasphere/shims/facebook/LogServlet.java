package datasphere.shims.facebook;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings( "serial" )
public class LogServlet extends HttpServlet {
	
	@SuppressWarnings( "unchecked" )
	public void doGet(
		HttpServletRequest req, 
		HttpServletResponse resp )
	throws IOException {
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query query = pm.newQuery( AppLog.class );
		query.setOrdering("timestamp desc");
		//query.setFilter("owner == 0");
		//query.setRange(5, 10);
	    List< AppLog > results = ( List<AppLog> ) query.execute();
	    
		resp.setContentType( "text/html" );
	    for ( AppLog a : results ) 
	    	resp.getWriter().println( a.toString() + "<br/>" );
	    
	    pm.close();
	}
}
