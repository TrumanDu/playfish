package wcc.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

import wcc.util.Constants;
import wcc.util.DataProcess;
import wcc.util.SystemUtil;
import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;

public class SystemCore {
	
	/**
	 * 是否打印失败的任务列表
	 */
	public final static boolean printFailedList = true;
	
	/**
	 * 系统核心，根据配置清除不需要的内容
	 * 注意：配置此项将会进行全文的替换，会极大的降低效率，除非必要，否则不推荐使用
	 * 
	 * @param html 		待清除的html代码
	 * @param cleans	清除列表
	 * @return String 	清除后的html代码
	 * 
	 */
	public static String clean(String html,List cleans){
		
		System.out.println("执行清理任务");
		
		String orgHtml = html;
		
		for (int i = 0; i < cleans.size(); i++) {
			
			Element currentElement = (Element)cleans.get(i);
			String type = currentElement.attributeValue(Constants.ATTR_TYPE);
			
			if(type.equals(Constants.VALUE_HEAD)){
				
				orgHtml = orgHtml.replaceFirst(".*<body>", "");
				orgHtml = orgHtml.replaceFirst("</body>.*", "");	
			}
			if(type.equals(Constants.VALUE_CSS)){
				
				orgHtml.replaceAll("<style.*?</style>", "");
			}
			if(type.equals(Constants.VALUE_SCRIPT)){
				
				orgHtml.replaceAll("<script.*?</script>", "");
			}
			if(type.equals(Constants.VALUE_TAGS)){
				
				String cleanTags = currentElement.getTextTrim();
				//clean all tags
				if(cleanTags == null || cleanTags.equals("")){
					orgHtml.replaceAll("<.*?>", "");
				}
				else{
					String [] tagList = cleanTags.split("|");
					for (int j = 0; j < tagList.length; j++) {
						orgHtml.replaceAll(tagList[i], "");
					}
				}
			}
			if(type.equals(Constants.VALUE_REGEX)){
				
				String regex = currentElement.getTextTrim();
				
				orgHtml.replaceAll(regex, "");
			}
		}
		System.out.println("清理完成");
		
		return orgHtml;
	}
	
	
	/**
	 * 系统核心功能，对html代码进行解析并存入数据库
	 * 
	 * @param html			待解析的html代码
	 * @param processes		解析任务列表
	 * @throws SQLException
	 * 
	 */
	public static void process(String html,List processes) throws SQLException{
		
		System.out.println("执行解析任务");
		
		Iterator processIterator = processes.iterator();
		
		String fields = "";
		String table = "";
		
		List<String> values = new ArrayList<String>();
		
		String orgHtml = html;
		
		while (processIterator.hasNext()) { 
			
			Element process = (Element) processIterator.next();
			/*
			 * 处理flow类型，即经过该处理后，返回的是被修整过的内容
			 */
			if(process.attributeValue(Constants.ATTR_FLOW).equals("true")){
				/*
				 * 处理 tag-filter
				 * 获得属性名值对，如果定义了多个标签
				 * 获得标签的位置
				 */
				if(SystemUtil.hasElement(process,Constants.FILTER_TAG_STR)){
					
					System.out.println("过滤：执行tag-filter");
					
					Element tagFliter = process.element(Constants.FILTER_TAG_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
					String attrKey = tagFliter.attributeValue(Constants.ATTR_KEY);
					String attrVal = tagFliter.attributeValue(Constants.ATTR_VALUE);
					String tagPos = tagFliter.attributeValue(Constants.ATTR_POS);
					
					
					//System.out.println(attrKey+":"+attrVal+":"+tagPos);
					
					int pos = 0;
					if(tagPos != null || tagPos !=""){
						pos = Integer.parseInt(tagPos)-1;
					}
					
					//System.out.println(orgHtml);
					
					Segment segment = (Segment) new Source(orgHtml);
					
					if(!attrKey.equals(Constants.VALUE_NAME)){
						orgHtml = segment.findAllElements(attrKey, attrVal, false).get(pos).toString();
					}else{
						orgHtml = segment.findAllElements(attrVal).get(pos).toString();
					}
					if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						orgHtml = new Source(orgHtml).getTextExtractor().toString();
					}
					
				}
				/*
				 * 处理regex-filter
				 * 注意，正则必须使用DOTALL格式，正常情况下得到的html代码都是多行的
				 * 如果不采用DOTALL格式，则一般只会匹配一行
				 */
				else if(SystemUtil.hasElement(process,Constants.FILTER_REGEX_STR)){
					
					System.out.println("过滤：执行regex-filter");
					
					String regex = process.elementTextTrim(Constants.FILTER_REGEX_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
			        Pattern htmlPattern = Pattern.compile(regex, Pattern.DOTALL);
			        
			        Matcher htmlMatcher = htmlPattern.matcher(orgHtml);
			        
			        while (htmlMatcher.find()) {
			        	orgHtml=htmlMatcher.group(1);
			        }
			        if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						orgHtml = new Source(orgHtml).getTextExtractor().toString();
					}
				}
				/*
				 * 处理flag-filter
				 */
				else if(SystemUtil.hasElement(process,Constants.FILTER_FLAG_STR)){
					
					System.out.println("过滤：执行flag-filter");
					
					Element flagElement = process.element(Constants.FILTER_FLAG_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
					String startFlag = flagElement.elementTextTrim(Constants.FILTER_START_FLAG);
					String endFlag = flagElement.elementTextTrim(Constants.FILTER_END_FLAG);
					
					int beginIndex = orgHtml.indexOf(startFlag);
//					终结位置的起始搜索点设置为已经获得的起始点，避免在起始点之前出现终结标志导致搜索异常
					int endIndex = orgHtml.indexOf(endFlag,beginIndex);
					
					orgHtml = orgHtml.substring(beginIndex, endIndex);
					
					if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						orgHtml = new Source(orgHtml).getTextExtractor().toString();
					}
				}
			}
			/*
			 * 处理flow为false的类型，经过该处理过程后，结果将被存储
			 */
			else{
				/*
				 * 处理 tag-filter
				 * 获得属性名值对，如果定义了多个标签
				 * 获得标签的位置
				 */
				if(SystemUtil.hasElement(process,Constants.FILTER_TAG_STR)){
					
					System.out.println("处理：执行tag-filter");
					
					Element tagFliter = process.element(Constants.FILTER_TAG_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
					String attrKey = tagFliter.attributeValue(Constants.ATTR_KEY);
					String attrVal = tagFliter.attributeValue(Constants.ATTR_VALUE);
					String tagPos = tagFliter.attributeValue(Constants.ATTR_POS);
					
					int pos = 0;
					if(tagPos != null || tagPos !=""){
						pos = Integer.parseInt(tagPos)-1;
					}
					
					Segment segment = (Segment) new Source(orgHtml);
					
					String temp ="";
					if(!attrKey.equals(Constants.VALUE_NAME)){
						temp = segment.findAllElements(attrKey, attrVal, false).get(pos).toString();
					}else{
						temp = segment.findAllElements(attrVal).get(pos).toString();
					}
					
					if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						temp = new Source(temp).getTextExtractor().toString();
					}
					
					String field = process.attributeValue(Constants.ATTR_FIELD);
					
					fields += ","+field;
					table = process.attributeValue(Constants.ATTR_TABLE);
					values.add(temp);
					
				}
				/*
				 * 处理regex-filter
				 */
				else if(SystemUtil.hasElement(process,Constants.FILTER_REGEX_STR)){
					
					System.out.println("处理：执行regex-filter");
					
					String temp="";
					
					String regex = process.elementTextTrim(Constants.FILTER_REGEX_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
			        Pattern htmlPattern = Pattern.compile(regex, Pattern.DOTALL);
			        
			        Matcher htmlMatcher = htmlPattern.matcher(orgHtml);
			        
			        while (htmlMatcher.find()) {
			        	temp=htmlMatcher.group(1);
			        }
			        
			        if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						temp = new Source(temp).getTextExtractor().toString();
					}
			        
			        String field = process.attributeValue(Constants.ATTR_FIELD);
			        
			        fields += ","+field;
			        table = process.attributeValue(Constants.ATTR_TABLE);
			        values.add(temp);
			        
				}
				/*
				 * 处理flag-filter
				 */
				else if(SystemUtil.hasElement(process,Constants.FILTER_FLAG_STR)){
					
					System.out.println("处理：执行flag-filter");
					
					Element flagElement = process.element(Constants.FILTER_FLAG_STR);
					String textonly = process.attributeValue(Constants.ATTR_TEXTONLY);
					
					String startFlag = flagElement.elementTextTrim(Constants.FILTER_START_FLAG);
					String endFlag = flagElement.elementTextTrim(Constants.FILTER_END_FLAG);
					
					int beginIndex = orgHtml.indexOf(startFlag);
					//终结位置的起始搜索点设置为已经获得的起始点，避免在起始点之前出现终结标志导致搜索异常
					int endIndex = orgHtml.indexOf(endFlag,beginIndex);
					
					String temp = orgHtml.substring(beginIndex, endIndex);
					
					if(textonly!=null && textonly.equals(Constants.VALUE_TRUE)){
						temp = new Source(temp).getTextExtractor().toString();
					}
					 
					String field = process.attributeValue(Constants.ATTR_FIELD);
				        
			        fields += ","+field;
			        table = process.attributeValue(Constants.ATTR_TABLE);
			        values.add(temp);
					
				}
			}
			System.out.println("解析完成");
		}
		
		
		
		//clear the first , in the fields
		if(fields != ""){
			fields = fields.substring(1);
		}
		//占位符生成
		String placeholder = "";
		for (int i = 0; i < values.size(); i++) {
			placeholder +=",?";
		}
		if(placeholder != ""){
			placeholder = placeholder.substring(1);
		}
		
		String query="";
		if(fields != "" && placeholder != "" && values.size()>0){
			
			query = "insert into "+table+"("+fields+") values("+placeholder+")";
			
			java.sql.PreparedStatement ps = DataProcess.getConnection().prepareStatement(query);
			for (int i = 0; i < values.size(); i++) {
				ps.setString(i+1,values.get(i));
			}
			ps.execute();
			System.out.println("保存到数据库完成");
		}
		
		
	}
	
	public static void main(String[] args) throws SQLException{
		
		long totalStartTime = System.currentTimeMillis();
		
		List missions = new ArrayList();
		if(args.length!=0){
			
			System.out.println("执行任务文件："+args[0]);
			missions= new MissionCore(args[0]).getMissions();
		}
		else{
			System.out.println("执行默认演示用任务文件");
			missions= new MissionCore().getMissions();
		}
		System.out.println("总共任务数:"+missions.size());
		
		int missionCount = 1;
		Iterator missionIterator = missions.iterator();
		while (missionIterator.hasNext()) {
			
			List<String> failedTargetList = new ArrayList<String>();
			
			long startTime = System.currentTimeMillis();
			
			System.out.println("任务"+missionCount+"执行开始...");
			
			Element mission = (Element) missionIterator.next();
			
			TargetCore targetCore = new TargetCore();
			
			String  encode = targetCore.getTargetEncode(mission);
			int timeout = targetCore.getTargetTimeout(mission);
			List targetList = targetCore.getTargetList(mission);
			
			List cleanList = new CleanCore().getCleanList(mission);
			List processList = new ProcessCore().getProcessList(mission);
			
			for(int i = 0 ; i<targetList.size();i++){
				
				String html = "";
				try {
					System.out.println("********************");
					System.out.println("开始获取目标"+i+"的内容...");
					html = SystemUtil.getContentByHttpClient((String)targetList.get(i), encode,timeout);
					System.out.println("内容抓取完成，开始进行解析...");
				
					html = SystemCore.clean(html,cleanList);
					SystemCore.process(html, processList);
					
				} catch(Exception e){
					e.printStackTrace();
					System.out.println("解析目标出错，该目标被跳过");
					failedTargetList.add((String)targetList.get(i));
					continue;
				}
			}
			
			long endTime = System.currentTimeMillis();
			System.out.println("＝＝＝＝＝＝＝执行结果报告＝＝＝＝＝＝＝");
			System.out.println("任务"+(missionCount)+"执行完成!");
			System.out.println("耗时:"+SystemUtil.getFormatTime(endTime-startTime));
			System.out.println("目标地址数目:"+(targetList.size()));
			System.out.println("获取成功数目:"+(targetList.size()-failedTargetList.size()));
			System.out.println("获取失败数目:"+(failedTargetList.size()));
			System.out.println("平均目标耗时："+SystemUtil.getFormatTime((endTime-startTime)/targetList.size()));
			if(printFailedList){
				System.out.println("失败任务列表:");
				for (int i = 0; i < failedTargetList.size(); i++) {
					System.out.println(i+"."+failedTargetList.get(i));
				}
			}
			System.out.println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
			missionCount++;
		}
		long totalEndTime = System.currentTimeMillis();
		System.out.println("******************************************");
		System.out.println("  全部任务执行完成!总计耗时："+SystemUtil.getFormatTime(totalEndTime-totalStartTime));
		System.out.println("******************************************");
		
	}
	
}
