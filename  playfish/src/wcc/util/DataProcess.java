package wcc.util;

import java.sql.*;

public class DataProcess {
	
	/**
	 * 取得数据库链接
	 * 
	 * @return 数据库链接
	 * 
	 */
	public static Connection getConnection()
	{	
		Connection con;
		
		try
		{
			Class.forName(Config.CONNECTION_DRIVER);
			con = DriverManager.getConnection(Config.CONNECTION_URL,Config.CONNECTION_USER,Config.CONNECTION_PWD);			
			return con;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 执行查询语句的通用方法
	 * 
	 * @param query sql查询语句
	 */
	public static void ExeQuery(String query)
	{
		Connection con=getConnection();
		try
		{
			Statement stmt=con.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
			con.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}


