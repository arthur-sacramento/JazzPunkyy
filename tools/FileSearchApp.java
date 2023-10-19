import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSearchApp extends JFrame {
    private JTextField searchField;
    private JList<String> resultList;
    private DefaultListModel<String> listModel;

    public FileSearchApp() {
        setTitle("File Search App");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        JScrollPane resultScrollPane = new JScrollPane(resultList);
        add(resultScrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchFiles();
            }
        });

        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = resultList.locationToIndex(evt.getPoint());
                    openFileInBrowser(listModel.getElementAt(index));
                }
            }
        });
    }

    private void searchFiles() {
        String searchText = searchField.getText();
        File directory = new File("hashes_contents"); // Replace with your directory path
        listModel.clear();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().contains(searchText)) {
                        listModel.addElement(file.getName());
                    }
                }
            }
        }
    }

    private void openFileInBrowser(String fileName) {
        String filePath = "hashes_contents/" + fileName; // Replace with the correct path
        try {
            Desktop.getDesktop().open(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening the file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileSearchApp app = new FileSearchApp();
                app.setVisible(true);
            }
        });
    }
}