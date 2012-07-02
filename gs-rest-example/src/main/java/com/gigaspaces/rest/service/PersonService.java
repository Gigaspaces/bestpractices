package com.gigaspaces.rest.service;

import java.util.List;

import com.gigaspaces.rest.model.Person;

public interface PersonService {

	public List<Person> getAllPeople();
	public Person getPerson(String id);
}
