package com.github.rkbalgi.apps.keycloak.cli


import java.io.{BufferedReader, Reader}

import com.github.rkbalgi.apps.keycloak.events.KuiEventBus
import com.github.rkbalgi.apps.keycloak.rest.RestLoggingFilter
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.authorization.client.{AuthzClient, Configuration}
import org.keycloak.representations.idm.authorization.DecisionStrategy
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.io.Source

/**
  *
  *
  */

object KeycloakCli extends App {


  private val log = LoggerFactory.getLogger(KeycloakCli.getClass)
  private val commandDefFile = """D:\GitRepos\scripts\keycloak\om\om_iam.kcmd""" //def_local.cmd"


  //runCommandFile(commandDefFile, buildConfig(commandDefFile));

  def runFile(cmdFile: String, t3: (Configuration, String, String)): Unit = {

    runContent(Source.fromFile(cmdFile).reader(), t3);
  }


  def runContent(reader: Reader, t3: (Configuration, String, String)): Unit = {

    //val t3 = buildConfig(commandDefFile)
    val configuration = t3._1
    val adminUser = t3._2
    val password = t3._3


    val restClient = new ResteasyClientBuilder().connectionPoolSize(10).build();
    restClient.register(classOf[RestLoggingFilter]);

    val keycloak = KeycloakBuilder.builder().serverUrl(configuration.getAuthServerUrl)
      .realm(configuration.getRealm)
      .clientId(configuration.getResource)
      .clientSecret(configuration.getCredentials.get("secret").asInstanceOf[String])
      .username(adminUser)
      .password(password).resteasyClient(restClient).build();

    val realmResource = keycloak.realms().realm(configuration.getRealm);
    val clientRsrc = realmResource.clients().get(realmResource.clients().findByClientId(configuration.getResource).get(0).getId)


    KuiEventBus.event(s" ***** ServerUrl = ${configuration.getAuthServerUrl} \t Realm = ${configuration.getRealm} " +
      s"\t Client = ${configuration.getResource} \n\t Admin User = ${adminUser} ***** ");


    val client = realmResource.clients().findByClientId(configuration.getResource)
    assert(client != null && client.size() == 1);


    val authzClient = AuthzClient.create(configuration)

    KuiEventBus.event("Existing resources/permissions and scopes ... \n--------------------------------------------------")

    authzClient.protection().resource().findAll().foreach((r) => {
      val resourceRep = authzClient.protection().resource().findById(r);
      val scopes = for (scope <- resourceRep.getScopes.asScala) yield scope.getName


      KuiEventBus.event(String.format("|%-50s|  with scopes [%s]\n", resourceRep.getName.padTo(50, "#").mkString, scopes.mkString(",")))
    })

    //process commands
    println("Processing commands .. \n--------------------------------------------------")


    new BufferedReader(reader).lines().filter(_.trim.length > 0)
      .filter(!_.startsWith("#"))
      .filter(!_.contains("="))
      .forEach(cmd => {
        KuiEventBus.event(s"processing command .. [${cmd}]")
        val commandName = cmd.substring(0, cmd.indexOf(" ")) //first space ends the command name and the command itself
        val command = cmd.substring(cmd.indexOf(" ") + 1)
        val commandObj = buildCommand(commandName, command);
        commandName match {

          case "add-user" => {
            KeycloakHelperFunctions.addUser(realmResource, commandObj.asInstanceOf[UserDef]);

          }

          case "delete-roles" => {
            DeleteHelper.deleteRoles(realmResource, command);
          }
          case "delete-resources" => {
            DeleteHelper.deleteResources(clientRsrc, command);
          }
          case "delete-policies" => {
            KeycloakHelperFunctions.deletePolicies(clientRsrc, command);
          }
          case "delete-permissions" => {
            KeycloakHelperFunctions.deletePermissions(clientRsrc, command);
          }

          case "add-agg-policy" => {
            KeycloakHelperFunctions.addAggregatePolicy(clientRsrc, commandObj.asInstanceOf[AggregatePolicyDef]);
          }
          case "add-role-based-policy" => {
            KeycloakHelperFunctions.addRoleBasedPolicy(clientRsrc, commandObj.asInstanceOf[RoleBasedPolicyDef]);
          }


          case "add-role" => {
            KeycloakHelperFunctions.addRole(realmResource, commandObj.asInstanceOf[RoleDef])
          }

          case "add-resource" => {
            KeycloakHelperFunctions.createResource(authzClient, commandObj.asInstanceOf[ResourceDef])
          }

          case "add-permission" => {
            KeycloakHelperFunctions.addPermission(clientRsrc, commandObj.asInstanceOf[PermissionDef]);


          }


        }
      })
  }


  def buildCommand(commandName: String, command: String): CmdObj = {

    commandName match {

      case "add-user" => {
        implicit val userReader = Json.reads[UserDef]
        Json.fromJson[UserDef](Json.parse(command)).get
      }
      case "delete-roles" => {
        null
      }
      case "delete-resources" => {
        null
      }
      case "delete-policies" => {
        null
      }
      case "delete-permissions" => {
        null
      }

      case "add-agg-policy" => {

        implicit val decisionStrategyReader = new Reads[DecisionStrategy] {

          override def reads(json: JsValue): JsResult[DecisionStrategy] = {
            val enumVal = json.as[String]
            return JsSuccess(DecisionStrategy.valueOf(enumVal.toUpperCase()))
          }

        }
        implicit val policyRoleReader = Json.reads[PolicyRole]
        implicit val reader = Json.reads[AggregatePolicyDef]
        Json.fromJson[AggregatePolicyDef](Json.parse(command)).get


      }
      case "add-role-based-policy" => {
        implicit val policyRoleReader = Json.reads[PolicyRole]
        implicit val policyReader = Json.reads[RoleBasedPolicyDef]


        Json.fromJson[RoleBasedPolicyDef](Json.parse(command)).get
      }
      case "add-role" => {
        implicit val roleReader = Json.reads[RoleDef]
        Json.fromJson[RoleDef](Json.parse(command)).get

      }
      case "add-resource" => {
        val jsonObj = Json.parse(command)

        val resourceName = (jsonObj \ "resource_name").get.as[String]
        val jScopes = (jsonObj \ "scopes").get.as[JsArray]
        val scopes = new Array[String](jScopes.value.length)
        var i = 0
        for (scope <- jScopes.value) {
          scopes(i) = scope.as[String];
          i += 1
        }

        new ResourceDef(resourceName, scopes)


      }

      case "add-permission" => {

        val jsObj = Json.parse(command)
        val permissionName = (jsObj \ "perm_name").get.as[String]
        val resource = (jsObj \ "resource").get.as[String]
        val scopes = for (s <- (jsObj \ "scopes").as[JsArray].value) yield s.as[String];
        val policies = for (s <- (jsObj \ "policies").as[JsArray].value) yield s.as[String];

        new PermissionDef(permissionName, resource, scopes.toList, policies.toList,
          (Option((jsObj \ "policy_strategy").get.as[String]).getOrElse("AFFIRMATIVE")))

        //val policyStrategy =
        //println(permissionName, resource, scopes, policies, policyStrategy.get)


      }
    }

  }

  def buildConfig(commandDefFile: String): (Configuration, String, String) = {
    //process configuration
    val configuration = new Configuration
    var adminUser: String = ""
    var password: String = ""

    println(s"Reading from file - ${commandDefFile}")

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

    return (configuration, adminUser, password)


  }

}
