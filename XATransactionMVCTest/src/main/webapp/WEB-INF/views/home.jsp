<%@page import="com.tmax.test.TiberoXARun"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
 <%
    TiberoXARun tbRun = new TiberoXARun();
    tbRun.main();
 %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello world!  
</h1>

</body>
</html>