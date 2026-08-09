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
  description = "VPC the RDS instance is deployed into."
}

variable "db_subnet_group_name" {
  type        = string
  description = "Name of the DB subnet group (private subnets spanning >= 2 AZs)."
}

variable "allowed_security_group_ids" {
  type        = list(string)
  description = "Security group IDs allowed to connect to PostgreSQL on port 5432 (the EC2 instance's SG)."
}

variable "engine_version" {
  type        = string
  default     = "16"
  description = "PostgreSQL major engine version. Confirm the target RDS engine version supports `CREATE EXTENSION vector` before applying."
}

variable "instance_class" {
  type        = string
  default     = "db.t4g.micro"
  description = "RDS instance class."
}

variable "allocated_storage_gb" {
  type        = number
  default     = 20
  description = "Allocated storage in GB (gp3)."
}

variable "max_allocated_storage_gb" {
  type        = number
  default     = 100
  description = "Ceiling for RDS storage autoscaling."
}

variable "multi_az" {
  type        = bool
  default     = false
  description = "Whether to deploy a Multi-AZ standby. Off by default for the initial release to control cost."
}

variable "db_name" {
  type        = string
  default     = "sync"
  description = "Name of the initial PostgreSQL database."
}

variable "master_username" {
  type        = string
  default     = "skkil"
  description = "Master PostgreSQL username."
}

variable "master_password" {
  type        = string
  sensitive   = true
  description = "Master PostgreSQL password."
}

variable "backup_retention_days" {
  type        = number
  default     = 7
  description = "Number of days to retain automated backups."
}

variable "deletion_protection" {
  type        = bool
  default     = true
  description = "Whether to enable RDS deletion protection."
}

variable "skip_final_snapshot" {
  type        = bool
  default     = false
  description = "Whether to skip the final snapshot on destroy. Keep false in prod."
}
