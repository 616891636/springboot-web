package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

//@SpringBootApplication
//public class MainApplication {
//	//放在com包下才能扫描到两个包的action
//	public static void main(String[] args) {
//		SpringApplication.run(MainApplication.class, args);
//	}
//
//}

@SpringBootApplication
public class MainApplication  extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MainApplication.class);
	}
	//放在com包下才能扫描到两个包的action
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

}


