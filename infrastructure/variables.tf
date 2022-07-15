variable "product" {
  default = "ccd"
}

variable "component" {
  default = "next-hearing-date-updater"
}

variable "location" {
  default = "UK South"
}

variable "env" {}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}
