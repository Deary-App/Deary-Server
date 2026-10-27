package com.deary.server;

import org.springframework.boot.SpringApplication;

public class TestDearyServerApplication {

	public static void main(String[] args) {
		SpringApplication.from(DearyServerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
