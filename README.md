# keycloak-cli
A UI based application for running batch of keycloak commands for creating roles, resources, policies, permissions etc

You can find some other useful resources here - https://gist.github.com/rkbalgi

Here's an example of the script containing the commands -
<pre>
##
authServerUrl=http://localhost:8080/auth
clientName=identiti-app
secret=71df4a13-7f58-45be-9f8a-9abac88f97fe
realm=demo
admin_user=admin_user
password=password
#
###################################
delete-permissions ["*"]
delete-policies ["*"]
delete-resources ["*"]
delete-roles ["*"]
###################################
###################################
#
# Add roles
#
###################################
#
add-role {"name": "role_manager","description": "Manager Role"}
add-role {"name": "role_admin","description": "Admin Role"}
#
###################################
#
# Add policies
#
###################################
add-role-based-policy {"name": "policy_manager_role","roles":[{"name":"role_manager","required":true}],"description": ""}
add-role-based-policy {"name": "policy_admin_role","roles":[{"name":"role_admin","required":true}],"description": ""}

add-agg-policy {"name": "policy_any_role","policies":["policy_manager_role","policy_admin_role"],"strategy": "affirmative"}
##
###################################
#
# Add resources
#
###################################
# This is the master resource definition - Each resource is listed with all possible scopes (actions) applicable for the resource
add-resource {"resource_name" :"doc", "scopes":["view","edit"]}
####################################
#
## Add permissions
#
####################################
# This section lists down permissions applicable to each resource
##
# "doc" resource permissions
add-permission {"perm_name":"perm_doc_edit","resource":"doc","scopes":["edit"],"policies":["policy_admin_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_doc_view","resource":"doc","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"unanimous"}

</pre>
