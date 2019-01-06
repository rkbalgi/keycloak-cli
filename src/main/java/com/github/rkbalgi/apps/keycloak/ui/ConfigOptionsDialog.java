package com.github.rkbalgi.apps.keycloak.ui;

import com.google.common.collect.Maps;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.keycloak.authorization.client.Configuration;
import scala.Tuple3;

/**
 *
 */
public class ConfigOptionsDialog extends JDialog {


  private Tuple3<Configuration, String, String> config;
  private JTextField serverUrlTextField = TextFieldFactory.create(200);
  private JTextField realmTextField = TextFieldFactory.create(50);
  private JTextField clientNameTextField = TextFieldFactory.create(50);
  private JTextField clientCredsTextField = TextFieldFactory.create(100);
  private JTextField adminUserTextField = TextFieldFactory.create(50);
  private JPasswordField adminPasswordTextField = new JPasswordField(20);


  private ConfigOptionsDialog(JFrame frame,
      Tuple3<Configuration, String, String> config) {
    super(frame, "Override Configuration");
    this.config = config;
  }

  public static ConfigOptionsDialog newDialog(JFrame frame,
      Tuple3<Configuration, String, String> config) {

    ConfigOptionsDialog instance = new ConfigOptionsDialog(frame, config);
    instance.initComponents();
    instance.setModal(true);

    instance.setSize(600, 400);
    instance.setLocationRelativeTo(frame);
    instance.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    return instance;

  }

  private void initComponents() {

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 2));

    serverUrlTextField.setText(config._1().getAuthServerUrl());
    realmTextField.setText(config._1().getRealm());
    clientNameTextField.setText(config._1().getResource());
    clientCredsTextField.setText(config._1().getCredentials().get("secret").toString());
    adminUserTextField.setText(config._2());
    adminPasswordTextField.setText(config._3());

    UIUtils.addRow(panel, new JLabel("Auth Server URL"), serverUrlTextField);
    UIUtils.addRow(panel, new JLabel("Realm"), realmTextField);
    UIUtils.addRow(panel, new JLabel("Client"), clientNameTextField);
    UIUtils.addRow(panel, new JLabel("Client Credentials"), clientCredsTextField);
    UIUtils.addRow(panel, new JLabel("Admin User"), adminUserTextField);
    UIUtils.addRow(panel, new JLabel("Admin Password"), adminPasswordTextField);

    setLayout(new BorderLayout());
    add(panel, BorderLayout.CENTER);

    JButton okBtn = ButtonFactory.create("OK");
    okBtn.addActionListener(e -> {
      config._1().setAuthServerUrl(serverUrlTextField.getText());
      config._1().setRealm(realmTextField.getText());
      config._1().setResource(clientNameTextField.getText());
      final HashMap<String, Object> newCredsMap = Maps.newHashMap();
      newCredsMap.put("secret", clientCredsTextField.getText());
      config._1().setCredentials(newCredsMap);

      config = config
          .copy(config._1(), adminUserTextField.getText(), adminPasswordTextField.getText());
      dispose();

    });
    JButton cancelBtn = ButtonFactory.create("Cancel");
    cancelBtn.addActionListener((e) -> dispose());

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(okBtn);
    buttonPanel.add(cancelBtn);
    add(buttonPanel, BorderLayout.SOUTH);


  }

  public void showDialog() {
    setVisible(true);

  }

  public Tuple3<Configuration, String, String> getConfig() {
    return config;
  }
}
