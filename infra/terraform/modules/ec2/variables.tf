variable "project_name" {
  type        = string
  description = "Name of the project, used as a prefix for resource names."
}

variable "environment" {
  type        = string
  description = "Deployment environment (e.g. local, prod)."
}

variable "vpc_id" {
  type        = string
  description = "VPC the instance is deployed into."
}

variable "subnet_id" {
  type        = string
  description = "Public subnet the instance is deployed into."
}

variable "instance_type" {
  type        = string
  default     = "t3.small"
  description = "EC2 instance type. Hosts the web, server and nginx containers together."
}

variable "root_volume_size_gb" {
  type        = number
  default     = 30
  description = "Root EBS volume size in GB (gp3)."
}


variable "media_bucket_arn" {
  type        = string
  description = "ARN of the S3 media bucket the instance role is granted read/write access to."
}

variable "secret_arns" {
  type        = list(string)
  description = "ARNs of the Secrets Manager secrets the instance role may read."
}

variable "ecr_repository_arns" {
  type        = list(string)
  description = "ARNs of the ECR repositories the instance role may pull images from."
}

variable "kms_key_arn" {
  type        = string
  description = "ARN of the KMS key used to encrypt the secrets, granted decrypt access."
}
