package com.senior.g5.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;

public class Tests {

	public static void main(String[] args) {
		HashMap<String, G5NodeParam> params = new HashMap<String, G5NodeParam>();
		String stringParameters = "{\"obsPrm\":\"tetes\",\"datFim\":\"2018-03-19\",\"datIni\":\"2018-03-19\",\"workflow_user\":\"robson.hostin\",\"colaborador\":\"1/1-1904-Robson Hostin\"}";
		JSONObject jsonParameter = new JSONObject(stringParameters .toString());	
		System.out.println("JSON Parameters: "+jsonParameter);
		String[] atributeNames = JSONObject.getNames(jsonParameter);
		for (String param : atributeNames) {
			Object objValue = jsonParameter.get(param);
			if (objValue instanceof JSONObject) {//é um objeto
				JSONObject jsonObj = (JSONObject) objValue;				
				G5NodeParam node = new G5NodeParam(param);
				final String[] subNames = JSONObject.getNames(jsonObj);
				if(subNames.length>0){
					for (String subParam : subNames) {
						Object subObjValue = jsonObj.get(subParam);
						String subStrValue = parseObjectValue(subParam, subObjValue);
						node.addChild(new G5NodeParam(subParam, subStrValue));
					}					
				}
				params.put(param, node);
			} else {
				String strValue = parseObjectValue(param, objValue);
				G5NodeParam node = new G5NodeParam(param, strValue);
				params.put(param, node);
			}
		}		
		Set<Entry<String, G5NodeParam>> set = params.entrySet();
		System.out.println("PRINTING...");
		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			Entry<String, G5NodeParam> entry = (Entry<String, G5NodeParam>) iterator.next();
			printValue(entry.getValue());
			
		}
	}
	
	private static void printValue(G5NodeParam p){
		String k = p.getName();
		List<G5NodeParam> c = p.getChildren();
		if(c != null){
			for (Iterator iterator = c.iterator(); iterator.hasNext();) {
				G5NodeParam g5NodeParam = (G5NodeParam) iterator.next();
				System.out.println(p.getName()+"->");
				printValue(g5NodeParam);				
			}
		}else{
			System.out.println(k+":"+p.getValue());
		}
	}
	
	private static String parseObjectValue(String name, Object objValue) {
		String strValue = "";
		if (objValue instanceof Integer || objValue instanceof Long) {
			strValue = objValue.toString();
		} else if (objValue instanceof Boolean) {
			strValue = ((Boolean) objValue).booleanValue() == true ? "1" : "0";
		} else if (objValue instanceof Float || objValue instanceof Double) {
			strValue = ((Number) objValue).toString();
		} else if (JSONObject.NULL.equals(objValue)) {
			strValue = "";
		} else {
			strValue = objValue.toString();
		}
		return strValue;
	}
}
