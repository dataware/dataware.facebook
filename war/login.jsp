<%@ page language="java"%>
<%@ page import="chant.Facebook" %>
<%@ page import="chant.MainServlet" %>
<%@ page import="chant.CookieUtils" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%
	//-- harvest cookie information
	Map< String, String > cookie = CookieUtils.fetch(
		request.getCookies(), Facebook.getCookieName() );

	if ( cookie != null ) {
		response.sendRedirect( "main" );	
	} else {
%>
<%@ include file="header.jsp" %>
<script>$( "#bgbox" ).css( "background-image", "url('/images/bgbox.png')" );</script>

   		<div id="left">
   			<img src="/images/speech.gif" />
   			This is a free service that connects your facebook account to your personal datasphere, giving you
   			 more access, control and insight into <i>your</i> facebook data!
   			<br/><br/>
   			<img src="/images/speech.gif" />
	   		Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi 
	   		ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit
	   		in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 
   		</div>
   		
		<div id="right">
			<a href="javascript:login();"
	        	onmouseout="button_off('login'); return true;" 
	        	onmouseover="button_on('login'); return true;" >
	        	<img src="/images/login_off.png" style="border: none;" id="login" />
	        </a>
		</div>
   		
   		<div id="middle">
	   		<img src="/images/speech.gif" />
	   		Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi 
	   		ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit
	   		in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur
	   		sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
	   		mollit anim id est laborum...
	   		<br/><br/>
	 		<img src="/images/speech.gif" />
	 		Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod 
	 		tempor incididunt ut labore et dolore...
		</div>

		<%@ include file="footer.jsp" %>
<%
    }
%>



