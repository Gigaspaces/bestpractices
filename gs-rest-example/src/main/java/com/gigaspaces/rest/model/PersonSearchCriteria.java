package com.gigaspaces.rest.model;

/**
 * Form backing bean to hold search details
 */
public class PersonSearchCriteria {

	/**
	 * The free form search query
	 */
	private String query;

	/**
	 * Construct an empty criteria
	 */
	public PersonSearchCriteria() {
		this("");
	}

	/**
	 * Construct criteria with the provided query
	 * 
	 * @param query
	 */
	public PersonSearchCriteria(String query) {
		this.query = query;
	}

	/**
	 * Get the query
	 * 
	 * @return query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Set the query
	 * 
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
}