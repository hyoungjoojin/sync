variable "root_domain" {
  type        = string
  description = "Root domain hosted in Route 53 (e.g. skkil.org). Must already have a hosted zone — either registered directly through Route 53 (auto-created) or delegated from an external registrar."
}

variable "record_name" {
  type        = string
  description = "Fully qualified subdomain to point at the app instance (e.g. sync.skkil.org)."
}

variable "target_ip" {
  type        = string
  description = "IP address (the EC2 Elastic IP) the A record resolves to."
}
