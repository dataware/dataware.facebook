<%@ page language="java" %>
<%@ page import="chant.MainServlet" %>
<%@ page import="chant.Facebook" %>

	   	</div>
    </div>
	<div id='fb-root'></div>
	<script src='http://connect.facebook.net/en_US/all.js'></script>
	<script>
		FB.init( { appId: '<%=Facebook.getClientId()%>', status: true, cookie: true, xfbml: true } );
	    
	    FB.getLoginStatus( function( response ) {
		  if ( response.session ) {
		  	if ( window.location.pathname == "<%= MainServlet.LOGIN_PAGE %>" )
    			window.location = "main";
    		else {
    			$( "#top" ).html( "<%= session.getAttribute( "name" ) %> | " );
    			$( "#top" ).append( "<a href=\"javascript:FB.logout( function( response ) { window.location = 'main?logout=true'; } ); \" >logout</a>");		
    		}
		  } else {
    		if ( window.location.pathname != "<%= MainServlet.LOGIN_PAGE %>" )
    			window.location = "<%= MainServlet.LOGIN_PAGE %>";
  		  }
		});
		
	</script>
	<script> 
	    function login() {
	    	FB.login( function( response ) {
	    		if( response.session ) {
	    			window.location = "main?link=true";
	    		} 
	    	}, 
	    	{perms:'<%=Facebook.getUserScope()%>'})
	    }
	</script>
  </body>
</html>