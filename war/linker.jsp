<%@ page language="java"%>
<%@ page import="chant.Facebook" %>
<%@ page import="chant.MainServlet" %>
<%@ page import="chant.CookieUtils" %>
<%@ page import="chant.FBUser" %>
<%@ page import="chant.PMF" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Map" %>
<%@ page import="javax.jdo.JDOObjectNotFoundException" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.servlet.http.HttpServlet" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="javax.servlet.http.HttpSession" %>

<%
	//-- harvest cookie information
	Map< String, String > cookie = CookieUtils.fetch(
		request.getCookies(), Facebook.getCookieName() );

    //-- authentication check
    if ( cookie == null ) {
    	response.sendRedirect( MainServlet.LOGIN_PAGE );
    } else {
    	//-- obtain user information
    	PersistenceManager pm = PMF.get().getPersistenceManager();
    	FBUser user = pm.getObjectById( FBUser.class, cookie.get( "uid" ) );
    	String jid = ( String ) session.getAttribute( "jid" );
    	String cause = request.getParameter( "cause" );
%>
<%@ include file="header.jsp" %>
<script>$( "#bgbox" ).css( "background-image", "url('/images/bgbox-shake.png')" );</script>

	<script>
		function begin() {
			setBox( "detect", "pending" );
			setTimeout( "doDetect()" ,1000 );
		}
		
		function setBox( name, result ) {
			divLeft = "#" + name + "_left";
			divRight = "#" + name + "_right";
			
			if ( result == "success" ) {
				$( divLeft ).html( "<img src='/images/ajax-success.gif' style='margin-top:8px;' />" );							
				$( divRight ).removeClass().addClass( "progress_right" );
				$( divRight ).append( " <b>success!</b>" );
			}
			else if ( result == "failure" ) {
				$( divLeft ).html("<img src='/images/ajax-failure.gif' style='margin-top:8px;' />" );							
				$( divRight ).removeClass().addClass( "progress_right" );
				$( divRight ).append( " <b>failed</b>" );
			}
			else { 
				$( divLeft ).html( "<img src='/images/ajax-loading.gif' style='margin-top:8px;' />" );
				$( divRight ).removeClass().addClass( "progress_right" );
			}
		}

		function doDetect() {
			jQuery.ajax({ 
				type : "GET", 
				url : "http://data-chant.appspot.com/speak?action=detect",
				cache : false,
				async : true,
				dataType : "json",
				contentType : "json",
				timeout : 10000,			
				error : function( data ) { 
					setBox( "detect", "failure" );
					failed( "error" );
				}, 
				success : function( data ) { 
					if ( data.success == true ) {
						setBox( "detect", "success" );
						setBox( "invite", "pending" );
						setTimeout( "doInvite()", 1000 );
					} else {
						setBox( "detect", "failure" );
						failed( "detect" );
					}
				}
			});
		}
				
		function doInvite() {
			setBox( "invite", "pending" );
			jQuery.ajax({ 
				type : "GET", 
				url : "http://data-chant.appspot.com/speak?action=invite",
				cache : false,
				async : true,
				dataType : "json",
				contentType : "json",
				timeout : 10000,			
				error : function( data ) { 
					setBox( "invite", "failure" );
					failed( "error" );
				}, 
				success : function( data ) { 
					if ( data.success == true ) {
						setBox( "invite", "success" );
						setBox( "confirm", "pending" );
						setTimeout( "doConfirm()", 1000 );
					} else {
						setBox( "invite", "failure" );
						failed( "invite" );
					}
				}
			});
		}
		
		
		attempts = 0;
		function doConfirm() {
			attempts++;
			setBox( "confirm", "pending" );
			$( "#confirm_right" ).html( "Getting confirmation - <b>attempt " + attempts + " of 5</b>");

			jQuery.ajax({ 
				type : "GET", 
				url : "http://data-chant.appspot.com/speak?action=confirm",
				cache : false,
				async : true,
				dataType : "json",
				contentType : "json",
				timeout : 10000,			
				error : function( data ) { 
					setBox( "error", "failure" );
					failed();
				}, 
				success : function( data ) { 
					if ( data.success == true ) {
						$( "#confirm_right" ).html( "Getting confirmation..." ); 
						setBox( "confirm", "success" );
						success();
					} else if ( attempts < 5 ) {
						setTimeout( "doConfirm()" , 4000 );
					} else {
						$( "#confirm_right" ).html( "Getting confirmation..." ); 
						setBox( "confirm", "failure" );
						failed( "confirm" );
					}
				}
			});
		}
		
		function failed( cause ) {
			$( '#result' ).append( "<span class='medium_red'>Connection failed...</span> ");
			
			if ( cause == "confirm" ) {
				$( '#result' ).append( "Our invite has not been accepted yet - you may need to do this manually <b>at your datasphere</b>... " );
				$( '#result' ).append( "<a href='main' onmouseout=\"button_off('skip'); return true;\" onmouseover=\"button_on('skip'); return true;\"><img id='skip' style='margin:12px 0px 0px -5px' src='/images/skip_off.png'/></a>" );
				$( '#result' ).append( "<a href='main?connect=true' onmouseout=\"button_off('retry'); return true;\" onmouseover=\"button_on('retry'); return true;\"><img id='retry' src='/images/retry_off.png'/></a>" ); 
		  	} else if ( cause == "invite" ) {
		  		$( '#result' ).append( "There is a technical issue meaning that we cannot currently invite your datasphere. " );
				$( '#result' ).append( "<i>Please try again later via the admin panel</i>.<br/>" );
				$( '#result' ).append( "<a href='main' onmouseout=\"button_off('skip'); return true;\" onmouseover=\"button_on('skip'); return true;\"><img id='skip' style='margin:12px 0px 0px -5px' src='/images/skip_off.png'/></a>" );
		  	} else {
		  		$( '#result' ).append( "The datasphere at <b>james.goulding@gmail.com</b> could not be contacted. Please retry using a new one, or try again later...<br/>" );
				$( '#result' ).append( "<a href='main' onmouseout=\"button_off('skip'); return true;\" onmouseover=\"button_on('skip'); return true;\"><img id='skip' style='margin:12px 0px 0px -5px' src='/images/skip_off.png'/></a>" );
				$( '#result' ).append( "<a href='main?connect=true' onmouseout=\"button_off('retry'); return true;\" onmouseover=\"button_on('retry'); return true;\"><img id='retry' src='/images/retry_off.png'/></a>" );					
			}
		}
		
		function success() {
			$( '#result' ).append( "<span class='medium'>Successful connection!</span> ");
			$( '#result' ).append( "We are now privately notifying your datasphere of changes to your facebook data...<br/>" );
			$( '#result' ).append( "<a href='main' onmouseout=\"button_off('continue'); return true;\" onmouseover=\"button_on('continue'); return true;\"><img id='continue' style='margin:10px 0px 0px -2px' src='/images/continue_off.png'/></a>" ); 
		}
		
		function pause( millis ){
			var date = new Date();
			var curDate = null;
			do { curDate = new Date(); }
			while( curDate - date < millis );
		}
		
		begin();
	</script>
 
	<div class="progress">
		<div class="progress_left" id="detect_left">1.</div>
		<div class="progress_right_inactive" id="detect_right" >
			Detecting datasphere...
		</div>
	</div>
	<div class="progress">
		<div class="progress_left" id="invite_left">2.</div>
		<div class="progress_right_inactive" id="invite_right">Inviting datasphere...</div>
	</div>			
	<div class="progress">
		<div class="progress_left" id="confirm_left" >3.</div>
		<div class="progress_right_inactive" id="confirm_right">Getting confirmation...</div>
	</div>
	<div id="result" style="width:310px; margin:15px 0px 0px 4px"></div>

<%@ include file="footer.jsp" %>
<%
		pm.close();
    }
%>
