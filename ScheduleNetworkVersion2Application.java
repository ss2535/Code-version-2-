package com.scheduleNetwork_version2.scheduleNetwork_version2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ScheduleNetworkVersion2Application {

	public static void main(String[] args) {
		SpringApplication.run(ScheduleNetworkVersion2Application.class, args);
	}

}


// http://localhost:8080/swagger-ui.html


// http://localhost:8080/v3/api-docs
// http://localhost:8080/v3/api-docs/public