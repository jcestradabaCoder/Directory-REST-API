package com.jc.rest.api.service;

import com.jc.rest.api.document.Customer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
	
	//Observable
	public Flux<Customer> findAll();
	public Mono<Customer> findById(String customerId);
	public Mono<Customer> saveCustomer(Customer customer);
	public Mono<Void> deleteCustomer(Customer customer);
}