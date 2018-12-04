###################################
#
#keycloak config and properties
#
###################################
authServerUrl=http://localhost:8080/auth
clientName=identiti-app
secret=71df4a13-7f58-45be-9f8a-9abac88f97fe
realm=infinx-demo
###################################
#
# Add resources
#
###################################
#add-resource demo-resource;scope1,scope2
#add-resource demo-resource2;scope2
add-resource {"resource_name" :"demo-resource4", "scopes":["scope1","scope3","scope3-execute"]}
####################################
#
## Add permissions
#
####################################
#add-permission {"perm_name":"demo-rsrc-scope1-perm","resource":"demo-resource","scopes":["scope1"],"policies":["user_role_policy"],"policy_strategy":"unanimous"}
#add-permission {"perm_name":"demo-rsrc2-scope1_2-perm","resource":"demo-resource2","scopes":["scope1","scope2"],"policies":["user_role_policy","admin_role_policy"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"demo-rsrc5-scope1_2-perm","resource":"demo-resource2","scopes":["scope1","scope2"],"policies":["user_role_policy","admin_role_policy"],"policy_strategy":"affirmative"}
