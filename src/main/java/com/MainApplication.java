package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
	//放在com包下才能扫描到两个包的action
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
	
}
