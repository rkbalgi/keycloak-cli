package com.github.rkbalgi.apps.keycloak.cli


import org.apache.http.HttpStatus
import org.keycloak.admin.client.resource.{ClientResource, RealmResource}
import org.keycloak.authorization.client.AuthzClient
import org.keycloak.representations.idm.authorization._
import org.keycloak.representations.idm.{CredentialRepresentation, RoleRepresentation, UserRepresentation}
import org.slf4j.LoggerFactory

/**
  *
  *
  */
object KeycloakHelperFunctions {

  private val LOG = LoggerFactory.getLogger("KeycloakHelperFunctions")


  def addUser(realmResource: RealmResource, userDef: UserDef): Unit = {
    import scala.collection.JavaConverters._
    val userRep = new UserRepresentation
    userRep.setUsername(userDef.userId)
    userRep.setEmail(userDef.email)
    userRep.setFirstName(userDef.firstName)
    userRep.setLastName(userDef.lastName)

    val creds = new CredentialRepresentation
    creds.setTemporary(false)
    creds.setType("secret")

    userRep.setCredentials(List(creds).asJava)
    userRep.setRealmRoles(userDef.roles.toList.asJava)
    val response = realmResource.users().create(userRep);
    if (Option(response).isDefined) {
      if (response.getStatus == 201) {
        realmResource.users().search(userDef.userId)
        var res = realmResource.users().search(userDef.userId)
        if (res.size() == 1) {
          LOG.info("User {} created with ID - {}", userDef.userId, (res.get(0).getId).asInstanceOf[Any])
        } else {
          LOG.info("Too many matches for {} :/", userDef.userId)
        }

      } else {
        LOG.error("Failed to create user - {}, http status code = {}", userDef.userId, response.getStatus);
      }
    } else {
      LOG.error("Failed to create user {}", userDef);
    }


  }


  def addRole(realmResource: RealmResource, roleDef: RoleDef): Unit = {


    val roleRep = new RoleRepresentation
    roleRep.setName(roleDef.name)
    roleRep.setDescription(roleDef.description.getOrElse("No description provided"))
    realmResource.roles().create(roleRep)
    val roleId = realmResource.roles().get(roleDef.name).toRepresentation.getId
    LOG.info(s"Role ${roleDef.name} created with ID ${roleId}")


  }


  def createResource(authzClient: AuthzClient, command: ResourceDef) = {


    LOG.debug("Creating resource .. {}", command)
    assert(command.scopes.length > 0, "1 or more scopes should be provided with add-scopes command")
    LOG.debug(s"Adding resource - ${command.name}  with scopes - ${command.scopes.mkString}")

    val resource = Option(authzClient.protection().resource().findByName(command.name))

    if (resource.isDefined) {
      LOG.error(s"resource ${command.name} exists and cannot be added");
      throw new RuntimeException();
    } else {

      val newResource = new ResourceRepresentation()
      newResource.setName(command.name)

      for (scope <- command.scopes) {
        newResource.addScope(scope)
      }
      val response = authzClient.protection().resource().create(newResource);
      LOG.info(s"Resource ${command.name} created with ID = ${response.getId}"); // response.getId

    }

  }

  def addPermission(adminClient: ClientResource, command: PermissionDef) = {

    val newScopePerm = new ScopePermissionRepresentation
    //set policies
    newScopePerm.setName(command.name)

    command.policies.foreach(arg => newScopePerm.addPolicy(arg))
    command.scopes.foreach(arg => newScopePerm.addScope(arg))


    newScopePerm.setDecisionStrategy(DecisionStrategy.valueOf(command.strategy.toUpperCase))
    newScopePerm.setLogic(Logic.POSITIVE)
    newScopePerm.addResource(command.resourceName)

    LOG.debug {
      s"Adding permission - ${command.name}"
    };

    val response = adminClient.authorization().permissions().scope().create(newScopePerm).
      readEntity(classOf[ScopePermissionRepresentation])
    if (response != null) {
      LOG.debug(s"Permission ${command.name} created with ID - ${response.getId}")
    } else {
      LOG.error(s"Failed to create permission - ${command.name}");
    }

  }

  def addRoleBasedPolicy(adminClient: ClientResource, policyDef: RoleBasedPolicyDef): Unit = {

    LOG.debug(s"Creating policy - ${policyDef.name}")
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
      LOG.info(s"Created role based policy - with ID - ${result.getId}")
    } else {
      LOG.error(s"Failed to create role based policy - Status code ${response.getStatus}")
    }
  }

  /** Adds a aggregate policy in keycloak */

  def addAggregatePolicy(adminClient: ClientResource, policyDef: AggregatePolicyDef) = {


    val policyRep = new AggregatePolicyRepresentation
    policyRep.setName(policyDef.name)
    for (policy <- policyDef.policies) policyRep.addPolicy(policy)
    policyRep.setDecisionStrategy(policyDef.strategy)

    val response = adminClient.authorization().policies().aggregate().create(policyRep);
    if (response.getStatus == HttpStatus.SC_CREATED) {
      val result = response.readEntity(classOf[RolePolicyRepresentation])
      LOG.info(s"Created aggregate based policy - ${result.getName} with ID - ${result.getId}");
    } else {
      LOG.error(s"Failed to aggregate role based policy - Status code ${response.getStatus}");
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
