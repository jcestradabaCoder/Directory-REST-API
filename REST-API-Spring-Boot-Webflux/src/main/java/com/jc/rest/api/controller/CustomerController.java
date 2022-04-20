package com.jc.rest.api.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.jc.rest.api.document.Customer;
import com.jc.rest.api.service.CustomerService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CustomerController {
	
	@Autowired
	private CustomerService customerService;
	
	@Value("${config.uploads.path}")
	private String path;
	
	
	@PostMapping("/customers/registerCustomerWithPhoto")
	public Mono<ResponseEntity<Customer>> createCliente(Customer customer, @RequestPart FilePart file) {
		customer.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
			.replace(" ", "")
			.replace(":", "")
			.replace("//", ""));
		
		return file.transferTo(new File(path + customer.getPhoto())).then(customerService.saveCustomer(customer))
				.map(c -> ResponseEntity.created(URI.create("/api/customers/".concat(c.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(c)
				);
	}
	
	@PostMapping("/customers/uploadPhoto/{customerId}")
	public Mono<ResponseEntity<Customer>> uploadPhoto(@PathVariable(name = "customerId") String customerId, @RequestPart FilePart file) {
		return customerService.findById(customerId).flatMap( c -> {
			c.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
			.replace(" ", "")
			.replace(":", "")
			.replace("//", ""));
			
			return file.transferTo(new File(path + c.getPhoto())).then(customerService.saveCustomer(c));
		}).map(c -> ResponseEntity.ok(c)).defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping("/customers")
	public Mono<ResponseEntity<Map<String, Object>>> createCustomer(@Valid @RequestBody Mono<Customer> monoCustomer) {
		Map<String, Object> resp = new HashMap<>();
		
		return monoCustomer.flatMap(customer -> {
			return customerService.saveCustomer(customer).map(c -> {
				resp.put("cliente", c);
				resp.put("mensaje", "Customer created successfully!");
				resp.put("timestamp", new Date());
				return ResponseEntity
						.created(URI.create("/api/customers/".concat(c.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(resp);
			});
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class)
					.flatMap(err -> Mono.just(err.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "Field : " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(errLst -> {
						resp.put("errors", errLst);
						resp.put("timestamp", new Date());
						resp.put("status", HttpStatus.BAD_REQUEST.value());
						
						return Mono.just(ResponseEntity.badRequest().body(resp));
					});
			});
	}
	
	@GetMapping("/customers")
	public Mono<ResponseEntity<Flux<Customer>>> getAllCustomers() {
		return Mono.just(
					ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(customerService.findAll())
		);
	}
	
	@GetMapping("/customers/{customerId}")
	public Mono<ResponseEntity<Customer>> getCustomerById(@PathVariable(name = "customerId") String customerId) {
		return customerService.findById(customerId).map(c -> ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(c)
				).defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PutMapping("/customers/{customerId}")
	public Mono<ResponseEntity<Customer>> updateCustomer(@PathVariable(name = "customerId") String customerId, @Valid @RequestBody Customer customer) {
		return customerService.findById(customerId).flatMap(c -> {
			c.setName(customer.getName());
			c.setSurname(customer.getSurname());
			c.setAge(customer.getAge());
			c.setSalary(customer.getSalary());
			
			return customerService.saveCustomer(c);
		}).map(c -> ResponseEntity.created(URI.create("/api/customers/".concat(c.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(c))
		  .defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/customers/{customerId}")
	public Mono<ResponseEntity<Void>> deleteCustomer(@PathVariable(name = "customerId") String customerId) {
		return customerService.findById(customerId).flatMap(c -> {
			return customerService.deleteCustomer(c).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}