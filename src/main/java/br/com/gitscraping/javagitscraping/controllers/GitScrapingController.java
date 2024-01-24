package br.com.gitscraping.javagitscraping.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
			String git = "https://github.com/" + rep + "/tree/master";

			JsonObject jsonObject = getHTMLContent(git);

			getFiles(jsonObject);
			

			

			// System.out.println(itemObject);
			return ResponseEntity.ok("teste");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}


	private JsonObject getHTMLContent(String rep) throws Exception{
		URL url = new URI(rep).toURL();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

			String regex = "<react-partial\\s+partial-name=\"repos-overview\"[^>]*>(.*?)</react-partial>";
			Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
			Matcher matcher = pattern.matcher(response);
			String firstContent = "";
			if (matcher.find()) {
				// Obtém o conteúdo entre <tbody> e </tbody>
				firstContent = matcher.group(1);
	
			} else {
				throw new Exception("The HTML content could not be retrieved.");
			}

			regex = "<script[^>]*>(.*?)</script>";
			pattern = Pattern.compile(regex, Pattern.DOTALL);
			matcher = pattern.matcher(firstContent);
			String scriptContent = "";
			JsonObject jsonObject = new JsonObject();

			if(matcher.find()){
				scriptContent = matcher.group(1);
			}else{
				throw new Exception("The HTML content could not be retrieved.");
			}

			try {
				jsonObject = JsonParser.parseString(scriptContent).getAsJsonObject();

				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return jsonObject;
	}

	private String getFiles(JsonObject jsonObject) throws Exception{
		JsonObject treeObject = jsonObject.getAsJsonObject("props")
                                               .getAsJsonObject("initialPayload")
                                               .getAsJsonObject("tree");
			
			if (treeObject.isJsonObject() && treeObject.has("items")) {
                // Acessa o array 'items'
                JsonArray itemsArray = treeObject.getAsJsonArray("items");

                // Itera sobre os elementos do array
                for (int i = 0; i < itemsArray.size(); i++) {
                    // Acessa cada item do array como JsonObject
					if(itemsArray.get(i)
								.getAsJsonObject()
								.get("contentType")
								.getAsString()
								.equalsIgnoreCase("directory")){

						System.out.println(itemsArray.get(i).getAsJsonObject().get("contentType").getAsString());

					}
                }
            } else {
				throw new Exception("The HTML content could not be retrieved.");
            }
			return null;

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