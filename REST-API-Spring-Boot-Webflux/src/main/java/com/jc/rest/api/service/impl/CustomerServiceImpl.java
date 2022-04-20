package com.jc.rest.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jc.rest.api.document.Customer;
import com.jc.rest.api.repository.CustomerRepository;
import com.jc.rest.api.service.CustomerService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired
	private CustomerRepository customerRepository;

	
	@Override
	public Flux<Customer> findAll() {
		return customerRepository.findAll();
	}

	@Override
	public Mono<Customer> findById(String customerId) {
		return customerRepository.findById(customerId);
	}

	@Override
	public Mono<Customer> saveCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	@Override
	public Mono<Void> deleteCustomer(Customer customer) {
		
		return customerRepository.delete(customer);
	}
}