<%@ page language="java" %>
<%@ page import="chant.MainServlet" %>
<html>
	<head>
       	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
    	<title>MyDataSphere</title>
		<link href="main.css" rel="stylesheet" type="text/css">
    	<script type="text/javascript" src="jquery-1.4.2.js"></script>   
		<script>
			
			//-- preload required images
			login_on = new Image();
			login_on.src = "/images/login_on.png";
			login_off = new Image();
			login_off.src = "/images/login_off.png";
		
			change_datasphere_on = new Image();
			change_datasphere_on.src = "/images/change_datasphere_on.png";
			change_datasphere_off = new Image();
			change_datasphere_off.src = "/images/change_datasphere_off.png";
			
			disconnect_on = new Image();
			disconnect_on.src = "/images/disconnect_on.png";
			disconnect_off = new Image();
			disconnect_off.src = "/images/disconnect_off.png";
			
			reconnect_on = new Image();
			reconnect_on.src = "/images/reconnect_on.png";
			reconnect_off = new Image();
			reconnect_off.src = "/images/reconnect_off.png";
			
			connect_on = new Image();
			connect_on.src = "/images/connect_on.png";
			connect_off = new Image();
			connect_off.src = "/images/connect_off.png";
			
			skip_on = new Image();
			skip_on = "/images/connect_off.png";
			skip_off = new Image();
			skip_off.src = "/images/connect_on.png";
					
			continue_on = new Image();
			continue_on.src = "/images/continue_on.png";
			continue_off = new Image();
			continue_off.src = "/images/continue_off.png";
			
			retry_on = new Image();
			retry_on.src = "/images/retry_on.png";
			retry_off = new Image();
			retry_off.src = "/images/retry_off.png";
			
			ajax_loading = new Image();
			ajax_loading.src = "/images/ajax-loading.gif";
			
			ajax_success = new Image();
			ajax_success.src = "/images/ajax-success.gif";
			
			ajax_failure = new Image();
			ajax_failure.src = "/images/ajax-failure.gif";
			
			function button_on ( imgId ) {
	  			if ( document.images ) {
	    			butOn =  "/images/" + imgId + "_on.png" ;
	    			document.getElementById( imgId ).src = butOn;
	  			}
			}
	
			function button_off ( imgId ) {
	  			if ( document.images ) {
	    			butOff = "/images/" + imgId + "_off.png";
	    			document.getElementById( imgId ).src = butOff;
	  			}
			}
		</script>
	</head>
	
	<body>
    <div id="bgbox">
    	<div id="top"></div>
		<div id="content">