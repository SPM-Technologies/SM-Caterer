package com.smtech.SM_Caterer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SmCatererApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmCatererApplication.class, args);
	}

}
