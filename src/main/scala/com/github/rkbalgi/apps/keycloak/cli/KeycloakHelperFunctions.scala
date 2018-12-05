package com.github.rkbalgi.apps.keycloak.cli


import org.apache.http.HttpStatus
import org.keycloak.admin.client.resource.{ClientResource, RealmResource}
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.authorization._
import org.slf4j.LoggerFactory
import play.api.libs.json._

/**
  *
  *
  */
object KeycloakHelperFunctions {

  def addRole(realmResource: RealmResource, command: String): Unit = {

    implicit val roleReader = Json.reads[RoleDef]
    val roleDef = Json.fromJson[RoleDef](Json.parse(command)).get
    log.debug(s"Creating role -  ${roleDef}")


    val roleRep = new RoleRepresentation
    roleRep.setName(roleDef.name)
    roleRep.setDescription(roleDef.description.getOrElse("No description provided"))
    realmResource.roles().create(roleRep)
    val roleId = realmResource.roles().get(roleDef.name).toRepresentation.getId
    log.info(s"Role ${roleDef.name} created with ID ${roleId}")


  }


  private val log = LoggerFactory.getLogger("KeycloakHelperFunctions")

  def createResource(authzClient: AuthzClient, command: String) = {

    val jsonObj = Json.parse(command)

    val resourceName = (jsonObj \ "resource_name").get.as[String]
    val scopes = (jsonObj \ "scopes").get.as[JsArray]
    assert(scopes.value.length > 0, "1 or more scopes should be provided with add-scopes command")
    log.debug(s"Adding resource - ${resourceName}  with scopes - ${
      scopes.value.mkString("[", "," +
        "", "]")
    }")

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

    newScopePerm.setDecisionStrategy(DecisionStrategy.valueOf(policyStrategy.get.as[String]
      .toUpperCase))
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

  def addRoleBasedPolicy(adminClient: ClientResource, command: String): Unit = {

    implicit val policyRoleReader = Json.reads[PolicyRole]
    implicit val policyReader = Json.reads[RoleBasedPolicyDef]


    val policyDef = Json.fromJson[RoleBasedPolicyDef](Json.parse(command)).get
    log.debug(s"Creating policy - ${policyDef.name}")
    policyDef.roles.foreach(println _)

    val policyRep = new RolePolicyRepresentation

    policyRep.setName(policyDef.name)
    policyRep.setLogic(Logic.POSITIVE)
    policyDef.roles.foreach(r => policyRep.addRole(r.name, r.required))
    //don't know what decision strategy means here - makes sense on a permission
    policyRep.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE)


    val response = adminClient.authorization().policies().role().create(policyRep)
    if (response.getStatus == HttpStatus.SC_CREATED) {
      val result = response.readEntity(classOf[RolePolicyRepresentation])
      log.info(s"Created role based policy - with ID - ${result.getId}")
    } else {
      log.error(s"Failed to create role based policy - Status code ${response.getStatus}")
    }
  }

  /** Adds a aggregate policy in keycloak */

  def addAggregatePolicy(adminClient: ClientResource, command: String) = {

    implicit val decisionStrategyReader = new Reads[DecisionStrategy] {

      override def reads(json: JsValue): JsResult[DecisionStrategy] = {
        val enumVal = json.as[String]
        return JsSuccess(DecisionStrategy.valueOf(enumVal.toUpperCase()))
      }

    }

    implicit val reader = Json.reads[AggregatePolicyDef]
    val policyDef = Json.fromJson[AggregatePolicyDef](Json.parse(command)).get

    val policyRep = new AggregatePolicyRepresentation
    policyRep.setName(policyDef.name)
    for (policy <- policyDef.policies) policyRep.addPolicy(policy)
    policyRep.setDecisionStrategy(policyDef.strategy)

    val response = adminClient.authorization().policies().aggregate().create(policyRep);
    if (response.getStatus == HttpStatus.SC_CREATED) {
      val result = response.readEntity(classOf[RolePolicyRepresentation])
      log.info(s"Created aggregate based policy - ${result.getName} with ID - ${result.getId}");
    } else {
      log.error(s"Failed to aggregate role based policy - Status code ${response.getStatus}");
    }


  }


  /** Deletes permissions from keycloak. If the command (JSON) contains a array property "name"
    * with the first value "*"
    * then all permissions are deleted!!, else all the listed permissions are deleted
    *
    * */
  def deletePermissions(adminClient: ClientResource, command: String): Unit = {

    DeleteHelper.deletePermissions(adminClient, command)

  }

  /**
    * Deletes policies from keycloak - See behaviour here - [[KeycloakHelperFunctions
    * .deletePermissions()]]
    *
    * @param adminClient
    * @param command
    */
  def deletePolicies(adminClient: ClientResource, command: String): Unit = {

    DeleteHelper.deletePolicies(adminClient, command)

  }
}
