package com.nexcloud.fullfillment.k8s.domain;

import java.util.HashMap;
import java.util.Map;

public class Namespace {
	private Map<String, String> map;

	public Map<String, String> getMap() {
		if( map == null )
			map	= new HashMap<String, String>();
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
