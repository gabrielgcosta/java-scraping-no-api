package br.com.gitscraping.javagitscraping.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

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
import java.util.concurrent.TimeUnit;

/**
 * GitScrapingController
 */
@RestController
public class GitScrapingController {

	private static final Cache<String, Object> cache = Caffeine.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();

	@GetMapping("/git-info")
	public ResponseEntity<?> get(String rep) {
		try {

			Object cacheResult = cache.getIfPresent(rep.substring(rep.lastIndexOf('/') + 1));

			if (cacheResult != null) {
				return ResponseEntity.ok(cacheResult);
			} else {
				List<GitInfo> gitInfos = new ArrayList<>();
				String git = "https://github.com/" + rep + "/tree/master/";
				StringBuilder response = getHTMLResponse(git);
				JsonObject jsonObject = getHTMLContent(response);
				getInfoFiles(jsonObject, git, "directory", null, gitInfos);

				cache.put(rep.substring(rep.lastIndexOf('/') + 1), gitInfos);
				
				return ResponseEntity.ok(gitInfos);
			}


			
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private StringBuilder getHTMLResponse(String rep) throws IOException, URISyntaxException {
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

	private JsonObject getHTMLContent(StringBuilder response) throws Exception {

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

		if (matcher.find()) {
			scriptContent = matcher.group(1);
		} else {
			throw new Exception("The HTML content could not be retrieved.");
		}

		try {
			jsonObject = JsonParser.parseString(scriptContent).getAsJsonObject();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jsonObject;
	}

	private void getInfoFiles(JsonObject jsonObject, String git, String type, String fileExtension, List<GitInfo> gitInfos) throws Exception {
		if (jsonObject.isJsonObject() && jsonObject.has("props")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("props")
					.getAsJsonObject("initialPayload")
					.getAsJsonObject("tree");
			accessFiles(treeObject, git, gitInfos);
		} else if (jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("directory")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
					.getAsJsonObject("tree");
			
			accessFiles(treeObject, git, gitInfos);
		} else if (jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("file")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
					.getAsJsonObject("blob")
					.getAsJsonObject("headerInfo");
			
			buildGetInfos( treeObject, fileExtension, gitInfos);	
		}
	}

	private void accessFiles(JsonObject treeObject, String git, List<GitInfo> gitInfos) throws Exception{
		if (treeObject.isJsonObject() && treeObject.has("items")) {
			// accessa o array 'items'
			JsonArray itemsArray = treeObject.getAsJsonArray("items");

			// Itera sobre os elementos do array
			for (int i = 0; i < itemsArray.size(); i++) {
				// accessa cada item do array como JsonObject
				if (itemsArray.get(i)
						.getAsJsonObject()
						.get("contentType")
						.getAsString()
						.equalsIgnoreCase("directory")) {

					String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
					String path = git + fileName;
					StringBuilder response = getHTMLResponse(path);
					JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
					getInfoFiles(jsonResponse, git, "directory", null, gitInfos);

				} else {
					String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
					String path = git + fileName;
					StringBuilder response = getHTMLResponse(path);
					JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
					getInfoFiles(jsonResponse, git, "file", fileName.substring(fileName.lastIndexOf('.') + 1), gitInfos);
				}
			}
		} else {
			throw new Exception("The HTML content could not be retrieved.");
		}
	}

	private void buildGetInfos(JsonObject treeObject, String fileExtension, List<GitInfo> gitInfos){
		int lines = 0;
			if (!(treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").isJsonNull())) {
				lines = treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").getAsInt();
			}
			String size = treeObject.get("blobSize").getAsString();
			int bytes;
			if (size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("kb")) {
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Math.round(Float.parseFloat(size) * 1024);
			} else if (size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("gb")) {
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Math.round(Float.parseFloat(size) * (1024 * 1024 * 1024));
			} else {
				size = size.substring(0, size.lastIndexOf(' '));
				bytes = Integer.parseInt(size);
			}

			boolean extensionExists = false;
			for (GitInfo existingGitInfo : gitInfos) {
				if (existingGitInfo.getExtension().equals(fileExtension)) {
					// Atualizar valores existentes
					existingGitInfo.setCount(existingGitInfo.getCount() + 1);
					existingGitInfo.setLines(existingGitInfo.getLines() + lines);
					existingGitInfo.setBytes(existingGitInfo.getBytes() + bytes);
					extensionExists = true;
					break;
				}
			}

			if (!extensionExists) {
				GitInfo newGitInfo = new GitInfo(fileExtension, 1, lines, bytes);
				gitInfos.add(newGitInfo);
			}

	}
}