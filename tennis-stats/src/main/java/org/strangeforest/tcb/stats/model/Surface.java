package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

public enum Surface implements CodedEnum {

	HARD("H", "Hard"),
	CLAY("C", "Clay"),
	GRASS("G", "Grass"),
	CARPET("P", "Carpet");

	private final String code;
	private final String text;

	Surface(String code, String text) {
		this.code = code;
		this.text = text;
	}

	@Override public String getCode() {
		return code;
	}

	@Override public String getText() {
		return text;
	}

	public static Map<String, String> asMap() {
		return CodedEnum.asMap(Surface.class);
	}
}