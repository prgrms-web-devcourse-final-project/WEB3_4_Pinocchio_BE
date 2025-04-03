output "ec2_public_ip" {
  description = "EC2 인스턴스의 Public IP"
  value       = aws_instance.ec2.public_ip
}

output "ec2_public_dns" {
  description = "EC2 인스턴스의 Public DNS"
  value       = aws_instance.ec2.public_dns
}

output "eip_address" {
  description = "할당된 Elastic IP 주소"
  value       = aws_eip.web_eip.public_ip
}

output "ec2_instance_id" {
  description = "생성된 EC2 인스턴스 ID"
  value       = aws_instance.ec2.id
}