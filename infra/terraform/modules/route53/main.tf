# The hosted zone is not created here: registering a domain through Route 53
# (as opposed to an external registrar) auto-creates its hosted zone and
# wires the registrar's nameservers to it already. Creating a second
# `aws_route53_zone` with the same name here would just produce an orphaned,
# undelegated zone.
data "aws_route53_zone" "root" {
  name = var.root_domain
}

resource "aws_route53_record" "app" {
  zone_id = data.aws_route53_zone.root.zone_id
  name    = var.record_name
  type    = "A"
  ttl     = 300
  records = [var.target_ip]
}
