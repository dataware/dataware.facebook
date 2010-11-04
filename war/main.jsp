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
<script>$( "#bgbox" ).css( "background-image", "url('/images/bgbox.png')" );</script>
			
	<div style="float:left; width:340px;">
		<span class="medium">Admin panel</span> 
		- see your update stats, control your datasphere connection, organize data channels...
		<br/><br/>
		<div style="padding:7px; width:372px; height:162px; background-image:url('/images/profile_back.png'); background">
			<div style="background-color:#ffffff; padding:10px; width:350px;)">
				<div style="float:left; ">
					<img src="https://graph.facebook.com/<%= user.getUid() %>/picture"/>
				</div>
				<div style="margin-left:63px;">
					<div style="margin-bottom:7px;">
						<span class="fbfontblue">
							<%= session.getAttribute( "name" )%>
						</span>
					</div>
					<div style="float:left;">
						<span class="fbfont">
							facebook id:<br/>
							activated:
						</span>
					</div>
					<div style="margin-left:83px;">
						<span class="fbfont">
							<%= cookie.get( "uid" ) %><br/>
							<%= FBUser.format( user.getJoined() ) %>
						</span>
					</div>
				</div>
			</div>
			<div style="background-color:#ffffff; padding:10px; width:350px;">
				<div style="float:left; ">
					<img src="/images/icon_datasphere.png"/>
				</div>			
				<div style="margin-left:63px; background-color:#fbf9ed; padding:5px;">	
					<div style="margin-bottom:7px;">
						<span class="fbfontblue"><%= user.getJID() %></span>
					</div>
					<div>
						<span class="fbfont">datasphere updates: </span>
						<span class="fbfont"><%= user.getNoUpdate() %></span>
						<br/>
						<span class="fbfont">last updated: </span> 
						<span class="fbfont"><%= FBUser.format( user.getLastUpdated() ) %></span>
						<br/>
						<span class="fbfont">connection: </span> 
						<span class="fbfont"><%= user.getBefriendStatus() %></span>
					</div>					
				</div>
			</div>
		</div>
		<div style="text-align:right; margin-top:6px; width:386px;">

			<a href="javascript:window.location='main?connect=true';" 
		       	onmouseout="button_off('change_datasphere'); return true;" 
	    	   	onmouseover="button_on('change_datasphere'); return true;" >
	        	<img src="/images/change_datasphere_off.png" style="border: none;" id="change_datasphere" />
	        </a>	
<%
	if ( user.getBefriendStatus().equals( "COMPLETE" ) ) {
%>	        
		<a href="javascript:window.location='main?link=true';" 
	       	onmouseout="button_off('disconnect'); return true;" 
    	   	onmouseover="button_on('disconnect'); return true;" >
        	<img src="/images/disconnect_off.png" style="border: none;" id="disconnect" />
        </a>
<% 		
	}
	else {
%>	        
		<a href="javascript:window.location='main?link=true';" 
	       	onmouseout="button_off('reconnect'); return true;" 
    	   	onmouseover="button_on('reconnect'); return true;" >
        	<img src="/images/reconnect_off.png" style="border: none;" id="reconnect" />
        </a>
<% 	
	}
%>	        
		</div>
	</div>
	<div style="margin-left:442px;">
		<div>
			<div style="float:left; ">
				<img src="/images/icon_blue_write.png" style="width:47px; height:47px; margin-bottom:4px"/>
			</div>
			<div style="margin:12 0 0 53;">
				<span class="fbfontblue">microblog updates: </span>
				<span class="fbfont"><%= user.getNoPost() %></span> 
				<br/>
				<span class="fbfontblue">last updated: </span> 
				<span class="fbfont"><%= FBUser.format( user.getLastPost() ) %></span>
			</div>
		</div>
		<div>
			<div style="float:left;" >
				<img src="/images/icon_blue_satellite.png" style="width:47px; height:47px; margin-bottom:4px"/>
			</div>
			<div style="margin:25 0 0 53; ">
				<span class="fbfontblue">location updates: </span> 
				<span class="fbfont"><%= user.getNoCheckin() %></span> 
				<br/>
				<span class="fbfontblue">last updated: </span> 
				<span class="fbfont"><%= FBUser.format( user.getLastCheckin() ) %></span>
			</div>
		</div>
		<div>
			<div style="float:left; "><img src="/images/icon_blue_license.png"/ style="width:47px; height:47px; margin-bottom:4px"></div>
			<div style="margin:25 0 0 53;">
				<span class="fbfontblue">preferences updates: </span> 
				<span class="fbfont"><%= user.getNoLike() %></span>
				<br/>
				<span class="fbfontblue">last updated: </span> 
				<span class="fbfont"><%= FBUser.format( user.getLastLike() ) %></span>
			</div>
		</div>
			<div>
			<div style="float:left; "><img src="/images/icon_blue_camera.png" style="width:47px; height:47px; margin-bottom:4px"/></div>
			<div style="margin:25 0 0 53;">
				<span class="fbfontblue">media updates: </span> 
				<span class="fbfont"><%= user.getNoMedia() %></span>
				<br/>
				<span class="fbfontblue">last updated: </span> 
				<span class="fbfont"><%= FBUser.format( user.getLastMedia() ) %></span>
			</div>
		</div>
		<div>
			<div style="float:left; "><img src="/images/icon_blue_person.png" style="width:47px; height:47px; margin-bottom:4px"/></div>
			<div style="margin:25 0 0 53;">
				<span class="fbfontblue">biography updates: </span>
				<span class="fbfont"><%= user.getNoBio() %></span>
				<br/>
				<span class="fbfontblue">last updated: </span> 
				<span class="fbfont"><%= FBUser.format( user.getLastBio() ) %></span>
			</div>			
		</div>
	</div>
	
	
	<%@ include file="footer.jsp" %>

<%
		pm.close();
    }
%>