variable "project_name" {
  type        = string
  description = "Name of the project, used as a prefix for resource names."
}

variable "environment" {
  type        = string
  description = "Deployment environment (e.g. local, prod)."
}

variable "repository_names" {
  type        = list(string)
  default     = ["web", "server", "nginx"]
  description = "Short names of the application images to create ECR repositories for."
}

variable "untagged_image_expiry_days" {
  type        = number
  default     = 14
  description = "Days after which untagged images are expired by the lifecycle policy."
}

variable "max_tagged_image_count" {
  type        = number
  default     = 10
  description = "Maximum number of tagged images to retain per repository."
}
