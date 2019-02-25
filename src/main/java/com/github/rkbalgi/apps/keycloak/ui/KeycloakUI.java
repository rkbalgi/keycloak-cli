package com.github.rkbalgi.apps.keycloak.ui;

import com.github.rkbalgi.apps.keycloak.IdReplacer;
import com.github.rkbalgi.apps.keycloak.cli.KeycloakCli;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.keycloak.authorization.client.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple3;

/**
 *
 */
public class KeycloakUI {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakUI.class);


  private static final String EMPTY = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
  private static final String APPLICATION_NAME = "KeycloakUI";
  private static final String APPL_VERSION = "v0.2";
  private final JFrame frame;
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private static TextArea notificationsTa = new TextArea(10, 5);
  private JEditorPane editorPane = new JEditorPane("text/plain", EMPTY);
  private JSplitPane splitPane;

  private Tuple3<Configuration, String, String> t3;

  private Font ST_FONT = new Font("Consolas", Font.PLAIN, 14);

  public KeycloakUI() {
    frame = new JFrame(String.format("%s %s", APPLICATION_NAME, APPL_VERSION));
    frame.setSize(1000, 800);
    initComponents();

  }

  public static void main(String[] args) {

    final List<String> argList = Arrays.asList(args);
    if (argList.contains("--file")) {
      int i = argList.indexOf("--file");
      if (i + 1 < args.length) {
        File script = new File(argList.get(i + 1));
        if (script.exists()) {
          if (script.isFile()) {
            try {
              KeycloakCli.runFile(script);
              System.out.println("script executed without errors.");
              return;
            } catch (Throwable e) {
              e.printStackTrace();
              System.exit(3);
            }
          } else {
            System.err.println("--file argument should be a regular file");
            System.exit(2);
          }
        } else {
          System.err.println("Invalid or non-existent file - " + script.getName());
          System.exit(2);
        }
      } else {
        System.err.println(
            "--file parameter requires a file argument (Example: --file /tmp/scripts/file.kcmd) ");
        System.exit(1);
      }

    }

    SwingUtilities.invokeLater(() -> {
      new KeycloakUI().show();
    });

  }

  public static void log(String msg) {
    notificationsTa.append(msg + "\n");

  }

  private void initComponents() {

    menuBar = new JMenuBar();
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem openCmdFileMi = new JMenuItem("Open File", KeyEvent.VK_O);
    openCmdFileMi.addActionListener((e) -> {
      FileDialog chooserDialog = new FileDialog(this.frame, "Open File");
      chooserDialog.setMode(FileDialog.LOAD);
      chooserDialog.setFilenameFilter((dir, name) -> {
        if (name.endsWith(".cmd")) {
          return true;
        }
        return false;
      });

      chooserDialog.show();

      Optional<String> selectedFile = Optional.ofNullable(chooserDialog.getFile());
      if (selectedFile.isPresent()) {
        //editorPane = new JEditorPane();
        try {
          final File cmdFile = new File(chooserDialog.getDirectory(), selectedFile.get());
          String content = new String(
              Files.readAllBytes(
                  cmdFile.toPath()));
          editorPane.setText(content);

        } catch (IOException e1) {
          notificationsTa.append(
              "failed to read selected file - " + selectedFile.get() + " Error = " + e1
                  .getMessage());
          LOG.error("", e1);
        }


      }


    });

    JMenuItem runFileMi = new JMenuItem("Run File", KeyEvent.VK_R);
    runFileMi.addActionListener((ev) -> {

      try {
        loadAndOverrideConfig();
        KeycloakCli.runContent(new StringReader(editorPane.getText()), t3);
        JOptionPane.showMessageDialog(frame, "Done.", "", JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        LOG.error("Failed to run file ", e);
        notificationsTa.append(e.getMessage());
        JOptionPane.showMessageDialog(this.frame, "Error -" + e.getMessage(), "",
            JOptionPane.ERROR_MESSAGE);
      }

    });

    JMenuItem cloneRealmMi = new JMenuItem("Clone Realm", KeyEvent.VK_C);
    cloneRealmMi.addActionListener((ev) -> {

      try {

        JFileChooser dialog = new JFileChooser();
        dialog.showOpenDialog(frame);
        File exportFile = dialog.getSelectedFile();
        if (exportFile != null && exportFile.exists()) {

          String newRealmName = JOptionPane
              .showInputDialog(frame, "New Realm Name",
                  "new-realm-??" + LocalDateTime.now().toString());

          String opFileName = new IdReplacer().replace(exportFile, newRealmName);

          JOptionPane.showMessageDialog(frame, "Done. New file = " + opFileName, "Info",
              JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e) {
        LOG.error("Failed to clone realm ", e);
        notificationsTa.append(e.getMessage());
        JOptionPane.showMessageDialog(this.frame, "Error -" + e.getMessage(), "",
            JOptionPane.ERROR_MESSAGE);
      }

    });

    //editorPanel.setMinimumSize(new Dimension(400, 400));
    editorPane.setSize(400, Integer.MAX_VALUE);
    editorPane.setFont(this.ST_FONT);
    editorPane.setBackground(Color.BLACK);
    editorPane.setForeground(Color.YELLOW);

    // = new TextArea(80, 5);
    notificationsTa.setMaximumSize(new Dimension(400, 100));
    notificationsTa.setFont(ST_FONT);

    splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(.8);
    splitPane.setResizeWeight(.8);
    splitPane.setTopComponent(new JScrollPane(editorPane));
    splitPane.setBottomComponent(new JScrollPane(notificationsTa));

    frame.setLayout(new BorderLayout());
    frame.add(splitPane, BorderLayout.CENTER);

    fileMenu.add(openCmdFileMi);
    fileMenu.add(runFileMi);
    fileMenu.add(cloneRealmMi);
    JMenuItem closeAppFileMi = new JMenuItem("Exit", KeyEvent.VK_X);
    closeAppFileMi.addActionListener((e) -> {
      System.exit(0);
    });
    fileMenu.add(closeAppFileMi);

    menuBar.add(fileMenu);
    frame.setJMenuBar(menuBar);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  private void loadAndOverrideConfig() {

    t3 = KeycloakCli
        .buildConfig(editorPane.getText());

    final ConfigOptionsDialog dialog = ConfigOptionsDialog.newDialog(this.frame, t3);

    dialog.showDialog();
    t3 = dialog.getConfig();


  }

  public void show() {
    log(String
        .format("[%s]  -- %s: %s started. ", LocalDateTime.now(), APPLICATION_NAME, APPL_VERSION));
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

}
