<%@ page language="java"%>
<%@ page import="chant.Facebook" %>
<%@ page import="chant.MainServlet" %>
<%@ page import="chant.CookieUtils" %>
<%@ page import="chant.FBUser" %>
<%@ page import="chant.PMF" %>
<%@ page import="chant.MainServlet" %>
<%@ page import="chant.CookieUtils" %>
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
    	
    	if ( user.getJID() != null )
    		session.setAttribute( "jid", user.getJID() );
    	
    	String jid = ( String ) session.getAttribute( "jid" );
    	String cause = request.getParameter( "cause" );
    	
%>
<%@ include file="header.jsp" %>
<script>$( "#bgbox" ).css( "background-image", "url('/images/bgbox-shake.png')" );</script>
<%
	 	if ( cause == null || cause.equals( "missing" ) ) {   			
%>
			<div style="width:450px;">
	  			<img src="/images/speech.gif" align="left"/>
	  				In order to give you more control over your Facebook data, we have to connect it 
	  				to your <b>datasphere</b> - and to do that we need to know its address.
	  		</div>
	  		
	  		<div style="width:375px; margin: 15px 0px 0px 73px;">
	   			<form name="getJID" action="main" method="get">
					<span style="color: #928d65; font-family: Georgia; font-size: 22px;">
						please supply the address of <br/>your datasphere:
					</span>
	    
	     			<div id="inputbox">
	     				<input type="text" size="32" maxlength="128" name="jid" value="<%= (jid == null ) ? "" : jid %>" />
	     			</div>
					<a href="javascript:document.forms['getJID'].submit();" 
		        	onmouseout="button_off('connect'); return true;" 
	    	    	onmouseover="button_on('connect'); return true;" >
	        			<img src="/images/connect_off.png" style="border: none; margin-left:-4px" id="connect" />
	        		</a>
	        		<input type="hidden" value="true" name="link"/>
				</form>
			</div> 
<%				
	 	} else if ( cause.equals( "invalid" ) ) {
%>	 
			<div style="width:450px;">
	  			<img src="/images/speech.gif" align="left"/>
	  				<span class="medium_red">Invalid Datasphere address</span> - please retry or supply 
	  				a different address (or check for typos) - n.b. at the moment 
	  			 	your datasphere is not connected to facebook.</i>
	  			</div>
	  		
	  			<div style="width:375px; margin: 15px 0px 0px 73px;">
	   			<form name="getJID" action="main" method="get">
					<h1>please supply a valid datasphere address:</h1>
					
		     		<div id="inputbox">
		     			<input type="text" size="32" maxlength="128" name="jid" value="<%= (jid == null ) ? "" : jid %>" />
		     		</div>
					<a href="javascript:document.forms['getJID'].submit();" 
			        	onmouseout="button_off('connect'); return true;" 
		    	    	onmouseover="button_on('connect'); return true;" >
		        		<img src="/images/connect_off.png" style="border: none; margin-left:-4px" id="connect" />
		        	</a>
		        	<input type="hidden" value="true" name="connect"/>
				</form>
			</div>
<%
	 	} else if ( cause.equals( "failed" ) ) {
%>	 
			<div style="width:450px;">
	  			<img src="/images/speech.gif" align="left"/ style="margin-bottom:20px"/>
	  			We <span style="color:color:#00bb00; font-weight:bold;">failed to connect</span> to the supplied datasphere
	  			address. Either:
	  			
	  			<div style="margin: 8px 0px 0px 10px">
	  				<li>try a different one, or check for typos <i>(recommended)</i>.</li>
	  				<li>try again later via the admin panel.</li>
	  			</div>
	  		</div>
	  		
	  		<div style="width:375px; margin: 15px 0px 0px 73px;">
	   			<form name="getJID" action="main?connect=true" method="get">
					<span style="color: #928d65; font-family: Georgia; font-size: 22px;">
						try a different datasphere address?
					</span>
					
		     		<div id="inputbox">
		     			<input type="text" size="32" maxlength="128" name="jid" value="<%= (jid == null ) ? "" : jid %>" />
		     		</div>
					<a href="javascript:document.forms['getJID'].submit();" 
			        	onmouseout="button_off('connect'); return true;" 
		    	    	onmouseover="button_on('connect'); return true;" >
		        		<img src="/images/connect_off.png" style="border: none; margin-left:-4px" id="connect" />
		        	</a>
		        	<br/>
					<a href="javascript:window.location='main';" 
			        	onmouseout="button_off('skip'); return true;" 
		    	    	onmouseover="button_on('skip'); return true;" >
		        		<img src="/images/skip_off.png" style="border: none; margin-left:-4px" id="skip" />
		        	</a>
		        	<input type="hidden" value="true" name="connect"/>
				</form>
			</div>
<%
		}
	 	else {
%>
			There has been a problem of unknown cause...
<%
	 	}
%>
<%@ include file="footer.jsp" %>
<%
		pm.close();
    }
%>


