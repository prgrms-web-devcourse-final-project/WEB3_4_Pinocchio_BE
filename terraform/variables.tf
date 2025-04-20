variable "prefix" {
  description = "Prefix for all resources"
  default     = "Team09"
}

variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "tagKey" {
  description = "Essential Tag Key"
  default     = "Team"
}

variable "tagValue" {
  description = "Essential Tag Value"
  default     = "devcos4-team09"
}

variable "env_file_path" {
  default = "./.env"
}