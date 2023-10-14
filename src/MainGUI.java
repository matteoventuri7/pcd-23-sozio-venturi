import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.util.Objects;

public class MainGUI {
    private static IWordSearcher s;
    private static JTextArea outputArea;
    private static JLabel totalFilesLabel;
    private static JLabel foundPdfFilesLabel;
    private static JLabel messageLabel; // New label for computing time
    private static JButton stopButton;
    private static JButton startButton;
    private static JButton suspendButton;
    private static JButton resumeButton;
    private static JComboBox<String> emptySelect;
    private static JFileChooser folderChooser;
    private static JTextField folderField, keywordField;

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Search GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (s != null) {
                    messageLabel.setText("Exiting...");
                    SwingUtilities.invokeLater(()->{
                        try {
                            s.stop();
                            s.close();
                        } catch (Exception e) {
                            // silent
                            System.err.println(e);
                        }
                    });
                }
            }
        });

        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        folderField = new JTextField("Click to select directory...");
        folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setAcceptAllFileFilterUsed(false);

        keywordField = new JTextField();
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
        emptySelect.addItem("Approach: Task");
        emptySelect.addItem("Approach: Events");

        // Create the computing time label
        messageLabel = new JLabel();
        inputPanel.add(messageLabel);

        folderField.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var result = folderChooser.showOpenDialog(folderField);
                if(result == JFileChooser.APPROVE_OPTION){
                    folderField.setText(folderChooser.getSelectedFile().getAbsolutePath());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        emptySelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                instanziateSearcher(folderField.getText(), emptySelect.getSelectedItem().toString(), keywordField.getText().trim());
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!CanStart()) return;

                messageLabel.setText("");
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
                        System.out.println("STOPPED");

                        stopButton.setEnabled(false);
                        resumeButton.setEnabled(false);
                        suspendButton.setEnabled(false);
                        startButton.setEnabled(true); // Enable the "Start" button

                        SwingUtilities.invokeLater(s::stop);

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
                    System.out.println("SUSPENDED");
                    stopButton.setEnabled(false);
                    suspendButton.setEnabled(false);
                    resumeButton.setEnabled(true);

                    messageLabel.setText("Suspending...");

                    SwingUtilities.invokeLater(()->{
                        s.pause();
                        messageLabel.setText("Suspended!");
                    });
                }
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (s != null) {
                    System.out.println("RESUMED");
                    stopButton.setEnabled(true);
                    suspendButton.setEnabled(true);
                    resumeButton.setEnabled(false);

                    messageLabel.setText("Resuming...");

                    SwingUtilities.invokeLater(()->{
                        s.resume();
                        messageLabel.setText("Resumed!");
                    });
                }
            }
        });

        frame.setVisible(true);
    }

    private static boolean CanStart(){
        return !Objects.equals(keywordField.getText(), "") &&
                !Objects.equals(folderField.getText(), "");
    }

    private static void instanziateSearcher(String folderPath, String selectedApproach, String keyword) {
        Path file = Path.of(folderPath);
        switch (selectedApproach) {
            case "Approach: Multithreaded":
                s = new MultiThreadFileSearcher(file, keyword);
                break;
            case "Approach: Virtual Thread":
                s = new VirtualThreadFileSearcher(file, keyword);
                break;
            case "Approach: Task":
                s = new TaskFileSearcher(file, keyword);
                break;
            case "Approach: Events":
                s = new EventsFileSearcher(file, keyword);
                break;
            default:
                break;
        }

        if (s != null) {
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

        messageLabel.setText("Computing Time: " + result.getElapsedTime() + " ms");
    }
}