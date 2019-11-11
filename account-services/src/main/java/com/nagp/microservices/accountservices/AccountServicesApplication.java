package com.nagp.microservices.accountservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.nagp.microservices.accountservices")
@SpringBootApplication
@EnableDiscoveryClient
@EnableHystrix
public class AccountServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServicesApplication.class, args);
	}

}
