package org.openspaces.bestpractices.mirror.model;

public class PersonBuilder {
    Person person=new Person();
    public PersonBuilder id(String id) {
        person.setId(id);
        return this;
    }
    public PersonBuilder firstName(String firstName) {
        person.setFirstName(firstName);
        return this;
    }
    public PersonBuilder lastName(String lastName) {
        person.setLastName(lastName);
        return this;
    }
    public PersonBuilder creditScore(int creditScore) {
        person.setCreditScore(creditScore);
        return this;
    }
    public Person build() {
        Person p=person;
        person=null;
        return p;
    }
}
