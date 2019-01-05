package com.github.rkbalgi.apps.keycloak.ui;

import javax.swing.JTextField;

/**
 *
 */
public class TextFieldFactory {

  public static JTextField create(int cols) {
    return new JTextField(cols);
  }
}
