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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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

	private List<GitInfo> gitInfos = new ArrayList<>();

    @GetMapping("/git-info")
	public ResponseEntity get(String rep) {
		try {
			String git = "https://github.com/" + rep + "/tree/master/";

			StringBuilder response = getHTMLResponse(git);

			JsonObject jsonObject = getHTMLContent(response);

			getFiles(jsonObject, git, "directory", null);

			for(int i = 0; i < gitInfos.size() -1; i++){
				System.out.println(gitInfos.get(i).getBytes());
				System.out.println(gitInfos.get(i).getCount());
				System.out.println(gitInfos.get(i).getExtension());
				System.out.println(gitInfos.get(i).getLines());
				System.out.println('\n');
			}

			

			// System.out.println(itemObject);
			return ResponseEntity.ok("teste");
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}


	private StringBuilder getHTMLResponse(String rep) throws IOException, URISyntaxException{
		URL url = new URI(rep).toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response;
	}

	private JsonObject getHTMLContent(StringBuilder response) throws Exception{
		
		String regex = "<react-partial\\s+partial-name=\"repos-overview\"[^>]*>(.*?)</react-partial>";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(response);
		String firstContent = "";
		if (matcher.find()) {
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

	private String getFiles(JsonObject jsonObject, String git, String type, String fileExtension) throws Exception{
		if(jsonObject.isJsonObject() && jsonObject.has("props")){
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

						String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
						String path = git + fileName;
						StringBuilder response = getHTMLResponse(path);
						JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
						getFiles(jsonResponse, git, "directory", null);
						// System.out.println(path);
						// System.out.println(itemsArray.get(i).getAsJsonObject().get("contentType").getAsString());

					}else{
						String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
						String path = git + fileName;
						StringBuilder response = getHTMLResponse(path);
						JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
						getFiles(jsonResponse, git, "file", fileName.substring(fileName.lastIndexOf('.') + 1));
						// System.out.println(path);
						// System.out.println(jsonResponse);
					}
                }
            } else {
				throw new Exception("The HTML content could not be retrieved.");
            }
		}else if(jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("directory")){
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
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
						
						String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
						String path = git + fileName;
						StringBuilder response = getHTMLResponse(path);
						JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
						getFiles(jsonResponse, git, "directory", null);

						// String path = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
						// System.out.println(path);
						// System.out.println(itemsArray.get(i).getAsJsonObject().get("name").getAsString());

					}else{
						String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
						String path = git + fileName;
						StringBuilder response = getHTMLResponse(path);
						JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
						getFiles(jsonResponse, git, "file", fileName.substring(fileName.lastIndexOf('.') + 1));
					}
				}
			}
		}else if(jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("file")){
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
                                               .getAsJsonObject("blob")
											   .getAsJsonObject("headerInfo");
			int lines = 0;
			if(!(treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").isJsonNull())){
				lines = treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").getAsInt();
			}
			String size = treeObject.get("blobSize").getAsString();
			int bytes;
			if(size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("kb")){
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Math.round(Float.parseFloat(size) * 1024);
			}else if(size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("gb")){
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Math.round(Float.parseFloat(size) * (1024*1024*1024));
			}else{
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Integer.parseInt(size);
			}
			GitInfo gitInfo = new GitInfo(fileExtension, 1, lines, bytes);
			gitInfos.add(gitInfo);
		}
		
		return null;

	}

	@GetMapping("/teste")
	public ResponseEntity teste() {
		String minhaString = "681 Bytes";

        // Encontrando a posição do último ponto

		String resultado = minhaString.substring(0, minhaString.lastIndexOf(' '));
        // Verificando se o ponto foi encontrado

        System.out.println("Resultado: " + resultado);
		return null;

	}
	
}