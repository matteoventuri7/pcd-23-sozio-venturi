import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

public class MainGUI {
    private static IWordSearcher s;
    private static SearchResult result;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel pdfFilesWithKeywordLabel;
    private static JButton stopButton;
    private static JButton startButton;
    private static String keyword; // Variable to store the searched keyword

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        JTextField folderField = new JTextField("/Users/diegosozio/Documents/PCD/test", 20);
        JTextField keywordField = new JTextField("ciao", 20);
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        JButton suspendButton = new JButton("Suspend");
        JButton resumeButton = new JButton("Resume");

        totalFilesLabel = new JLabel("Total files: 0");
        foundPdfFilesLabel = new JLabel("Found PDF files: 0");
        pdfFilesWithKeywordLabel = new JLabel("PDF files with keyword: 0");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        inputPanel.add(new JLabel("Folder:"));
        inputPanel.add(folderField);
        inputPanel.add(new JLabel("Keyword:"));
        inputPanel.add(keywordField);
        inputPanel.add(startButton);
        inputPanel.add(stopButton);
        inputPanel.add(suspendButton);
        inputPanel.add(resumeButton);
        inputPanel.add(totalFilesLabel);
        inputPanel.add(foundPdfFilesLabel);
        inputPanel.add(pdfFilesWithKeywordLabel);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderField.getText();
                keyword = keywordField.getText().trim(); // Save the searched keyword
                System.out.println("STARTED");
                stopButton.setEnabled(true);

                // Clear the output area before starting a new search
                outputArea.setText("");
                totalFilesLabel.setText("Total files: 0");
                foundPdfFilesLabel.setText("Found PDF files: 0");
                pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");

                try {
                    s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                    s.search();
                } catch (Exception ex) {
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.close();
                        System.out.println("STOPPED");
                        stopButton.setEnabled(false);
                        startButton.setEnabled(true); // Enable the "Start" button
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        suspendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    s.pause();
                }
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.resume();
                        startButton.setEnabled(true);
                        stopButton.setEnabled(true);

                        // Clear the output area when Resume is pressed
                        outputArea.setText("");
                        totalFilesLabel.setText("Total files: 0");
                        foundPdfFilesLabel.setText("Found PDF files: 0");
                        pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");
                        keyword = keywordField.getText().trim(); // Update the searched keyword
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });


        frame.setVisible(true);
        updateOutputArea();
    }

    private static void updateOutputArea() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (s != null && s.getResult() != result) {
                        result = s.getResult();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                outputArea.setText("");
                                outputArea.append("IsPartial: " + result.isPartial() + "\n");
                                totalFilesLabel.setText("Total files: " + result.getTotalFiles());
                                foundPdfFilesLabel.setText("Found PDF files: " + result.getFiles().size());

                                // Reset the counter for PDF files with the keyword
                                int pdfWithKeyword = 0;
                                for (Path file : result.getFiles()) {
                                    outputArea.append(file.toString() + "\n");
                                    if (file.toString().toLowerCase().endsWith(".pdf")) {
                                        // Check if the filename contains the keyword
                                        if (file.getFileName().toString().toLowerCase().contains(keyword)) {
                                            pdfWithKeyword++;
                                        }
                                    }
                                }
                                pdfFilesWithKeywordLabel.setText("PDF files with keyword: " + pdfWithKeyword);
                            }
                        });
                    }
                    try {
                        Thread.sleep(100); // Add a short delay to avoid busy-waiting
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
