package br.com.gitscraping.javagitscraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class JavaGitScrapingApplication {

	@GetMapping("/")
	public String teste(){
		return "teste";
	}

	public static void main(String[] args) {
		SpringApplication.run(JavaGitScrapingApplication.class, args);
	}

}
