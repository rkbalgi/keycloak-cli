package com.github.rkbalgi.apps.keycloak.cli

/**
  *
  *
  */
case class UserDef(val userId: String,
                   val firstName: String,
                   val lastName: String, val email: String, val password: String, val roles: Array[String]) extends CmdObj {

}
