package com.github.rkbalgi.apps.keycloak;


import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;
import scala.Tuple2;

/**
 * This utility replaces the ID's (UUID) from a keycloak exported realm so that it enables cloning
 * of the realm (http://lists.jboss.org/pipermail/keycloak-user/2018-July/014547.html)
 */

public class IdReplacer {


  public static void main(String[] args) throws IOException {
    new IdReplacer()
        .replace(new File("C:\\Users\\Admin\\Downloads\\realm-export (1).json"), "test_realm");
  }


  public void replace(File file, String realmName) throws IOException {

    final String opFileName = realmName + "_1.json";

    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    ObjectNode rootNode = (ObjectNode) objectMapper.readTree(file);

    Lists.newArrayList(rootNode.elements()).stream().flatMap(n -> flatten(rootNode, n))
        .filter(n -> n._1.isObject() && n._1.has("id"))
        .forEach(v -> ((ObjectNode) v._1).put("id", UUID.randomUUID().toString()));

    rootNode.put("id", realmName);
    rootNode.put("realm", realmName);

    ArrayNode rolesArray = (ArrayNode) rootNode.get("roles").get("realm");
    rolesArray.forEach(r -> ((ObjectNode) r).put("containerId", realmName));

    objectMapper.writer().with(new DefaultPrettyPrinter())
        .writeValue(new File(file.getParent(), opFileName), rootNode);

  }

  private Stream<Tuple2<JsonNode, JsonNode>> flatten(JsonNode parent, JsonNode n) {

    if (n.isContainerNode()) {
      return Lists.newArrayList(n.elements()).stream().flatMap(n2 -> flatten(n, n2));
    } else {
      return Stream.of(new Tuple2<>(parent, n));

    }

  }

}