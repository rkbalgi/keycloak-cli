package com.github.rkbalgi.apps.keycloak.ui;

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
import java.util.Optional;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
  private final JFrame frame;
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private static TextArea notificationsTa = new TextArea(10, 5);
  private JEditorPane editorPane = new JEditorPane("text/plain", EMPTY);
  private JSplitPane splitPane;

  private Tuple3<Configuration, String, String> t3;

  private Font ST_FONT = new Font("Consolas", Font.PLAIN, 14);

  public KeycloakUI() {
    frame = new JFrame("KeycloakUI v0.1");
    frame.setSize(800, 800);
    initComponents();

  }

  public static void main(String[] args) {

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

    //editorPanel.setMinimumSize(new Dimension(400, 400));
    editorPane.setSize(400, Integer.MAX_VALUE);
    editorPane.setFont(this.ST_FONT);
    editorPane.setBackground(Color.CYAN);
    JPanel notificationsPanel = new JPanel();

    // = new TextArea(80, 5);
    notificationsTa.setMaximumSize(new Dimension(400, 100));

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
    frame.setVisible(true);
  }

}
