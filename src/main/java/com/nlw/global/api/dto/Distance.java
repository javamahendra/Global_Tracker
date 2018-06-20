package com.nlw.global.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Distance {
	private String text;
	private long value;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

}
