package com.gigaspaces.rest.tests;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gigaspaces.rest.model.Person;
import com.gigaspaces.rest.service.PersonServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/rest-test-context.xml")
public class PersonServiceTest {

	@Autowired
	GigaSpace gigaSpace;

	PersonServiceImpl service;

	int count = 10;

	@Before
	public void after() {
		clearSpaces();
		System.out
				.println("===================================================");
		// Load dummy data
		for (int i = 0; i < count; i++) {
			Person p = new Person();
			p.setId("" + i);
			p.setFirstName("John " + i);
			p.setLastName("Doe");
			p.setAge(30);

			gigaSpace.write(p);
		}

		service = new PersonServiceImpl();
		service.setGigaSpace(gigaSpace);
	}

	@Test
	public void testGetPerson() {
		Person p = service.getPerson("1");

		Assert.assertTrue(p.getId().equals("1"));
	}

	@Test
	public void testGetAllPeople() {
		List<Person> people = service.getAllPeople();

		Assert.assertEquals(10, people.size());
	}

	@After
	public void before() {
		clearSpaces();
	}

	private void clearSpaces() {
		gigaSpace.clear(null);
	}

}
