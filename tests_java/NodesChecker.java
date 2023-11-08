import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;

public class NodesChecker {
    public static List<String> generateCombinations(String[] arr) {
        List<String> combinations = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                if (i != j) {
                    //combinations.add(arr[i] + arr[j]);
                    try {
                        String response = sendGetRequestWithServerParam(arr[i], arr[j]);
                        System.out.println("Response: " + response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }

        return combinations;
    }

    public static boolean isUrlOnline(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode < 400;  // Any status code less than 400 is considered online
        } catch (IOException e) {
            return false;
        }
    }

    public static List<String> removeOfflineURLs(String[] urls) {
        List<String> onlineURLs = new ArrayList<>();
        for (String url : urls) {
            if (isUrlOnline(url)) {
                onlineURLs.add(url);
                compareFilesWithUrl(url); 
                System.out.println("online: " + url);
            } else {
                System.out.println("it is offline: " + url); 
            }
        }
        return onlineURLs;
    }

    public static String sendGetRequestWithServerParam(String url, String serverValue) throws IOException {
        // Create a URL object with the base URL
        URL urlObject = new URL(url);

        // Create a query string with the 'server' parameter
        String query = "server=" + serverValue;

        // Combine the base URL and the query string
        String fullUrl = urlObject.toString() + "?" + query;

        // Create an HttpURLConnection for the full URL
        HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();

        // Set the HTTP method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // If the response code is 200 (HTTP OK), read the response content
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Return the response content as a string
            return response.toString();
        } else {
            // If the response code is not 200, return an error message
            return "HTTP GET request failed with response code: " + responseCode;
        }
    }
  
   public static void compareFilesWithUrl(String url) {

        String directoryPath = "files";

        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            String filename = file.getName();
                            String fileContent = new String(Files.readAllBytes(file.toPath()));
                            doesFileExistWithSameContent(url + filename, filename, fileContent); 
                            //fileContentsMap.put(filename, fileContent);
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file.getName());
                        }
                    }
                }
            } else {
                System.err.println("No files found in the directory: " + directoryPath);
            }
        } else {
            System.err.println("Directory not found: " + directoryPath);
        }
    }

    public static boolean doesFileExistWithSameContent(String url, String filename, String content) {
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("URL request failed with response code: " + connection.getResponseCode());
                return false;
            }

            // Get the URL content
            StringBuilder urlContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    urlContent.append(line);
                }
            }

            // Calculate SHA-256 hash of URL content
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] urlHash = md.digest(urlContent.toString().getBytes());

            // Calculate SHA-256 hash of provided content
            byte[] providedContentHash = md.digest(content.getBytes());

            if (MessageDigest.isEqual(urlHash, providedContentHash)) {
                System.out.println("File with the name '" + filename + "' and the same content exists in the URL." + url);
                return true;
            } else {
                System.out.println("File with the name '" + filename + "' does not exist or has different content in the URL." + url);
                return false;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

     String[] urls = {
            "http://localhost/files/", 
            "http://localhost/files/",
            "https://www.bing.com",
            "https://www.wikipedia.org"
        };

        List<String> onlineURLs = removeOfflineURLs(urls);

        System.out.println("Online URLs:");
        for (String url : onlineURLs) {
            System.out.println(url);
        }
    
        List<String> combinations = generateCombinations(urls);

        for (String combo : combinations) {
            System.out.println(combo);
        }
    }
}