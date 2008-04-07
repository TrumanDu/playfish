package wcc.core;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import wcc.util.Constants;

public class ProcessCore {
	
	private List<Element> _processList = new ArrayList<Element>();
	
	/**
	 * 返回代处理的列表
	 * @param mission
	 * @return processList 处理列表
	 * 
	 */
	public List<Element> getProcessList(Element mission){
		
		List processList = mission.element(Constants.MISSION_PROCESSER).elements(Constants.PROCESSER_PROCESS);
		
		_processList.addAll(processList);	
		
		return _processList;
	}
}
