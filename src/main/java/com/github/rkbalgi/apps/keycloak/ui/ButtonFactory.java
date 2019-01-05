package com.github.rkbalgi.apps.keycloak.ui;

import javax.swing.JButton;

/**
 *
 */
public class ButtonFactory {

  public static JButton create(String text) {
    return new JButton(text);
  }
}
