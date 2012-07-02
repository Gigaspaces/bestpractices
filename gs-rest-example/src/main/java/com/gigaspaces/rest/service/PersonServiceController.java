package com.gigaspaces.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.gigaspaces.rest.model.Person;
import com.j_spaces.core.IJSpace;

@Controller
public class PersonServiceController {

	@Autowired
	PersonService personService;

	@RequestMapping(value = "/persons/*")
	public ModelAndView getAllPersons() {
		System.out.println("in getAllPeople");
		List<Person> people = personService.getAllPeople();
		ModelAndView mav = new ModelAndView("personView", "people", people);
		return mav;
	}

	@RequestMapping(value = "/person/{id}")
	public ModelAndView getPerson(@PathVariable String id) {
		System.out.println("in getPerson");
		Person person = personService.getPerson(id);
		List<Person> people = new ArrayList<Person>();
		people.add(person);
		ModelAndView mav = new ModelAndView("personView", "people", people);
		return mav;
	}

}