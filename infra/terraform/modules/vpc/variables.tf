variable "project_name" {
  type        = string
  description = "Name of the project, used as a prefix for resource names."
}

variable "environment" {
  type        = string
  description = "Deployment environment (e.g. local, prod)."
}

variable "vpc_cidr" {
  type        = string
  default     = "10.20.0.0/16"
  description = "CIDR block for the VPC."
}

variable "public_subnet_cidr" {
  type        = string
  default     = "10.20.0.0/24"
  description = "CIDR block for the public subnet that hosts the EC2 instance."
}

variable "private_subnet_cidrs" {
  type        = list(string)
  default     = ["10.20.1.0/24", "10.20.2.0/24"]
  description = "CIDR blocks for the private subnets that host RDS. Must span at least two AZs."
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones to spread the public and private subnets across. First AZ hosts the public subnet."
}
