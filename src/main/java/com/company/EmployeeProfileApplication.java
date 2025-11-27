package com.company;

import com.company.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EmployeeProfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeProfileApplication.class, args);
	}

}
