package com.github.rkbalgi.apps.keycloak.cli


import com.github.rkbalgi.apps.keycloak.rest.RestLoggingFilter
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.authorization.client.{AuthzClient, Configuration}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.io.Source

/**
  *
  *
  */

object KeycloakCli extends App {


  val keycloak = KeycloakBuilder.builder().serverUrl(configuration.getAuthServerUrl)
    .realm(configuration.getRealm)
    .clientId(configuration.getResource)
    .clientSecret(configuration.getCredentials.get("secret").asInstanceOf[String])
    .username(adminUser)
    .password(password).resteasyClient(restClient).build();

  private val log = LoggerFactory.getLogger(KeycloakCli.getClass)


  val configuration = new Configuration
  val adminClient = realmResource.clients().get(client.get(0).getId)
  private val commandDefFile = "C:\\Users\\Admin\\Desktop\\om_cmds.cmd" //def_local.cmd"
  //process configuration
  Source.fromFile(commandDefFile).getLines().filter(!_.startsWith("#")).filter(_.contains("="))
    .map(prop => {
      val parts = prop.split("=")
      (parts(0), parts(1))
    }).
    foreach(propTuple => {
      propTuple._1 match {
        case "authServerUrl" => configuration.setAuthServerUrl(propTuple._2)
        case "realm" => configuration.setRealm(propTuple._2)
        case "clientName" => configuration.setResource(propTuple._2);
        case "secret" => configuration.setCredentials(Map[String, AnyRef]("secret" -> propTuple._2).asJava)
        case "admin_user" => adminUser = propTuple._2
        case "password" => password = propTuple._2
      }
    })

  val restClient = new ResteasyClientBuilder().connectionPoolSize(10).build();
  restClient.register(classOf[RestLoggingFilter]);
  var adminUser: String = ""


  val realmResource = keycloak.realms().realm(configuration.getRealm);
  println(s" ***** ServerUrl = ${configuration.getAuthServerUrl} \t Realm = ${configuration.getRealm} \t Client = ${configuration.getResource} ***** ");


  val client = realmResource.clients().findByClientId(configuration.getResource)
  assert(client != null && client.size() == 1);
  var password: String = ""


  val authzClient = AuthzClient.create(configuration)

  println("Existing resources/permissions and scopes ... \n--------------------------------------------------")

  authzClient.protection().resource().findAll().foreach((r) => {
    val resourceRep = authzClient.protection().resource().findById(r);
    val scopes = for (scope <- resourceRep.getScopes.asScala) yield scope.getName


    printf("|%-50s|  with scopes [%s]\n", resourceRep.getName.padTo(50, "#").mkString, scopes.mkString(","))
  })

  //process commands
  println("Processing commands .. \n--------------------------------------------------")


  Source.fromFile(commandDefFile).getLines().filter(_.trim.length > 0)
    .filter(!_.startsWith("#"))
    .filter(!_.contains("="))
    .foreach(cmd => {
      println(s"processing command .. [${cmd}]")
      val commandName = cmd.substring(0, cmd.indexOf(" ")) //first space ends the command name and the command itself
      val command = cmd.substring(cmd.indexOf(" ") + 1)
      commandName match {
        case "delete-roles" => {
          DeleteHelper.deleteRoles(realmResource, command);
        }
        case "delete-resources" => {
          DeleteHelper.deleteResources(adminClient, command);
        }
        case "delete-policies" => {
          KeycloakHelperFunctions.deletePolicies(adminClient, command);
        }
        case "delete-permissions" => {
          KeycloakHelperFunctions.deletePermissions(adminClient, command);
        }

        case "add-agg-policy" => {
          KeycloakHelperFunctions.addAggregatePolicy(adminClient, command);
        }
        case "add-role-based-policy" => {
          KeycloakHelperFunctions.addRoleBasedPolicy(adminClient, command);
        }
        case "add-role" => {
          KeycloakHelperFunctions.addRole(realmResource, command);
        }
        case "add-resource" => {
          KeycloakHelperFunctions.createResource(authzClient, command)
        }

        case "add-permission" => {
          KeycloakHelperFunctions.addPermission(adminClient, command);


        }


      }
    })


}
