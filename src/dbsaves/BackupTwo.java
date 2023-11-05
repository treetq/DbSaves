/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbsaves;

/**
 *
 * @author INF08
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.FontUIResource;

public class BackupTwo extends JFrame {
    private DefaultListModel<String> databaseListModel;
    private JList<String> databaseList;
    private JButton backupButton;
    private JButton closeButton;
    private JPanel progressPanel;
    static Path subdirectoryPath ;
    
    public String url = "jdbc:sqlserver://localhost:0;databaseName=master;";
    public String user = "book";
    public String password = "t";

    public BackupTwo()  {
        setTitle("Database Backup Application");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a panel for the database list
        JPanel databaseListPanel = new JPanel();
        databaseListPanel.setLayout(new BorderLayout());
        
        JButton chooseButton = new JButton("Choose Directory");
        chooseButton.setPreferredSize(new Dimension(150, 30));
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = fileChooser.showDialog(null, "Select Directory");

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedDirectory = fileChooser.getSelectedFile();
                    createSubdirectory(selectedDirectory);
                }
            }
        });


        databaseListModel = new DefaultListModel<>();
        databaseList = new JList<>(databaseListModel);
        JScrollPane scrollPane = new JScrollPane(databaseList);
        
        // Set the scroll pane to take up half of the available height
        scrollPane.setPreferredSize(new Dimension(0, 200));

        databaseListPanel.add(scrollPane, BorderLayout.CENTER);

        // Create the backup button
        backupButton = new JButton("Start Backup");
        closeButton= new JButton("Close");

        getAllDBNames(); // Call the method to populate the databaseListModel

        backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBackupProcess();
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        // Create a main panel to hold the database list and the progress panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chooseButton,BorderLayout.WEST);
        mainPanel.add(databaseListPanel, BorderLayout.NORTH);
        mainPanel.add(backupButton, BorderLayout.CENTER);
        mainPanel.add(closeButton,BorderLayout.AFTER_LAST_LINE);

        add(mainPanel, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
    }
    
    private void createSubdirectory(File parentDirectory) {
        if (parentDirectory == null) {
            System.out.println("No directory selected.");
            return;
        }

        String subdirectoryName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        Path parentPath = Paths.get(parentDirectory.getAbsolutePath());
        subdirectoryPath = parentPath.resolve(subdirectoryName);

        if (!Files.exists(subdirectoryPath)) {
            try {
                Files.createDirectories(subdirectoryPath);
                System.out.println("Subdirectory created successfully: " + subdirectoryPath);
            } catch (IOException e) {
                System.err.println("Failed to create subdirectory: " + e.getMessage());
            }
        } else {
            System.out.println("Subdirectory already exists: " + subdirectoryPath);
        }
    }

    private void getAllDBNames() {
        List<String> dbNames = new ArrayList<>();
        try {
            // Replace with your SQL Server connection details
            

            Connection connection = DriverManager.getConnection(url, user, password);
            String sql = "SELECT name FROM sys.databases";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            java.sql.ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                dbNames.add(resultSet.getString("name"));
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Populate the databaseListModel with the obtained database names
        dbNames.forEach(databaseListModel::addElement);
    }

    private void startBackupProcess() {
        backupButton.setEnabled(false);
        closeButton.setEnabled(true);
        databaseList.setEnabled(false);
        processSelectedDatabases();
    }

    private void processSelectedDatabases() {
        List<String> selectedDatabases = databaseList.getSelectedValuesList();

        if (selectedDatabases.isEmpty()) {
            backupButton.setEnabled(true);
            closeButton.setEnabled(true);
            databaseList.setEnabled(true);
            return;
        }

        for (String database : selectedDatabases) {
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            JLabel label = new JLabel("Backup: " + database);

            addProgressComponent(label);
            addProgressComponent(progressBar);

            BackupSwingWorker worker = new BackupSwingWorker(database, progressBar);
            worker.execute();
        }
    }

    private void addProgressComponent(Component component) {
        progressPanel.add(component);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
       
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            
        } catch (InstantiationException ex) {
            Logger.getLogger(BackupTwo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BackupTwo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(BackupTwo.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font(
              "Tw Cen MT", Font.BOLD, 16)));

        /*
         * Create and display the form
         */
         java.awt.EventQueue.invokeLater(new Runnable() {
              
            @Override
            public void run() {
             BackupTwo t = new BackupTwo();
             t.setVisible(true);
             //Définit sa taille : 400 pixels de large et 100 pixels de haut
             //t.setSize(530, 240);
             //Nous demandons maintenant à notre objet de se positionner au centre
             t.setLocationRelativeTo(null);
//             t.setResizable(false);
            }
        }//        java.awt.EventQueue.invokeLater(() -> {
        );
    }
}

class BackupSwingWorker extends SwingWorker<Void, Integer> {
    private String database;
    private JProgressBar progressBar;
    BackupTwo bt = new BackupTwo();

    public BackupSwingWorker(String database, JProgressBar progressBar) {
        this.database = database;
        this.progressBar = progressBar;
    }

    @Override
    protected Void doInBackground() throws IOException {
        try {
            
            //String parentDirectory = "C:\\BACKUP_BIBLIO";
        
            // Get the current date in the 'yymmdd' format
            //String subdirectoryName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

            // Create a Path for the parent directory
            //Path parentPath = Paths.get(parentDirectory);

            // Create a Path for the subdirectory
            //Path subdirectoryPath = parentPath.resolve(subdirectoryName);

            if (!Files.exists( BackupTwo.subdirectoryPath )) {
                try {
                    Files.createDirectories(BackupTwo.subdirectoryPath);
                    System.out.println("Subdirectory created successfully: " + BackupTwo.subdirectoryPath);
                } catch (IOException e) {
                    System.err.println("Failed to create subdirectory: " + e.getMessage());
                }
            } else {
                System.out.println("Subdirectory already exists: " + BackupTwo.subdirectoryPath);
            }
            
           

            Connection connection = DriverManager.getConnection(bt.url, bt.user, bt.password);
            String backupSQL = "BACKUP DATABASE " + database + " TO DISK = '" +BackupTwo.subdirectoryPath+"\\"+ database + ".bak'";
            PreparedStatement preparedStatement = connection.prepareStatement(backupSQL);
            preparedStatement.execute();
            connection.close();

            for (int i = 0; i <= 100; i++) {
                Thread.sleep(100); // Simulate work being done
                publish(i);
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
            publish(0);
        }

        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int value = chunks.get(chunks.size() - 1);
        progressBar.setValue(value);
    }

    @Override
    protected void done() {
        JOptionPane.showMessageDialog(null, "Backup completed!");
    }
}
