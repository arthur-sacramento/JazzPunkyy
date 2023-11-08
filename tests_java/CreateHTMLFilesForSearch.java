import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.io.FileWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class CreateHTMLFilesForSearch {
    public static void main(String[] args) {
        // Define the directory path
        String directoryPath = "hashes_contents";

        // Create a File object representing the directory
        File directory = new File(directoryPath);

        String uniqueWords = "";

        // Check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // Get the list of files in the directory
            File[] files = directory.listFiles();

            // Print the names of the files
            System.out.println("List of files in the directory:");
            
            for (File file : files) {

                String fileName = file.getName();                     
   
                String fileFullPath = file.getAbsolutePath();

                String fileContents = readContentFromFile(fileFullPath);

                String fileContentsAlphanumeric = replaceNonAlphanumericWithSpace(fileContents);

                uniqueWords = getUniqueWords(fileContentsAlphanumeric);

                String onlyHashFromFileName = getFirstElementAfterSplit(fileName);

                System.out.println(onlyHashFromFileName); 

                String hashContents = readTextFile("hashes/" + onlyHashFromFileName + ".txt");

                appendToHtmlFiles(uniqueWords, hashContents); 

                System.out.println(uniqueWords); 
            }
        } else {
            System.out.println(uniqueWords);
        }
    }

    public static String readContentFromFile(String filePath) {
        StringBuilder content = new StringBuilder();

        try {
            // Create a FileReader to read the file
            FileReader fileReader = new FileReader(filePath);

            // Wrap the FileReader in a BufferedReader for efficient reading
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;

            // Read lines from the file and append them to the content StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
                content.append("\n"); // Add a newline character for line separation
            }

            // Close the BufferedReader and FileReader
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            // Handle any potential exceptions that may occur during file reading
            e.printStackTrace();
        }

        return content.toString();
    }

    public static String replaceNonAlphanumericWithSpace(String input) {
        // Use a regular expression to replace non-alphanumeric characters with spaces
        return input.replaceAll("[^a-zA-Z0-9]", " ");
    }

    public static String getUniqueWords(String input) {
        // Split the input string into words
        String[] words = input.split(" ");

        // Create a set to store unique words
        LinkedHashSet<String> uniqueWords = new LinkedHashSet<>();

        // Add words to the set to ensure uniqueness
        for (String word : words) {
            uniqueWords.add(word);
        }

        // Convert the set back to a comma-separated string
        StringBuilder result = new StringBuilder();
        for (String word : uniqueWords) {
            result.append(word);
            result.append(",");
        }

        // Remove the trailing comma, if any
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    public static void appendToHtmlFiles(String input, String contents) {
        // Split the input string into elements
        String[] elements = input.split(",");

        // Define the folder where you want to create/append to HTML files
        String folderPath = "contents";

        // Ensure the folder exists or create it if necessary
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Process each element
        for (String element : elements) {            

            boolean fileNameWithNumber = containsNumber(element);

            if (fileNameWithNumber){
                continue;
            } 

            boolean smallLenght = isLengthLessThan2(element);

            if (smallLenght){
                continue;
            } 

            element = element.toLowerCase();

            // Generate a unique HTML file name based on the element
            String rawDate = currentDateRaw();

            String fileName = element + ".html";

            String fileNameWithDate = element + "_" + rawDate + ".html";   

            boolean fileRepeatedContents = isStringInFile("contents/" + fileName, contents);

            if (fileRepeatedContents){
                continue;
            } 

            boolean fileExists = checkIfFileExists("contents/" + fileName);

            boolean fileExistsDated = checkIfFileExists("contents/" + fileNameWithDate);

            boolean defaultFileIsBig = isFileSizeGreaterThan1MB("contents/" + fileName); 

            String headerHTMLFile;

            if (!fileExists){
                headerHTMLFile = "<a href='" + fileNameWithDate  + "' id='nextButton' class='nextButton'>Next results</a><div id='ads'></div><script src='../advertise.js'></script><link rel='stylesheet' type='text/css' href='../default.css'>";
            } else { 
                headerHTMLFile = "";
            }

            if (!defaultFileIsBig){

                try {
                    // Create a FileWriter in append mode to write content to the HTML file
                    FileWriter fileWriter = new FileWriter(new File(folder, fileName), true);

                    // Write the HTML content to the file (you can customize this part)
                    fileWriter.write(headerHTMLFile + "<p><a href='" + contents + "' target='_blank'>" + contents +"</a></p>");

                    // Close the FileWriter
                    fileWriter.close();

                    System.out.println("Appended to HTML file: " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!fileExistsDated){
                headerHTMLFile = "<button id='nextButton' class='nextButton' onclick=" + '"' + "checkAndRedirectWithPrefix('" + element + "_" + "')" + '"' + ">Next</button><script src='../nextpage.js'></script><div id='ads'></div><script src='../advertise.js'></script><link rel='stylesheet' type='text/css' href='../default.css'>";
            } else { 
                headerHTMLFile = "";
            }

            try {
                // Create a FileWriter in append mode to write content to the HTML file
                FileWriter fileWriter = new FileWriter(new File(folder, fileNameWithDate), true);

                // Write the HTML content to the file (you can customize this part)
                fileWriter.write(headerHTMLFile + "<p><a href='" + contents + "' target='_blank'>" + contents +"</a></p>");

                // Close the FileWriter
                fileWriter.close();

                System.out.println("Appended to HTML file: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean containsNumber(String text) {
        // Define a regular expression pattern to match any digit
        Pattern pattern = Pattern.compile(".*\\d+.*");

        // Use a Matcher to check if the text matches the pattern
        Matcher matcher = pattern.matcher(text);

        return matcher.matches();
    }

    public static boolean isLengthLessThan2(String text) {
        return text.length() < 2;
    }

    public static String getFirstElementAfterSplit(String input) {
        String[] parts = input.split("\\.");
        if (parts.length > 0) {
            return parts[0];
        } else {
            // Handle the case where there are no periods in the string
            return input;
        }
    }

    public static String readTextFile(String filePath) {
        StringBuilder content = new StringBuilder();
        
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    public static boolean isStringInFile(String filePath, String targetString) {
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            StringBuilder fileContent = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            bufferedReader.close();

            // Search for the target string in the entire file content
            return fileContent.toString().contains(targetString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean checkIfFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
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

    public static String currentDateRaw() {
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        
        // Format the date without leading zeros and delimiters
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dMMuuuu");
        String formattedDate = currentDate.format(formatter);
        
        return formattedDate;
    }

    public static boolean isFileSizeGreaterThan1MB(String filePath) {
        File file = new File(filePath);
        // Check if the file exists and its size is greater than 1 MB
        return file.exists() && file.length() > 1048576; // 1 MB = 1,048,576 bytes
    }
}
