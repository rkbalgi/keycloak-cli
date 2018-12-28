package com.github.rkbalgi.apps.keycloak.cli

/**
  *
  *
  */
case class PermissionDef(val name: String, val resourceName: String, val scopes: List[String], val policies: List[String], val strategy: String) extends CmdObj {

}
