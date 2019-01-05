package com.github.rkbalgi.apps.keycloak.events;

import com.github.rkbalgi.apps.keycloak.ui.KeycloakUI;
import com.google.common.eventbus.Subscribe;
import java.time.LocalDateTime;
import javax.swing.SwingUtilities;

/**
 *
 */
public class EventBusListener {


  @Subscribe
  public void newEvent(String event) {

    if (SwingUtilities.isEventDispatchThread()) {
      KeycloakUI.log(String.format("[%s] - Message: %s", LocalDateTime.now(), event));
    } else {
      System.out.println(event);
    }
  }
}
