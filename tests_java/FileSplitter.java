import java.io.*;
import java.util.Base64;

public class FileSplitter {
    public static void main(String[] args) {
        String inputFileName = "duck.jpg";
        int chunkSize = 1024; // 1 KB
        String outputFolder = "base64/";

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            String base64Data = Base64.getEncoder().encodeToString(buffer);
            int totalChunks = (int) Math.ceil((double) base64Data.length() / chunkSize);

            for (int i = 0; i < totalChunks; i++) {
                int startIndex = i * chunkSize;
                int endIndex = Math.min(startIndex + chunkSize, base64Data.length());
                String chunk = base64Data.substring(startIndex, endIndex);

                String outputFileName = outputFolder + inputFileName.replace(".", "-" + (i + 1) + ".");
                saveBase64ToFile(outputFileName, chunk);
            }

            System.out.println("File split into " + totalChunks + " base64 pieces and saved in the 'base64' folder.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveBase64ToFile(String fileName, String base64Data) throws IOException {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.print(base64Data);
        }
    }
}
