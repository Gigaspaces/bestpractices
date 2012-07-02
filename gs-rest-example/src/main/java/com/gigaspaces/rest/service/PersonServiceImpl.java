package com.gigaspaces.rest.service;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.openspaces.core.GigaSpace;
import org.springframework.stereotype.Service;

import com.gigaspaces.rest.model.Person;
import com.google.common.collect.Lists;

@Service
public class PersonServiceImpl implements PersonService {

	@Resource
	private GigaSpace gigaSpace;

	public GigaSpace getGigaSpace() {
		return gigaSpace;
	}

	public void setGigaSpace(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}

	@Override
	public List<Person> getAllPeople() {

		Person[] results = gigaSpace.readMultiple(new Person());
		System.out.println("Found " + results.length + " People.");

		List<Person> persons = Arrays.asList(results);

		return Lists.newArrayList(persons.iterator());
	}

	@Override
	public Person getPerson(String id) {
		Person template = new Person();
		template.setId(id);

		Person p = gigaSpace.read(template);

		System.out.println("Read Person -> " + p);

		return p;
	}
}
