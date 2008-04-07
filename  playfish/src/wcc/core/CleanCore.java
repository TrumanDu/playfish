package wcc.core;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import wcc.util.Constants;

public class CleanCore {
	
	private List<Element> _cleanList = new ArrayList<Element>();
	
	/**
	 * 读取xml配置文件中的clean列表
	 * 
	 * @param mission
	 * @return cleanList 要进行清除的任务列表
	 * 
	 */
	public List<Element> getCleanList(Element mission){
		
		List cleanList = mission.element(Constants.MISSION_CLEANER).elements(Constants.CLEANER_CLEAN);
		
		_cleanList.addAll(cleanList);	
		
		return _cleanList;
	}
}
