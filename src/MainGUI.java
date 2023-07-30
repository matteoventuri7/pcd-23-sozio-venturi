import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Collectors;


public class MainGUI {
    private static boolean closeOutputThread = false;
    private static IWordSearcher s;
    private static Thread updateOutputAreaThread;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel computingTimeLabel; // New label for computing time
    private static JButton stopButton;
    private static JButton startButton;
    private static JButton suspendButton;
    private static JButton resumeButton;
    private static String keyword;
    private static volatile boolean isSuspended = false;
    private static JComboBox<String> emptySelect;
    private static VirtualThreadFileSearcher virtualThreadFileSearcher;
    private static Timer computingTimer; // Timer to measure computing time
    private static long startTime; // Variable to store the start time

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeOutputThread = true;

                if (s != null) {
                    try {
                        s.stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                if (updateOutputAreaThread != null) {
                    try {
                        updateOutputAreaThread.join(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                        // ops
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
        computingTimeLabel = new JLabel("Computing Time: ");
        inputPanel.add(computingTimeLabel);

        emptySelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = folderField.getText();
                String selectedApproach = (String) emptySelect.getSelectedItem();
                switch (selectedApproach) {
                    case "Approach: Multithreaded":
                        s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                        break;
                    case "Approach: Virtual Thread":
                        if (virtualThreadFileSearcher == null) {
                            virtualThreadFileSearcher = new VirtualThreadFileSearcher(Path.of(folderPath), keyword);
                        }
                        s = virtualThreadFileSearcher;
                        break;
                    case "Approach: Task Java": // Create the Task Java approach instance
                        s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                        break;
                    default:
                        break;
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startOutputArea();
                // Store the start time when the "Start" button is pressed
                String folderPath = folderField.getText();
                keyword = keywordField.getText().trim(); // Save the searched keyword
                startTime = System.nanoTime();
                stopButton.setEnabled(true);
                suspendButton.setEnabled(true);
                stopButton.setEnabled(true);
                startButton.setEnabled(false);
                System.out.println("STARTED");

                // Clear the output area before starting a new search
                outputArea.setText("");
                totalFilesLabel.setText("Total files: 0");
                foundPdfFilesLabel.setText("Found PDF files: 0");

                try {
                    s = new MultiThreadFileSearcher(Path.of(folderPath), keyword);
                    s.search();

                    // The search has finished, call the method to signal it
                    // NON SAPPIAMO QUANDO REALMETNE FINISCE IL FILE WALKER - searchFinished();
                } catch (Exception ex) {
                    outputArea.append("Error: " + ex.getMessage() + "\n");
                    // Still make sure to signal the end of the search in case of error
                    //searchFinished();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    try {
                        s.stop();
                        closeOutputThread = true;
                        System.out.println("STOPPED");
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
                    keyword = keywordField.getText().trim(); // Update the searched keyword
                }
            }
        });

        frame.setVisible(true);
    }

    private static void startOutputArea() {
        if (updateOutputAreaThread != null) {
            try {
                updateOutputAreaThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        updateOutputAreaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                closeOutputThread = false;

                while (true) {
                    if (closeOutputThread) {
                        break;
                    }

                    if (s != null && s.isFinished()) {
                        searchFinished();
                    }

                    // Update the GUI with the search results
                    if (!isSuspended && s != null && s.getResult() != null) {
                        var result = s.getResult();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                String listOfFiles = result.getFiles()
                                        .stream()
                                        .map(Path::toString)
                                        .collect(Collectors.joining("\n"));
                                outputArea.setText(listOfFiles);
                                totalFilesLabel.setText("Total files: " + result.getTotalFiles());
                                foundPdfFilesLabel.setText("Found PDF files: " + result.getFiles().size());
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
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        resumeButton.setEnabled(false);
        suspendButton.setEnabled(false);
        // Reset the flags to indicate that the search has ended and can be resumed again
        closeOutputThread = true;
        isSuspended = false;
        // Compute and update the computing time label
        long endTime = System.nanoTime();
        long computingTimeNano = endTime - startTime;
        double computingTimeMillis = computingTimeNano / 1_000_000.0;
        computingTimeLabel.setText("Computing Time: " + computingTimeMillis + " ms");
    }
}
