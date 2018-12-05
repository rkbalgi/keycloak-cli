package com.github.rkbalgi.apps.keycloak.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 *
 */
public class KeycloakUI {


  private static final String EMPTY = new String(
      "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
  private final JFrame frame;
  private JMenuBar menuBar;
  private JMenu fileMenu;
  private TextArea notificationsTa;
  private JEditorPane editorPane = new JEditorPane("text/plain", EMPTY);
  //private JPanel editorPanel;
  private JPanel notificationsPanel;
  private JSplitPane splitPane;

  public KeycloakUI() {
    frame = new JFrame("KeycloakUI v0.1");
    frame.setSize(600, 600);
    initComponents();

  }

  public static void main(String[] args) {

    SwingUtilities.invokeLater(() -> {
      new KeycloakUI().show();
    });

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
          String content = new String(
              Files.readAllBytes(
                  new File(chooserDialog.getDirectory(), selectedFile.get()).toPath()));
          editorPane.setText(content);
          //System.out.println(content);
          //editorPanel.add(editorPane);
        } catch (IOException e1) {
          notificationsTa.append(
              "failed to read selected file - " + selectedFile.get() + " Error = " + e1
                  .getMessage());
          e1.printStackTrace();
        }


      }


    });

    JPanel editorPanel = new JPanel();

    //editorPanel.setMinimumSize(new Dimension(400, 400));
    editorPane.setSize(400, Integer.MAX_VALUE);
    editorPane.setFont(new Font("Consolas", Font.PLAIN, 14));
    //editorPane.setPreferredSize(new Dimension(400, 400));
    //editorPane.setMaximumSize(new Dimension(400, 400));
    editorPane.setBackground(Color.CYAN);
    //editorPanel.add(editorPane);
    notificationsPanel = new JPanel();

    notificationsTa = new TextArea(80, 5);
    notificationsTa.setMaximumSize(new Dimension(400, 100));
    //notificationsPanel.add(new JScrollPane(notificationsTa));

    /*JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(notificationsPanel, BorderLayout.SOUTH);
    panel.add(new JScrollPane(editorPanel), BorderLayout.CENTER);
*/

    splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(.8);
    splitPane.setResizeWeight(.8);
    splitPane.setTopComponent(new JScrollPane(editorPane));
    splitPane.setBottomComponent(new JScrollPane(notificationsTa));

    frame.setLayout(new BorderLayout());
    frame.add(splitPane, BorderLayout.CENTER);

    fileMenu.add(openCmdFileMi);
    JMenuItem closeAppFileMi = new JMenuItem("Exit", KeyEvent.VK_X);
    closeAppFileMi.addActionListener((e) -> {
      System.exit(0);
    });
    fileMenu.add(closeAppFileMi);

    menuBar.add(fileMenu);
    frame.setJMenuBar(menuBar);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  public void show() {
    frame.setVisible(true);
  }

}
