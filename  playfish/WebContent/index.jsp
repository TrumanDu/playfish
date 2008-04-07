<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.sql.*,java.util.*,wcc.core.*,wcc.util.*" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
</head>
<body>
<%
	String query="select * from bbs";
	Connection con = DataProcess.getConnection();
	try {			
		Statement stmt = con.createStatement();
		ResultSet rst= stmt.executeQuery(query);	
		int i = 1;
		while(rst.next()){			
			out.println("<h1>截取内容："+(i++)+"</h1>");
			out.println(rst.getString("title"));
			out.println("作者:"+rst.getString("author"));
			out.println(" 时间："+rst.getString("posttime"));
			out.println(rst.getString("content"));
		}
		rst.close();
		stmt.close();
		con.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}	

%>
</body>
</html>