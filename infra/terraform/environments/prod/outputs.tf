output "app_public_ip" {
  value       = module.ec2.public_ip
  description = "Elastic IP of the EC2 instance. Terraform no longer manages any DNS, so app_domain must be pointed here by hand wherever the zone is hosted."
}

output "ecr_repository_urls" {
  value       = module.ecr.repository_urls
  description = "Push built images here; referenced by scripts/cd/prod-deploy.sh."
}

output "rds_endpoint" {
  value       = module.rds.endpoint
  description = "RDS PostgreSQL endpoint hostname."
}

output "ec2_instance_id" {
  value       = module.ec2.instance_id
  description = "Used as the target for `aws ssm send-command`."
}
