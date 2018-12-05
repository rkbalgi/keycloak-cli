package com.github.rkbalgi.apps.keycloak.cli

import org.keycloak.representations.idm.authorization.DecisionStrategy

/**
  *
  *
  */
case class AggregatePolicyDef(name: String, description: Option[String], policies: List[String], strategy: DecisionStrategy) {


}
