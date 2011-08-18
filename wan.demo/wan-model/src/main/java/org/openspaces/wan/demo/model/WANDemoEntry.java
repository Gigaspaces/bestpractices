package org.openspaces.wan.demo.model;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceProperty;

@SpaceClass
public class WANDemoEntry implements Comparable<WANDemoEntry> {
	String id;
	String name;
	Double value;

	public WANDemoEntry() {
	}

	@SpaceId(autoGenerate = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@SpaceProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SpaceProperty
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WANDemoEntry [id=").append(id).append(", name=")
				.append(name).append(", value=").append(value).append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(WANDemoEntry o) {
		return getName().compareTo(o.getName());
	}

}
