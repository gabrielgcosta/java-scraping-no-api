package br.com.gitscraping.javagitscraping.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gitscraping.javagitscraping.classes.GitInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import org.springframework.web.bind.annotation.RequestParam;



/**
 * GitScrapingController
 */
@RestController
public class GitScrapingController {
    @GetMapping("/git-info")
	public ResponseEntity get(String rep) {
		try {
			String git = "https://github.com/" + rep;
			URL url = new URI(git).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

			String regex = "<tbody>(.*?)</tbody>";
			Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
			Matcher matcher = pattern.matcher(response);
			if (matcher.find()) {
				// Obtém o conteúdo entre <tbody> e </tbody>
				String conteudoTbody = matcher.group(1);
	
				// Imprime o conteúdo
				System.out.println("Conteúdo entre <tbody> e </tbody>: " + conteudoTbody);
			} else {
				System.out.println("Nenhuma correspondência encontrada.");
			}
			
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/teste")
	public ResponseEntity teste() {
		String inputText = "o padre roeu a roupa do rei e roeu o sapato também";

      	// Define a expressão regular para encontrar o texto entre 'roeu' e 'roeu'
      	String regex = "<tbody>\\s(.*?)\\s</tbody>";

      	// Compila a expressão regular em um padrão
      	Pattern pattern = Pattern.compile(regex);

      	// Cria um objeto Matcher para a entrada
      	Matcher matcher = pattern.matcher(inputText);

      	// Enquanto houver correspondências na entrada
      	while (matcher.find()) {
      	    // Imprime o texto encontrado entre 'roeu' e 'roeu'
      	    System.out.println("Texto encontrado: " + matcher.group(1));
      	}
		return ResponseEntity.ok("foi");

	}
	
}