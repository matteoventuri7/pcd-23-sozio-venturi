import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

public class MainGUI {
    private static IWordSearcher s;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel computingTimeLabel; // New label for computing time
    private static JButton stopButton;
    private static JButton startButton;
    private static JButton suspendButton;
    private static JButton resumeButton;
    private static JComboBox<String> emptySelect;

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (s != null) {
                    try {
                        s.stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        JTextField folderField = new JTextField("/Users/diegosozio/Documents/PCD/test", 20);
        JTextField keywordField = new JTextField("ciao", 20);
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        suspendButton = new JButton("Suspend");
        resumeButton = new JButton("Resume");

        totalFilesLabel = new JLabel("Total files: 0");
        foundPdfFilesLabel = new JLabel("Found PDF files: 0");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Initialize the JComboBox
        emptySelect = new JComboBox<>();

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

        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        suspendButton.setEnabled(false);

        // Add the JComboBox to the input panel
        inputPanel.add(new JLabel("Select:"));
        inputPanel.add(emptySelect);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add elements to the JComboBox
        emptySelect.addItem("Approach: Multithreaded");
        emptySelect.addItem("Approach: Virtual Thread");
        emptySelect.addItem("Approach: Task Java");

        // Create the computing time label
        computingTimeLabel = new JLabel("");
        inputPanel.add(computingTimeLabel);

        emptySelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                instanziateSearcher(folderField.getText(), emptySelect.getSelectedItem().toString(), keywordField.getText().trim());
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computingTimeLabel.setText("");
                // Store the start time when the "Start" button is pressed
                String folderPath = folderField.getText();
                String keyword = keywordField.getText().trim(); // Save the searched keyword

                stopButton.setEnabled(true);
                suspendButton.setEnabled(true);
                stopButton.setEnabled(true);
                startButton.setEnabled(false);

                System.out.println("STARTED");

                try {
                    instanziateSearcher(folderPath, emptySelect.getSelectedItem().toString(), keyword);
                    s.start();

                    // Clear the output area before starting a new search
                    outputArea.setText("");
                    totalFilesLabel.setText("Total files: 0");
                    foundPdfFilesLabel.setText("Found PDF files: 0");
                } catch (Exception ex) {
                    try {
                        s.close();
                    } catch (Exception exc) {
                    }
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                    // Still make sure to signal the end of the search in case of error
                    searchFinished(s.getResult());
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.stop();
                        System.out.println("STOPPED");

                        // Clear the output area
                        outputArea.setText("");

                        // Reset labels
                        totalFilesLabel.setText("Total files: 0");
                        foundPdfFilesLabel.setText("Found PDF files: 0");

                        stopButton.setEnabled(false);
                        resumeButton.setEnabled(false);
                        suspendButton.setEnabled(false);
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

                    System.out.println("SUSPENDED");
                    stopButton.setEnabled(false);
                    suspendButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                }
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    s.resume();

                    System.out.println("RESUMED");
                    stopButton.setEnabled(true);
                    suspendButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                }
            }
        });

        frame.setVisible(true);
    }

    private static void instanziateSearcher(String folderPath, String selectedApproach, String keyword) {
        switch (selectedApproach) {
            case "Approach: Multithreaded":
                s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case "Approach: Virtual Thread":
                s = new VirtualThreadFileSearcher(Path.of(folderPath), keyword);
                break;
            case "Approach: Task Java": // Create the Task Java approach instance
                s = null;
                break;
            default:
                break;
        }

        if(s != null){
            s.register(new IEventsRegistrable() {
                @Override
                public void onNewResultFile(ResultEventArgs ev) {
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append(ev.getFile().toString() + System.lineSeparator());
                        foundPdfFilesLabel.setText("Found PDF files: " + ev.getTotalResultsFiles());
                    });
                }

                @Override
                public void onNewFoundFile(long totalFiles) {
                    SwingUtilities.invokeLater(() -> {
                        totalFilesLabel.setText("Total files: " + totalFiles);
                    });
                }

                @Override
                public void onFinish(SearchResult result) {
                    SwingUtilities.invokeLater(() -> {
                        searchFinished(result);
                    });
                }
            });
        }
    }

    // Method to signal the end of the search
    private static void searchFinished(SearchResult result) {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        resumeButton.setEnabled(false);
        suspendButton.setEnabled(false);

        computingTimeLabel.setText("Computing Time: " + result.getElapsedTime() + " ms");
    }
}