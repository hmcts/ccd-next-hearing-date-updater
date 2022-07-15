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

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}
