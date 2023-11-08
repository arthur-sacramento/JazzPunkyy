import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloaderGUI extends JFrame {

    private JTextField urlField;
    private JTextField extensionField;
    private JButton downloadButton;
    private JTextArea logArea;

    public FileDownloaderGUI() {
        setTitle("File Downloader");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel urlLabel = new JLabel("Base URL: ");
        urlField = new JTextField(20);
        JLabel extensionLabel = new JLabel("File Extension: ");
        extensionField = new JTextField(5);
        downloadButton = new JButton("Download");

        inputPanel.add(urlLabel);
        inputPanel.add(urlField);
        inputPanel.add(extensionLabel);
        inputPanel.add(extensionField);
        inputPanel.add(downloadButton);

        add(inputPanel, BorderLayout.NORTH);

        logArea = new JTextArea(15, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // Add clickable image (label)
        ImageIcon googleIcon = new ImageIcon("google_icon.png");
        JLabel googleLabel = new JLabel(googleIcon);
        googleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        googleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new URL("http://google.com").toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        add(googleLabel, BorderLayout.SOUTH);

        // Add action listener to the download button
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String baseUrl = urlField.getText();
                String extension = extensionField.getText();

                downloadFiles(baseUrl, extension);
            }
        });
    }

    private void downloadFiles(String baseUrl, String extension) {
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
                    logArea.append("Downloaded: " + localFilePath + "\n");
                } catch (IOException e) {
                    filesExist = false;
                }
                fileNumber++;
            } catch (IOException e) {
                filesExist = false;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileDownloaderGUI gui = new FileDownloaderGUI();
                gui.setVisible(true);
            }
        });
    }
}