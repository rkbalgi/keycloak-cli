## This is the roles/resources script is for Operations Manager
## FileName: dev_om.cmd
## Create Date: 4/12/2018
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
# This is the master resource definition - Each resource is listed with all possible scopes (actions) applicable for the resource
add-resource {"resource_name" :"case", "scopes":["view","edit","deactivate","comment","add_attach","delete_attach","add_tag","set_flag","assign","audit"]}
add-resource {"resource_name" :"job", "scopes":["view","edit","add_tag"]}
add-resource {"resource_name" :"monitoring-dashboard", "scopes":["view"]}
add-resource {"resource_name" :"workflow", "scopes":["create","view","edit","change_state"]}
add-resource {"resource_name" :"report", "scopes":["create","view","export"]}
add-resource {"resource_name" :"user", "scopes":["view","edit"]}
add-resource {"resource_name" :"case-audit", "scopes":["view","comment","rebutt"]}
add-resource {"resource_name" :"onboarding-screen", "scopes":["view","edit"]}
add-resource {"resource_name" :"auditor-dashboard", "scopes":["view","edit"]}
####################################
#
## Add permissions
#
####################################
# This section lists down permissions applicable to each resource
##
# "case" resource permissions
add-permission {"perm_name":"perm_case_common","resource":"case","scopes":["edit","deactivate","comment"],"policies":["policy_msa_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_attachments","resource":"case","scopes":["add_attach","delete_attach"],"policies":["policy_manager_role","policy_supervisor_role"],"policy_strategy":"unanimous"}
add-permission {"perm_name":"perm_case_assign","resource":"case","scopes":["assign"],"policies":["policy_manager_role","policy_supervisor_role","policy_auditor_supervisor_role","policy_auditor_agent_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_add_tag_flag","resource":"case","scopes":["add_tag","set_flag"],"policies":["policy_msa_role","policy_auditors_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_view","resource":"case","scopes":["add_tag","set_flag"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_audit","resource":"case","scopes":["audit"],"policies":["policy_auditors_role"],"policy_strategy":"affirmative"}
# "job" resource permissions
add-permission {"perm_name":"perm_job_view","resource":"job","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_job_edit","resource":"job","scopes":["edit"],"policies":["policy_msa_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_job_add_tag","resource":"job","scopes":["add_tag"],"policies":["policy_msa_role","policy_auditors_role"],"policy_strategy":"affirmative"}
# "monitoring-dashboard" resource permissions
add-permission {"perm_name":"perm_monitoring_dashboard_view","resource":"monitoring-dashboard","scopes":["view"],"policies":["policy_non_auditors_role"],"policy_strategy":"affirmative"}
# "workflow" resource permissions
add-permission {"perm_name":"perm_workflow_view","resource":"workflow","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_workflow_change_state","resource":"workflow","scopes":["change_state"],"policies":["policy_msa_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_workflow_edit","resource":"workflow","scopes":["edit"],"policies":["policy_manager_role","policy_support_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_workflow_create","resource":"workflow","scopes":["create"],"policies":["policy_manager_role"],"policy_strategy":"affirmative"}
# "report" permissions
add-permission {"perm_name":"perm_report_view","resource":"report","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_report_export","resource":"report","scopes":["create","export"],"policies":["policy_msa_role","policy_auditors_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_report_edit","resource":"report","scopes":["edit"],"policies":["policy_support_role"],"policy_strategy":"affirmative"}
# "user" permissions
add-permission {"perm_name":"perm_user_export","resource":"user","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_user_edit","resource":"user","scopes":["edit"],"policies":["policy_manager_role","policy_admin_role","policy_support_role"],"policy_strategy":"affirmative"}
# "case-audit" permissions
add-permission {"perm_name":"perm_case_audit_view","resource":"case-audit","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_audit_comment","resource":"case-audit","scopes":["comment"],"policies":["policy_msa_role","policy_auditors_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_audit_rebutt","resource":"case-audit","scopes":["rebutt"],"policies":["policy_msa_role"],"policy_strategy":"affirmative"}
# "onboarding-screen" permissions
add-permission {"perm_name":"perm_case_ob_screen_view","resource":"onboarding-screen","scopes":["view"],"policies":["policy_any_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_case_ob_screen_audit_edit","resource":"onboarding-screen","scopes":["edit"],"policies":["policy_msa_role"],"policy_strategy":"affirmative"}
# "auditor-dashboard" permissions
add-permission {"perm_name":"perm_audit_dashboard_view","resource":"auditor-dashboard","scopes":["view"],"policies":["policy_auditors_role","policy_support_role"],"policy_strategy":"affirmative"}
add-permission {"perm_name":"perm_audit_dashboard_edit","resource":"auditor-dashboard","scopes":["edit"],"policies":["policy_auditors_role"],"policy_strategy":"affirmative"}



