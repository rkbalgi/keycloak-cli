package com.github.rkbalgi.apps.keycloak.events;

import com.google.common.eventbus.EventBus;

/**
 *
 */
public class KuiEventBus {

  private static final EventBus eventBus = new EventBus("log");

  static {
    eventBus.register(new EventBusListener());
  }


  public static void event(String event) {
    eventBus.post(event);
  }

}
