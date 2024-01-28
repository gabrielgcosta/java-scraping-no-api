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
import org.springframework.web.bind.annotation.RequestParam;


/**
 * GitScrapingController
 */
@RestController
public class GitScrapingController {

	// Creating cache with max size of 10000 itens and expiration time of 10 minutes
	private static final Cache<String, Object> cache = Caffeine.newBuilder()
			.maximumSize(10000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();

	@GetMapping("/")
	public String getMethodName(@RequestParam String param) {
		System.out.println("teste");
		String teste = "oi";
		return  teste;
	}
	

	@GetMapping("/git-info")
	public ResponseEntity<?> get(String rep) {
		try {

			// Trying to get the cached response using the name of the rep as key
			Object cacheResult = cache.getIfPresent(rep.substring(rep.lastIndexOf('/') + 1));

			// If the cache result was found, just return it
			if (cacheResult != null) {
				return ResponseEntity.ok(cacheResult);
			} else {
				// Create a new gitInfos list
				List<GitInfo> gitInfos = new ArrayList<>();
				// Build the git URL
				String git = "https://github.com/" + rep + "/tree/master/";
				// Get the HTML from the page
				StringBuilder response = getHTMLResponse(git);
				// Fetch the needed content
				JsonObject jsonObject = getHTMLContent(response);
				// Get the information of the files
				getInfoFiles(jsonObject, git, "directory", null, gitInfos);

				// Put the result into the cache, using the name of the rep as key
				cache.put(rep.substring(rep.lastIndexOf('/') + 1), gitInfos);

				return ResponseEntity.ok(gitInfos);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private StringBuilder getHTMLResponse(String rep) throws IOException, URISyntaxException {
		// Conect to the rep URL
		URL url = new URI(rep).toURL();
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// Read the HTML and build the response
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

		// Create a first regex to narrow the html content
		String regex = "<react-partial\\s+partial-name=\"repos-overview\"[^>]*>(.*?)</react-partial>";
		// Build a pattern with the regex
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		// Matches the response with the pattern
		Matcher matcher = pattern.matcher(response);
		String firstContent = "";
		// If the pattern is found, get the content between the tag
		if (matcher.find()) {
			firstContent = matcher.group(1);

		} else {
			throw new Exception("The HTML content could not be retrieved.");
		}

		// Create a new regex to get only the files information
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

		// With the information I need, I turn it into a json object
		try {
			jsonObject = JsonParser.parseString(scriptContent).getAsJsonObject();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jsonObject;
	}

	private void getInfoFiles(JsonObject jsonObject, String git, String type, String fileExtension,
			List<GitInfo> gitInfos) throws Exception {
		// This treats the request to the main rep URL
		// Only the main URL returns the key props
		if (jsonObject.isJsonObject() && jsonObject.has("props")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("props")
					.getAsJsonObject("initialPayload")
					.getAsJsonObject("tree");
			// Access the files in the rep
			// This function builds a new URL pointing to a directory or a file, and then
			// calls the getInfoFiles function again with the new URL informations
			accessFiles(treeObject, git, gitInfos);

			// If the new URL points to a directory, get the information, and calls the
			// getInfoFiles function again to access the files into that directory
		} else if (jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("directory")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
					.getAsJsonObject("tree");

			accessFiles(treeObject, git, gitInfos);

			// If the new URL points to a file, get the information of that file, and calls
			// buildGetInfos function to get the information and add it to the response list
		} else if (jsonObject.isJsonObject() && jsonObject.has("payload") && type.equalsIgnoreCase("file")) {
			JsonObject treeObject = jsonObject.getAsJsonObject("payload")
					.getAsJsonObject("blob")
					.getAsJsonObject("headerInfo");

			buildGetInfos(treeObject, fileExtension, gitInfos);
		}
	}

	private void accessFiles(JsonObject treeObject, String git, List<GitInfo> gitInfos) throws Exception {
		if (treeObject.isJsonObject() && treeObject.has("items")) {

			// Access 'items' array
			JsonArray itemsArray = treeObject.getAsJsonArray("items");

			// Iterates over the elements of the array.
			for (int i = 0; i < itemsArray.size(); i++) {
				// Accesses each item of the array as a JsonObject.
				// Checks if the item is a directory or a file
				if (itemsArray.get(i)
						.getAsJsonObject()
						.get("contentType")
						.getAsString()
						.equalsIgnoreCase("directory")) {

					// Get the path to the directory
					String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
					// Build de URL
					String path = git + fileName;
					// Get the HTML response, that in this call returns a json
					StringBuilder response = getHTMLResponse(path);
					// Parse it to a json object
					JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
					// Send the information about the directory
					getInfoFiles(jsonResponse, git, "directory", null, gitInfos);

				} else {
					// Get the path to the file
					String fileName = itemsArray.get(i).getAsJsonObject().get("path").getAsString();
					// Build de URL
					String path = git + fileName;
					// Get the HTML response, that in this call returns a json
					StringBuilder response = getHTMLResponse(path);
					// Parse it to a json object
					JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
					// Send the information about the file
					getInfoFiles(jsonResponse, git, "file", fileName.substring(fileName.lastIndexOf('.') + 1),
							gitInfos);
				}
			}
		} else {
			throw new Exception("The HTML content could not be retrieved.");
		}
	}

	private void buildGetInfos(JsonObject treeObject, String fileExtension, List<GitInfo> gitInfos) {
		// Get the number of lines in the file
		int lines = 0;
		if (!(treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").isJsonNull())) {
			lines = treeObject.getAsJsonObject("lineInfo").get("truncatedLoc").getAsInt();
		}

		// Get the size of the file in bytes
		String size = treeObject.get("blobSize").getAsString();
		int bytes;
		// Converts kb to bytes
		if (size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("kb")) {
			size = size.substring(0, size.lastIndexOf(' '));
			bytes = Math.round(Float.parseFloat(size) * 1024);
		// Converts gb to bytes
		} else if (size.substring(size.lastIndexOf(' ') + 1).equalsIgnoreCase("gb")) {
			size = size.substring(0, size.lastIndexOf(' '));
			bytes = Math.round(Float.parseFloat(size) * (1024 * 1024 * 1024));
		} else {
			size = size.substring(0, size.lastIndexOf(' '));
			bytes = Integer.parseInt(size);
		}

		// Checks if the file extension alredy exists in the list
		boolean extensionExists = false;
		for (GitInfo existingGitInfo : gitInfos) {
			if (existingGitInfo.getExtension().equals(fileExtension)) {
				// If exists, update the information
				existingGitInfo.setCount(existingGitInfo.getCount() + 1);
				existingGitInfo.setLines(existingGitInfo.getLines() + lines);
				existingGitInfo.setBytes(existingGitInfo.getBytes() + bytes);
				extensionExists = true;
				break;
			}
		}
		if (!extensionExists) {
			// If doesn't exists, create a new git infor object and add it to the list
			GitInfo newGitInfo = new GitInfo(fileExtension, 1, lines, bytes);
			gitInfos.add(newGitInfo);
		}

	}
}