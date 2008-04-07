
package wcc.util;
import java.io.*;
import java.util.Properties;
public class Config {
	/**
	 * 系统允许所使用的数据库文件配置
	 */
	private static Properties prop=new Properties();
	static{
		try{
			prop.load(Config.class.getResourceAsStream("dbConfig.txt"));
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static String CONNECTION_TYPE=prop.getProperty("conn_type");
	public static String CONNECTION_URL=prop.getProperty("conn_url");
	public static String CONNECTION_USER=prop.getProperty("conn_user");
	public static String CONNECTION_PWD=prop.getProperty("conn_pwd");
	public static String CONNECTION_DRIVER=prop.getProperty("conn_driver");
	
	public static int PAGE_SIZE=Integer.parseInt(prop.getProperty("page_size"));
	public static String REQUEST_ENCODE=prop.getProperty("request_encode");
}
