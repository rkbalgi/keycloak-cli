package com.github.rkbalgi.apps.keycloak.cli

import java.util.function.Predicate

import javax.ws.rs.NotFoundException
import org.keycloak.admin.client.resource.{ClientResource, RealmResource}
import org.keycloak.representations.idm.RoleRepresentation
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

/**
  *
  *
  */
object DeleteHelper {

  private val log = LoggerFactory.getLogger("DeleteHelper")


  def deleteRoles(adminClient: RealmResource, command: String) = {

    val toDelete = Json.fromJson[Array[String]](Json.parse(command))
    val notDefaultRole: Predicate[_ >: RoleRepresentation] = r => !r.getName.equals("uma_authorization") && !r.getName.equals("offline_access")

    if (toDelete.isSuccess) {
      if (toDelete.get(0).equals("*")) {
        adminClient.roles().list().stream()
          .filter(notDefaultRole)
          .forEach(r => {
            log.info(s"Deleting role [${r.getName}] with ID - ${r.getId}");
            adminClient.roles().deleteRole(r.getName)
          })
      } else {

        adminClient.roles().list().stream().filter(r => toDelete.get.contains(r.getName))
          .filter(notDefaultRole)
          .forEach(r => {
            log.info(s"Deleting role [${r.getName}] with ID - ${r.getId}");
            adminClient.roles().deleteRole(r.getName)
          })
      }
    } else {
      log.error("Incorrectly specified delete-roles command!")
    }
  }

  /**
    * Deletes resources from keycloak
    *
    * @param adminClient
    * @param command
    */
  def deleteResources(adminClient: ClientResource, command: String) = {

    val toDelete = Json.fromJson[Array[String]](Json.parse(command))

    if (toDelete.isSuccess) {
      if (toDelete.get(0).equals("*")) {
        adminClient.authorization().resources().resources().forEach(r => {
          log.info(s"Deleting resource [${r.getName}] with ID - ${r.getId}");
          adminClient.authorization().resources().resource(r.getId).remove();
        })
      } else {
        adminClient.authorization().resources().resources()
          .stream()
          .filter(r => toDelete.get.contains(r.getName))
          .forEach(r => {
            log.info(s"Deleting resource [${r.getName}] with ID - ${r.getId}");
            adminClient.authorization().resources().resource(r.getId).remove();
          })
      }
    } else {
      log.error("Incorrectly specified delete-resources command!")
    }
  }


  def deletePolicies(adminClient: ClientResource, command: String) = {


    def deleteAllPolicies() = {

      adminClient.authorization().policies().policies().forEach(p => {
        log.info(s"Deleting policy [${p.getName}] of type [${p.getType}] with ID - ${p.getId}")
        try {
          adminClient.authorization().permissions().scope().findById(p.getId).remove()
        } catch {
          case e: NotFoundException => log.error(s"policy ${p.getName} could not be found, was probably deleted by a previous action")
        }
      })
    }

    def deletePolicies(toDelete: Array[String]) = {
      val allPolicies = adminClient.authorization().policies().policies()
      toDelete.foreach(p => {
        val notfound = allPolicies.stream().map[String](p2 => p2.getName).noneMatch(p3 => p.equals(p3))
        notfound match {
          case true => log.warn(s"policy - [${p}] not found!")
          case _ =>
        }
      })

      allPolicies.stream().filter(p => toDelete.contains(p.getName)).forEach(p => {
        log.info(s"Deleting policy [${p.getName}] of type [${p.getType}] -ID = ${p.getId}")
        try {
          adminClient.authorization().permissions().scope().findById(p.getId).remove()
        } catch {
          case e: NotFoundException => log.error(s"policy ${p.getName} could not be found, was probably deleted by a previous action")
        }
      })
    }


    val toDelete = Json.fromJson[Array[String]](Json.parse(command))

    if (toDelete.isSuccess) {
      if (toDelete.get(0).equals("*")) {
        deleteAllPolicies()
      } else {
        deletePolicies(toDelete.get)
      }
    } else {
      log.error("Incorrectly specified delete-policies command!")
    }

  }

  def deletePermissions(adminClient: ClientResource, command: String): Unit = {
    def deleteAllPermissions() = {

      adminClient.authorization().policies().policies().forEach(p => {
        adminClient.authorization().policies().policy(p.getId).dependentPolicies().forEach(dp => {
          //println(p.getName + " " + dp.getName + " " + dp.getType)
          if (dp.getType.equals("scope") || dp.getType.equals("resource")) {
            //this is a permission
            log.debug(s"Deleting permission ${dp.getName} of type ${dp.getType} .... ")
            if (dp.getType.equals("scope")) {
              adminClient.authorization().permissions().scope().findById(dp.getId).remove()
            } else {
              adminClient.authorization().permissions().resource().findById(dp.getId).remove()
            }

          }

        })
      })
    }

    def deletePermissions(toDelete: Array[String]): Unit = {

      adminClient.authorization().policies().policies().forEach(p => {
        adminClient.authorization().policies().policy(p.getId)
          .dependentPolicies()
          .stream()
          .filter(dp => dp.getType.equals("scope") || dp.getType.equals("resource"))
          .filter(dp => toDelete.contains(dp.getName))
          .forEach(dp => {
            //this is a permission
            log.debug(s"Deleting permission ${dp.getName} of type ${dp.getType} .... ")
            if (dp.getType.equals("scope")) {
              adminClient.authorization().permissions().scope().findById(dp.getId).remove()
            } else {
              adminClient.authorization().permissions().resource().findById(dp.getId).remove()
            }


          })
      })
    }


    val toDelete = Json.fromJson[Array[String]](Json.parse(command))

    if (toDelete.isSuccess) {
      if (toDelete.get(0).equals("*")) {
        deleteAllPermissions()
      } else {
        deletePermissions(toDelete.get)
      }
    } else {
      log.error("Incorrectly specified delete-permissions command!")
    }

  }
}
