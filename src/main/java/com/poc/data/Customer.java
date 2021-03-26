package com.poc.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String firstName;
    private String lastName;
    private String realm;

    protected Customer() {}

    public Customer(String firstName, String lastName, String realm) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.realm = realm;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s', realm='%s']",
                id, firstName, lastName, realm);
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRealm() {
        return realm;
    }
}
