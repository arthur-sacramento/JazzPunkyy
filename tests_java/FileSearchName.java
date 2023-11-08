import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class FileSearchName {
    private JFrame frame;
    private JTextField userInputField;
    private DefaultListModel<String> fileListModel;
    private JList<String> fileList;

    public FileSearchName() {
        frame = new JFrame("File Search App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        userInputField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchFiles(userInputField.getText());
            }
        });

        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = fileList.locationToIndex(e.getPoint());
                    openFile(fileListModel.getElementAt(index));
                }
            }
        });

        panel.add(userInputField);
        panel.add(searchButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(fileList), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void searchFiles(String searchString) {
        fileListModel.clear();

        File directory = new File("hashes");
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(searchString)) {
                    fileListModel.addElement(file.getName());
                }
            }
        }
    }

    private void openFile(String filename) {
        String filePath = "hashes" + File.separator + filename;
        try {
            Desktop.getDesktop().open(new File(filePath));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FileSearchName();
            }
        });
    }
}
