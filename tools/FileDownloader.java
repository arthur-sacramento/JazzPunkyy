import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloader {

    public static void main(String[] args) {
        String baseUrl = getUserInput("Enter the base URL for downloading files (e.g., http://localhost/files/): ");
        String extension = getUserInput("Enter the file extension (e.g., jpg): ");

        String destinationFolder = "downloaded";
        new File(destinationFolder).mkdirs();

        int fileNumber = 1;
        boolean filesExist = true;

        while (filesExist) {
            String remoteFileUrl = baseUrl + fileNumber + "." + extension;
            String localFilePath = destinationFolder + "/" + fileNumber + "." + extension;

            try {
                URL url = new URL(remoteFileUrl);
                URLConnection connection = url.openConnection();

                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(localFilePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Downloaded: " + localFilePath);
                } catch (IOException e) {
                    filesExist = false;
                }
                fileNumber++;
            } catch (IOException e) {
                filesExist = false;
            }
        }
    }

    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }
}