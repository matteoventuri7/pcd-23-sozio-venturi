import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.time.Duration;

public class MainGUI {
    private static boolean isClosing = false;
    private static IWordSearcher s;
    private static Thread updateOutputAreaThread;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel pdfFilesWithKeywordLabel;
    private static JLabel computingTimeLabel; // New label for computing time
    private static JButton stopButton;
    private static JButton startButton;
    private static String keyword;
    private static volatile boolean isStopping = false;
    private static volatile boolean isSuspended = false;
    private static JComboBox<String> emptySelect;

    private static Timer computingTimer; // Timer to measure computing time
    private static long startTime; // Variable to store the start time

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                isClosing = true;

                if (s != null) {
                    try {
                        s.stop();
                        s.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                stopUpdateThread(); // Stop the update thread gracefully

                if (updateOutputAreaThread != null) {
                    try {
                        updateOutputAreaThread.join(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                        // ops
                    }
                }
            }

            private static void stopUpdateThread() {
                if (updateOutputAreaThread != null) {
                    updateOutputAreaThread.interrupt();
                    try {
                        updateOutputAreaThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateOutputAreaThread = null;
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
        JButton suspendButton = new JButton("Suspend");
        JButton resumeButton = new JButton("Resume");

        totalFilesLabel = new JLabel("Total files: 0");
        foundPdfFilesLabel = new JLabel("Found PDF files: 0");
        pdfFilesWithKeywordLabel = new JLabel("PDF files with keyword: 0");

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
        inputPanel.add(pdfFilesWithKeywordLabel);

        // Add the JComboBox to the input panel
        inputPanel.add(new JLabel("Select:"));
        inputPanel.add(emptySelect);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add elements to the JComboBox
        emptySelect.addItem("Option 1");
        emptySelect.addItem("Option 2");
        emptySelect.addItem("Option 3");

        // Create the computing time label
        computingTimeLabel = new JLabel("Computing Time: ");
        inputPanel.add(computingTimeLabel);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Store the start time when the "Start" button is pressed
                String folderPath = folderField.getText();
                keyword = keywordField.getText().trim(); // Save the searched keyword
                startTime = System.nanoTime();
                stopButton.setEnabled(true);
                startButton.setEnabled(false);
                System.out.println("STARTED");

                // Clear the output area before starting a new search
                outputArea.setText("");
                totalFilesLabel.setText("Total files: 0");
                foundPdfFilesLabel.setText("Found PDF files: 0");
                pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");

                try {
                    s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                    s.search();

                    // The search has finished, call the method to signal it
                    searchFinished();
                } catch (Exception ex) {
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                    // Still make sure to signal the end of the search in case of error
                    searchFinished();
                }
            }
        });

        frame.setVisible(true);
        updateOutputArea();

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.stop();
                        s = null; // Set the search instance to null to indicate the search has stopped
                        isStopping = true; // Set the flag to stop the thread
                        System.out.println("STOPPED");
                        stopButton.setEnabled(false);
                        startButton.setEnabled(true); // Enable the "Start" button

                        // Remove the output area text when "Stop" is pressed
                        outputArea.setText("");
                        totalFilesLabel.setText("Total files: 0");
                        foundPdfFilesLabel.setText("Found PDF files: 0");
                        pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");

                        // Update the keyword with the new value from the keywordField
                        keyword = keywordField.getText().trim();
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
                    isSuspended = true; // Set the flag to indicate the search is suspended
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
                    s.pause();
                    isSuspended = false; // Reset the flag to indicate the search is no longer suspended
                    System.out.println("RESUMED");
                    stopButton.setEnabled(true);
                    suspendButton.setEnabled(true);
                    resumeButton.setEnabled(false);

                    // Remove the output area text when "Resume" is pressed
                    outputArea.setText("");
                    totalFilesLabel.setText("Total files: 0");
                    foundPdfFilesLabel.setText("Found PDF files: 0");
                    pdfFilesWithKeywordLabel.setText("PDF files with keyword: 0");
                    keyword = keywordField.getText().trim(); // Update the searched keyword
                }
            }
        });

        frame.setVisible(true);
        updateOutputArea();
    }

    private static void updateOutputArea() {
        updateOutputAreaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isClosing) {
                        break;
                    }

                    // Check if the search is stopped, then wait until it's resumed
                    while (isStopping) {
                        try {
                            Thread.sleep(100); // Add a short delay to avoid busy-waiting
                        } catch (InterruptedException e) {
                            // It's okay
                        }
                    }

                    // Update the GUI with the search results
                    if (!isSuspended && s != null && s.getResult() != null) {
                        var result = s.getResult();
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
                        // It's okay
                    }
                }
            }
        });
        updateOutputAreaThread.setDaemon(true);
        updateOutputAreaThread.start();
    }

    // Method to signal the end of the search
    private static void searchFinished() {
        if (updateOutputAreaThread != null) {
            // Restart the update thread if it was interrupted
            if (updateOutputAreaThread.isInterrupted()) {
                updateOutputAreaThread.interrupt();
            }
            // Reset the flags to indicate that the search has ended and can be resumed again
            isStopping = false;
            isSuspended = false;
            // Compute and update the computing time label
            long endTime = System.nanoTime();
            long computingTimeNano = endTime - startTime;
            double computingTimeMillis = computingTimeNano / 1_000_000.0;
            computingTimeLabel.setText("Computing Time: " + computingTimeMillis + " ms");
        }
    }
}
