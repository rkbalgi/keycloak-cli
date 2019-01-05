package com.github.rkbalgi.apps.keycloak.ui;

import com.google.common.base.Preconditions;
import java.awt.GridLayout;
import javax.swing.JComponent;

/**
 *
 */
public class UIUtils {

  //The containers layout should be a GridBag!
  public static void addRow(JComponent container, JComponent... components) {
    Preconditions.checkArgument(container.getLayout().getClass() == GridLayout.class);
    for (JComponent c : components) {
      container.add(c);

    }
  }
}
