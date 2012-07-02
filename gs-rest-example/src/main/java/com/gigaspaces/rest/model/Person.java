/**
 * 
 */
package com.gigaspaces.rest.model;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("person")
public class Person implements Serializable {
	private String firstName;

	private String lastName;

	private Integer age;

	private String id;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@SpaceId(autoGenerate = false)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		return "Id[" + id + "] First Name[" + firstName + "] Last Name["
				+ lastName + "] Age[" + age + "]";
	}
}
