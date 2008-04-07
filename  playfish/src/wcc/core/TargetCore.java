package wcc.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

import wcc.util.Constants;
import wcc.util.SystemUtil;

/**
 * @author Administrator
 *
 */
public class TargetCore {
	
	/**
	 * 任务列表
	 */
	private List<String> _targetList = new ArrayList<String>();
	
	/**
	 * 默认编码
	 */
	private String _encode = "UTF-8";
	
	/**
	 * 默认传输超时时限
	 */
	private int _timeout = 5000;
	
	/**
	 * 从任务选项中获得目标网址列表并返回
	 * 判断是哪种类型的目标列表，并添加到目标列表中
	 * 
	 * @param mission 任务
	 * @return targetList 任务列表
	 * 
	 */
	public List getTargetList(Element mission){
		
		Element target = mission.element(Constants.MISSION_TARGET);
		
		System.out.println("开始获取目标地址列表...");
		
		if(SystemUtil.hasElement(target,Constants.TARGET_MULTIURL))
		{
			getMultiUrl(target);
		}
		if(SystemUtil.hasElement(target,Constants.TARGET_WILDCARDURL))
		{
			getWildcardUrl(target);
		}
		
		System.out.println("目标列表总数："+_targetList.size());
		
/*		for(int i = 0;i<_targetList.size();i++){
			
			System.out.println(i+":"+_targetList.get(i));
	
		}*/
		
		System.out.println("获取目标列表完成");
		
		return _targetList;
	}
	
	/**
	 * 获得指定的页面编码，如果没有指定该参数，默认为UTF-8
	 * 
	 * @param mission 任务
	 * @return	targetEncode 任务页面的页面编码
	 * 
	 */
	public String getTargetEncode(Element mission){
		
		Element target = mission.element(Constants.MISSION_TARGET);
		
		if(target.attributeValue(Constants.ATTR_ENCODE)!=null && !target.attributeValue(Constants.ATTR_ENCODE).equals("")){
			
			_encode = target.attributeValue(Constants.ATTR_ENCODE);
		}
		
		return _encode;
	}
	
	/**
	 * 获得任务目标页面的传输超时设置，默认为5000ms
	 * 
	 * @param mission 任务
	 * @return	targetTimeout 任务目标页面的传输超时设置
	 */
	public int getTargetTimeout(Element mission){
		
		Element target = mission.element(Constants.MISSION_TARGET);
		
		if(target.attributeValue(Constants.ATTR_TIMEOUT)!=null && !target.attributeValue(Constants.ATTR_TIMEOUT).equals("")){
			
			_timeout = Integer.parseInt(target.attributeValue(Constants.ATTR_TIMEOUT));
		}
		
		return _timeout;
	}
	
	/**
	 * 多URL模式下，将解析得到的任务列表添加到总的任务列表中
	 * 
	 * @param parentElement 父元素，一般为XML文件中的TARGET元素
	 * 
	 */
	public void getMultiUrl(Element parentElement){
		
		Element multiUrl = parentElement.element(Constants.TARGET_MULTIURL);
		
		for(Iterator i = multiUrl.elementIterator(Constants.TARGET_SINGLEURL);i.hasNext();)
		{
			if(!SystemUtil.hasElement(parentElement, Constants.TARGET_REGEX)){
				
				Element temp = (Element)i.next();
				
				_targetList.add(temp.attributeValue(Constants.ATTR_HREF));
			}
			else{
				
				Element temp = (Element)i.next();
				
				String regex = parentElement.elementTextTrim(Constants.TARGET_REGEX);
				String root = parentElement.element(Constants.TARGET_REGEX).attributeValue(Constants.ATTR_ROOT);
				
				_targetList.addAll(getTargetByRegex(temp.attributeValue(Constants.ATTR_HREF),root,regex));
			}
		}
	}
	
	/**
	 * 通配符url模式下，将通配符替换成数字，并将解析得到的任务列表添加到总的任务列表中
	 * 注意：暂时只支持(*)替换成数字
	 * 
	 * @param parentElement
	 */
	public void getWildcardUrl(Element parentElement){
		
		Element wildcardUrl = parentElement.element(Constants.TARGET_WILDCARDURL);
		
		int startPos = Integer.parseInt(wildcardUrl.attributeValue(Constants.ATTR_STARTPOS));
		int endPos = Integer.parseInt(wildcardUrl.attributeValue(Constants.ATTR_ENDPOS));
		String href = wildcardUrl.attributeValue(Constants.ATTR_HREF);
		
		List<String> targetList = new ArrayList<String>();
		
		for(int i = startPos;i<=endPos;i++){
			
			String temp = href;
			
			temp = temp.replace(Constants.WILDCARD_STAR, String.valueOf(i));
			
			targetList.add(temp);
		}
		
		if(!SystemUtil.hasElement(parentElement, Constants.TARGET_REGEX)){
			
			_targetList.addAll(targetList);
		}
		else{
			
			String regex = parentElement.elementTextTrim(Constants.TARGET_REGEX);
			String root = parentElement.element(Constants.TARGET_REGEX).attributeValue(Constants.ATTR_ROOT);
			
			for(int i = 0; i<targetList.size(); i++){
				
				_targetList.addAll(getTargetByRegex(targetList.get(i),root,regex));
				
			}
		}
	}
	
	/**
	 * 获得html内容，并通过正则表达式获得链接列表
	 * 将链接加上root属性的值
	 * 
	 * @param url 	目标列表的获取地址
	 * @param root 	目标列表的根，针对相对URL
	 * @param regex	获取目标地址的正则表达式
	 * @return	目标列表
	 * 
	 */
	public List<String> getTargetByRegex(String url,String root,String regex) {
		
		String targetUrlContent = "";
		try {
			targetUrlContent = SystemUtil.getContentByHttpClient(url, _encode, _timeout);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        List<String> targetList = new ArrayList<String>();
        
        Pattern targetPattern = Pattern.compile(regex, Pattern.DOTALL);
        
        Matcher targetMatcher = targetPattern.matcher(targetUrlContent);
        
        while (targetMatcher.find()) {
        	targetList.add(root+targetMatcher.group(1));
        }
        
        return targetList;
    }
}
