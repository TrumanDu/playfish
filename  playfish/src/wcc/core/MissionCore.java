package wcc.core;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MissionCore {
	
	/**
	 * 默认的任务配置文件
	 */
	private String _missionFile = "WebContent/WEB-INF/example.xml";
	
	/**
	 * 任务列表
	 */
	private List<Element> _missionList = new ArrayList<Element>();
	
	public MissionCore() {
		
	}
	
	/**
	 * @param missionFile 执行的任务文件名
	 * 同时保留默认构造跟带参构造函数，接受不同的参数
	 */
	public MissionCore(String missionFile) {
		_missionFile = "WebContent/WEB-INF/"+missionFile;
	}

	/**
	 * @return missionList 任务列表
	 * 获得任务列表
	 */
	public List<Element> getMissions(){
		
		Document document;
		
		try {
			
			SAXReader saxReader = new SAXReader();
			
			document = saxReader.read(_missionFile);
			
			_missionList = document.selectNodes("missions/mission"); 
			
		} catch (DocumentException e) {
			e.printStackTrace();
		} 
		return _missionList;
	}
	

}
