package com.jc.rest.api.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.jc.rest.api.document.Customer;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

}