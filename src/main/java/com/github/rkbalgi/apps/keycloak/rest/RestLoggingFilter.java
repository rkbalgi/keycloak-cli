package com.github.rkbalgi.apps.keycloak.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 */
@Provider
public class RestLoggingFilter implements ClientResponseFilter {


  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
      throws IOException {

    /*System.out.println(requestContext.getUri().toString());
    System.out.println(responseContext.getStatusInfo());
    byte[] response = IOUtils
        .readFully(responseContext.getEntityStream(), responseContext.getLength());
    System.out.println("Response =  .. " + new String(response, "UTF-8"));
    responseContext.setEntityStream(new ByteArrayInputStream(response));
    */

  }
}
