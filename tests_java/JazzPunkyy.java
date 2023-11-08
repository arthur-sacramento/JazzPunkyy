import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.*;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JazzPunkyy {

    public static void main(String[] args) {

        createFolder("files");
        createFolder("hashes");
        createFolder("hashes_contents");          
        createFolder("hashes_domain");           
        createFolder("downloaded");      

        // Specify the directory path
        String directoryPath = "hashes";

        // Create a File object for the directory
        File directory = new File(directoryPath);    

        ArrayList<String> urlsToNotify = new ArrayList<>();    
        String message = "";  

        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // List all the files in the directory
            File[] files = directory.listFiles();                 
              
            // Check if there are files in the directory
            if (files != null && files.length > 0) {
                System.out.println("Files in the 'hashes' directory:");

                /*/ 
                   Iterate through the files 
                   If it is a text file, then try to open an HTTP connection with its contents.
                   Try to obtain the links from the URL and save each one as a text file.
                   Each file will be saved using a SHA-256 hash as its hash name (based on its contents).
                   If it is not a text file, it will be copied to the 'files' folder using a number as its filename.
                   There will be new URLs to be checked, in case the previous URLs have any links. 

                /*/

                for (File file : files) {
                    if (file.isFile()) {
  
                        String fileName = file.getName();                     
   
                        String fileFullPath = file.getAbsolutePath();

                        String extension = fileExtension(fileFullPath);  

                        String fileContents = readFile(fileFullPath);    

                        String textHash = calculateSHA256FromTextFile(fileContents);             

                        String fileHash = calculateSHA256FromBinaryFile(fileFullPath);                       

                        if (fileHash != null) {

                            boolean fileExists = checkIfFileExists("hashes/" + fileHash + "." + extension);

                            if (fileExists){
                                continue;
                            }

                        } else {
                  
                            System.err.println("Failed to calculate the SHA-256 hash.");
                        }

                        boolean fileExists = checkIfFileExists("files/" + fileName); 

                        if (fileExists){continue;}
                       
                        if (extension.equals("txt")){
                            
                            fileContents = removeTrailingSlash(fileContents);
                                                                        
                            writeFileWithExtension("hashes/" + textHash, fileContents, extension); 

                            boolean isSend = isURLContentEqualToString(fileContents + "hashes/" + textHash + "." + extension, fileContents);
                            System.out.println(fileContents + (isSend ? " - host hashfile found (the host is a node)" : " - no host hashfile (the host is not a node)"));

                            try {
                                String urlContents = fetchURLContent(fileContents);
                                extractLinks(urlContents);
                                writeFileWithExtension("hashes_contents/" + textHash, urlContents, ".html");   
                                System.out.println(urlContents);
                           } catch (IOException e) {
                                e.printStackTrace();
                           }                            

                            //if(isSend){
                               //writeFileWithExtension("validated_hashes/" + textHash, fileContents, extension);
                                //urlsToNotify.add(fileContents);    
                            //}                              

                        } else {
                            
                            copyFile(fileFullPath, extension); 
                        }                
       
                        numberedFilesCopy(fileFullPath, extension);    
                         
                    }
                }

            //notifyAllNodes(urlsToNotify,message);    

            } else {
                System.out.println("No files found in the 'hashes' directory.");
            }
        } else {
            System.err.println("The 'hashes' directory does not exist or is not a directory.");
        }
    }
   
    public static String fileExtension(String fileName) {

        int lastDotIndex = fileName.lastIndexOf('.');
        String extension = "";

        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1);
        }

        return extension;
    }

    public static boolean checkAvailability(String urlString) {
        try {
            // Create a URL object
            URL url = new URL(urlString);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the connection timeout and read timeout to 5 seconds
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Connect to the URL
            connection.connect();

            // Check the HTTP status code
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                return true;
            } else {
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
    } catch (IOException e) {
        e.printStackTrace(); // or log the error
        return null; // Return an indication of failure, e.g., return an empty string or throw a custom exception.
    }

        return content.toString();
    }

    public static String calculateSHA256FromTextFile(String input) {
        try {
            // Create a MessageDigest object for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Compute the hash value by converting the string to bytes and updating the digest
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Return an indication of failure
        }
    }

    public static void writeFile(String filename, String contents) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(contents);
            writer.close();
            System.out.print("'" + filename + "' has been written");
        } catch (IOException e) {
            System.err.print("Error writing to file: " + e.getMessage());
        }
    }

    public static void writeFileWithExtension(String filename, String contents, String extension) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename + "." + extension));
            writer.write(contents);
            writer.close();
            //System.out.print("'" + filename + "." + extension  + "' has been written");
        } catch (IOException e) {
            System.err.print("Error writing to file: " + e.getMessage());
        }
    }

    public static void copyFile(String sourcePath, String extension) {
        try {
            Path source = Paths.get(sourcePath);
            String destinationPath = "hashes/" + calculateSHA256FromBinaryFile(sourcePath) + "." + extension;   
            Path destination = Paths.get(destinationPath);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully from " + sourcePath + " to " + destinationPath);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error appropriately, e.g., log it or throw a custom exception.
        }
    }

   public static void notifyAllNodes(ArrayList<String> urls, String message) {
        for (String currentUrl : urls) {
            for (String targetUrl : urls) {
                if (!currentUrl.equals(targetUrl)) {
                    try {
                        String urlWithQueryParam = targetUrl + "?url=" + currentUrl;
                        String response = sendGetRequest(urlWithQueryParam);
                        System.out.println("GET request to " + targetUrl + " with 'url' parameter: " + response);
                    } catch (IOException e) {
                        System.err.println("Error making GET request to " + targetUrl + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private static String sendGetRequest(String url) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL getUrl = new URL(url);
            connection = (HttpURLConnection) getUrl.openConnection();
            connection.setRequestMethod("GET");

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static boolean isURLContentEqualToString(String url, String expectedString) {
        try {
            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            reader.close();

            // Compare the content of the URL with the expected string and return the result
            return content.toString().equals(expectedString);
        } catch (IOException e) {
            e.printStackTrace(); // Handle or log the exception as needed
            return false; // Return false if there's an exception (e.g., URL not found)
        }
    }

    public static boolean doesFileExist(String fileName) {
        // Specify the path to the 'files' directory
        String directoryPath = "files";

        // Create a File object representing the directory
        File directory = new File(directoryPath);

        // Create a File object representing the file to check
        File fileToCheck = new File(directory, fileName);

        // Check if the file exists
        return fileToCheck.exists();
    }

    public static void numberedFilesCopy (String sourcePath, String extension) {

        int x = 1;

        while (true) {

            String fileName = x + "." + extension;
            boolean fileExists = doesFileExist(fileName);

            if (!fileExists) {
                try {
                    Path source = Paths.get(sourcePath);
                    String destinationPath = "files/" + fileName;
                    Path destination = Paths.get(destinationPath);
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File copied successfully from " + sourcePath + " to " + destinationPath);
                } catch (IOException e) {
                    e.printStackTrace();
                // Handle the error appropriately, e.g., log it or throw a custom exception.
                }

                break;
            }

            System.out.println("This is an infinite loop.");
            x++;
        }
    }

    public static void createFolder(String folderPath) {
        // Create a Path object from the folderPath
        Path path = Paths.get(folderPath);

        // Check if the folder exists
        if (Files.exists(path) && Files.isDirectory(path)) {
            System.out.println("Folder" + folderPath + " exists."); 
        }

        // If the folder does not exist, attempt to create it
        try {
            Files.createDirectories(path);
            System.out.println("Creating folder (" + folderPath + ")..."); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    public static String fetchURLContent(String urlString) throws IOException {
        URL url = new URL(urlString);
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        } catch (IOException e) {
        // Handle the exception appropriately, such as logging the error or re-throwing it
            throw e;
        }

        return content.toString();
    }

    public static void extractLinks(String text) {
        //Set<String> links = new HashSet<>();

        // Define a regex pattern to match URLs
        //Pattern pattern = Pattern.compile("https?://\\S+");
        Pattern pattern = Pattern.compile("https?://\\S+[^']?");

        // Create a matcher with the pattern and the input text
        Matcher matcher = pattern.matcher(text);

        String groupValue;
        String urlHash;
        String domainHash;
        String domainAddrr;
  
        // Find and add all matching URLs to the Set
        while (matcher.find()) {

            groupValue = matcher.group();
            groupValue = removeSpacesAndQuotes(groupValue);
            groupValue = removeTrailingSlash(groupValue);
               
            urlHash = calculateSHA256FromTextFile(groupValue);
            writeFileWithExtension("hashes/" + urlHash, groupValue, ".txt"); 

            domainAddrr = extractDomain(groupValue);
            domainHash = calculateSHA256FromTextFile(domainAddrr); 
            writeFileWithExtension("hashes_domain/" + domainHash, domainAddrr, ".txt"); 
            //links.add(matcher.group());
        }
    }

    public static String removeSpacesAndQuotes(String input) {
        // Use regex to remove spaces, single quotes, and double quotes
        String result = input.replaceAll("[\\s'\"']", "");
        return result;
    }

    public static String extractDomain(String input) {
        int thirdSlashIndex = -1;
        for (int i = 0; i < 3; i++) {
            thirdSlashIndex = input.indexOf('/', thirdSlashIndex + 1);
            if (thirdSlashIndex == -1) {
                break; // Break if there are fewer than 3 slashes
            }
        }

        if (thirdSlashIndex != -1) {
            return input.substring(0, thirdSlashIndex);
        } else {
            return input; // Return the entire string if there are fewer than 3 slashes
        }
    }

    public static String removeTrailingSlash(String input) {
        if (input != null && input.endsWith("/")) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }

    public static String getContentAfterSecondSlash(String input) {
        // Find the index of the first '/'
        int firstSlashIndex = input.indexOf('/');
        if (firstSlashIndex == -1) {
            // No '/' found, return the original input
            return input;
        }

        // Find the index of the second '/' starting from the position after the first '/'
        int secondSlashIndex = input.indexOf('/', firstSlashIndex + 1);

        if (secondSlashIndex == -1) {
            // No second '/' found, return the original input
            return input;
        }

        // Extract the substring after the second '/'
        String result = input.substring(secondSlashIndex + 1);

        return result;
    }

    public static long getLatency(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to HEAD to minimize data transfer
            connection.setRequestMethod("HEAD");

            long startTime = System.currentTimeMillis();
            connection.connect();
            long endTime = System.currentTimeMillis();

            // Calculate and return the latency
            return endTime - startTime;
        } catch (IOException e) {
            // Handle exceptions, e.g., URL is invalid or cannot be reached
            e.printStackTrace();
            return -1; // Return a negative value to indicate an error
        }
    }

    public static String calculateSHA256FromBinaryFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                System.err.println("File does not exist or is not a regular file.");
                return null;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            fileInputStream.close();

            byte[] hashBytes = digest.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Return an indication of failure
        }
    }

     public static String fileSize(String filePath) {
        try {
            File file = new File(filePath);
            long fileSize = file.length();
            String fileSizeString = String.valueOf(fileSize);

            // Ensure we don't exceed the length of the file size string
            int length = Math.min(60, fileSizeString.length());

            return fileSizeString.substring(0, length);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                System.err.println("Failed to delete the file: " + filePath);
                return false;
            }
        } else {
            System.err.println("File not found: " + filePath);
            return false;
        }
    }

    public static String CurrentDateSimple() {
        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        // Get the current date
        Date currentDate = new Date();

        // Format the date using the specified format
        String formattedDate = dateFormat.format(currentDate);

        return formattedDate;
    }
}