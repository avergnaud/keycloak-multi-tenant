package com.poc.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Override
    List<Customer> findAll();

    List<Customer> findByRealm(String realm);

    Customer findById(long id);
}
