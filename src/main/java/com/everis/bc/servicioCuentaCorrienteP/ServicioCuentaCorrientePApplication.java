package com.everis.bc.servicioCuentaCorrienteP;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
@EnableEurekaClient
@SpringBootApplication
public class ServicioCuentaCorrientePApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioCuentaCorrientePApplication.class, args);
	}

}
