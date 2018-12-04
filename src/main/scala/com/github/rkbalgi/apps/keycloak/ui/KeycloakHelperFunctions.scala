package com.github.rkbalgi.apps.keycloak.ui


import org.keycloak.admin.client.resource.ClientResource
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.idm.authorization._
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, Json}

/**
  *
  *
  */
object KeycloakHelperFunctions {

  private val log = LoggerFactory.getLogger("KeycloakHelperFunctions")

  def createResource(authzClient: AuthzClient, command: String) = {

    val jsonObj = Json.parse(command)

    val resourceName = (jsonObj \ "resource_name").get.as[String]
    val scopes = (jsonObj \ "scopes").get.as[JsArray]
    assert(scopes.value.length > 0, "1 or more scopes should be provided with add-scopes command")
    log.debug(s"Adding resource - ${resourceName}  with scopes - ${scopes.value.mkString("[", ",", "]")}")

    val resource = Option(authzClient.protection().resource().findByName(resourceName))

    if (resource.isDefined) {
      log.error(s"resource ${resourceName} exists and cannot be added");
      throw new RuntimeException();
    } else {

      val newResource = new ResourceRepresentation()
      newResource.setName(resourceName)

      for (scope <- scopes.value) {
        newResource.addScope(scope.as[String])
      }
      val response = authzClient.protection().resource().create(newResource);
      log.info(s"Resource ${resourceName} created with ID = ${response.getId}"); // response.getId

    }

  }

  def addPermission(adminClient: ClientResource, command: String) = {

    val jsObj = Json.parse(command)
    val permissionName = (jsObj \ "perm_name").get.as[String]
    val resource = (jsObj \ "resource").get.as[String]
    val scopes = (jsObj \ "scopes").as[JsArray]
    val policies = (jsObj \ "policies").as[JsArray]

    val policyStrategy = jsObj \ "policy_strategy"
    println(permissionName, resource, scopes, policies, policyStrategy.get)

    val newScopePerm = new ScopePermissionRepresentation
    //set policies
    newScopePerm.setName(permissionName)

    for (policy <- policies.value) newScopePerm.addPolicy(policy.as[String])

    //set scopes
    for (scope <- scopes.value) newScopePerm.addScope(scope.as[String])

    newScopePerm.setDecisionStrategy(DecisionStrategy.valueOf(policyStrategy.get.as[String].toUpperCase))
    newScopePerm.setLogic(Logic.POSITIVE)
    newScopePerm.addResource(resource)

    log.debug {
      s"Adding permission - ${permissionName}"
    };

    val response = adminClient.authorization().permissions().scope().create(newScopePerm).
      readEntity(classOf[ScopePermissionRepresentation])
    if (response != null) {
      log.debug(s"Permission ${permissionName} created with ID - ${response.getId}")
    } else {
      log.error(s"Failed to create permission - ${permissionName}");
    }

  }
}
