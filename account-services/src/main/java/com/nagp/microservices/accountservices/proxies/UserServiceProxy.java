package com.nagp.microservices.accountservices.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Component
@FeignClient(name = "zuul-api-gateway")
public interface UserServiceProxy {

	@PostMapping(path = "/user-services/addAccount/{userID}/{accountNo}", consumes = "application/json", produces = "application/json")
	public String addAccount(@PathVariable String userID, @PathVariable String accountNo);

	@PostMapping(path = "/user-services/removeAccount/{userID}/{accountNo}", consumes = "application/json", produces = "application/json")
	public String removeAccount(@PathVariable String userID, @PathVariable String accountNo);

}
