package wcc.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.dom4j.Element;

public class SystemUtil {

    /**
     * 通过httpclient方法获得网页内容
     * 
     * @param url 目标的URL
     * @return 页面内容字符串
     * @throws IOException
     * 
     */
    public static String getContentByHttpClient(String url,String encode,int timeout) throws IOException {
    	
    	HttpClient client = new HttpClient();
    	
        GetMethod method = new GetMethod(url);
        
        //设置失败后默认的重试次数为3
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler(3, false));
       
        //设置失败延时为5000ms，即5秒
        method.getParams().setIntParameter(HttpMethodParams.SO_TIMEOUT, timeout);
       
        ByteArrayOutputStream outstream = null;
       
        BufferedInputStream bufferedInputStream = null;
       
        try {

        	int statusCode = client.executeMethod(method);
        	
            if (statusCode != HttpStatus.SC_OK) {
            	
                System.out.println("方法失败: " + method.getStatusLine());
            }
            //缓冲读取的内容4k，提高性能
            bufferedInputStream = new BufferedInputStream(method.getResponseBodyAsStream());
            
            outstream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = bufferedInputStream.read(buffer)) > 0) {
            	
                outstream.write(buffer, 0, len);
            }
            
            outstream.flush();
            
            byte[] responseBody = outstream.toByteArray();
            
            return new String(responseBody,encode);
            
        } catch (HttpException e) {
        	
        	System.out.println("致命协议错误：" + e.getMessage());
            throw e;
        } catch (IOException e) {

        	System.out.println("致命传输错误：" + e.getMessage());
            throw e;
        } finally {
            if (bufferedInputStream != null) {
            	
                bufferedInputStream.close();
            }
            if (outstream != null) {
            	
                outstream.close();
            }
            //注意，连接在完成后必须释放
            method.releaseConnection();
        }
    }
	
	
	/**
	 * 判断某个元素是否包含有某个子元素的全局共用方法
	 * 
	 * @param parentElement 父元素
	 * @param childElementName 子元素名称
	 * @return 真或者假
	 * 
	 */
	public static boolean hasElement(Element parentElement,String childElementName)
	{
		
		boolean hasElement = false;
		
		Element chilaElement = parentElement.element(childElementName);
		
		if(chilaElement != null)
			hasElement = true;
		
		return hasElement;
	}
    /**
     * 将毫秒转换成小时：分钟：秒的格式
     * 
     * @param time
     * @return 格式化后的时间格式
     * 
     */
    public static String getFormatTime(long time) {
    	
        int hour = 0;
        int minute = 0;
        int second = 0;

        second = new Long(time).intValue() / 1000;

        if (second > 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        return String.valueOf(hour)+ "小时" + String.valueOf(minute)  + "分钟"
                + String.valueOf(second) + "秒";
    }
	
}
