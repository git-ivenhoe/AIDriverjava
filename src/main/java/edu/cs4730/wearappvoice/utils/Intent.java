package edu.cs4730.wearappvoice.utils;

import edu.cs4730.wearappvoice.voice.views.DataProcessView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Intent implements Serializable {

	private String action;
	private String targetClass; // 可选：用于广播或跳转的目标类名
	private final Map<String, Object> extras = new HashMap<>();

	public Intent() {
	}

	public Intent(String action) {
		this.action = action;
	}

	public Intent(Context mContext, Class<?> targetClass) {
		this.targetClass = targetClass.getName();
	}


	public String getAction() {
		return action;
	}

	public Intent setAction(String action) {
		this.action = action;
		return this;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public Intent setTargetClass(Class<?> cls) {
		this.targetClass = cls.getName();
		return this;
	}

	public Intent putExtra(String key, Object value) {
		extras.put(key, value);
		return this;
	}

	public Object getExtra(String key) {
		return extras.get(key);
	}

	public String getStringExtra(String key) {
		Object val = extras.get(key);
		return val instanceof String ? (String) val : null;
	}

	public int getIntExtra(String key, int defaultValue) {
		Object val = extras.get(key);
		return val instanceof Integer ? (Integer) val : defaultValue;
	}

	public boolean getBooleanExtra(String key, boolean defaultValue) {
		Object val = extras.get(key);
		return val instanceof Boolean ? (Boolean) val : defaultValue;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	@Override
	public String toString() {
		return "Intent{" +
				"action='" + action + '\'' +
				", targetClass='" + targetClass + '\'' +
				", extras=" + extras +
				'}';
	}
}
