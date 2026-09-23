package com.codeassess.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@org.springframework.scheduling.annotation.EnableScheduling
public class CodeassessBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeassessBackendApplication.class, args);
	}

}
