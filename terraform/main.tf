# ----------------------------------------
# Terraform 설정
# ----------------------------------------
terraform {
  # AWS 라이브러리 불러옴
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

# ----------------------------------------
# AWS Provider 설정
# ----------------------------------------
provider "aws" {
  region = var.region
}

# ----------------------------------------
# VPC 설정
# ----------------------------------------
resource "aws_vpc" "vpc" {
  cidr_block = "10.0.0.0/16"

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name         = "${var.prefix}-api-server"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 퍼블릭 서브넷 1 (AZ-a)
# ----------------------------------------
resource "aws_subnet" "subnet_1" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = true

  tags = {
    Name         = "${var.prefix}-subnet-1"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 퍼블릭 서브넷 2 (AZ-b)
# ----------------------------------------
resource "aws_subnet" "subnet_2" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${var.region}b"
  map_public_ip_on_launch = true

  tags = {
    Name         = "${var.prefix}-subnet-2"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 퍼블릭 서브넷 3 (AZ-c)
# ----------------------------------------
resource "aws_subnet" "subnet_3" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "${var.region}c"
  map_public_ip_on_launch = true

  tags = {
    Name         = "${var.prefix}-subnet-3"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 인터넷 게이트웨이 설정 (외부 인터넷 접근용)
# ----------------------------------------
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id

  tags = {
    Name         = "${var.prefix}-igw"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 라우팅 테이블 생성 (인터넷 접근 허용)
# ----------------------------------------
resource "aws_route_table" "rt" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name         = "${var.prefix}-rt"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 서브넷 - 라우팅 테이블 연결
# ----------------------------------------
resource "aws_route_table_association" "association_1" {
  subnet_id      = aws_subnet.subnet_1.id
  route_table_id = aws_route_table.rt.id
}

resource "aws_route_table_association" "association_2" {
  subnet_id      = aws_subnet.subnet_2.id
  route_table_id = aws_route_table.rt.id
}

resource "aws_route_table_association" "association_3" {
  subnet_id      = aws_subnet.subnet_3.id
  route_table_id = aws_route_table.rt.id
}

# ----------------------------------------
# 보안 그룹 (Security Group) 설정: SSH, HTTP, HTTPS만 허용
# ----------------------------------------
resource "aws_security_group" "sg" {
  name = "${var.prefix}-sg"

  # 인바운드 규칙 (들어오는 트래픽)
  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # 아웃바운드 규칙 (나가는 트래픽)
  egress {
    from_port = 0
    to_port   = 0
    protocol  = "all"
    cidr_blocks = ["0.0.0.0/0"]
  }

  vpc_id = aws_vpc.vpc.id

  tags = {
    Name         = "${var.prefix}-sg"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# IAM 역할 생성 (EC2용)
# ----------------------------------------
resource "aws_iam_role" "ec2_role" {
  name = "${var.prefix}-ec2-role"

  # 이 역할에 대한 신뢰 정책 설정. EC2 서비스가 이 역할을 가정할 수 있도록 설정
  assume_role_policy = <<EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "",
        "Action": "sts:AssumeRole",
        "Principal": {
            "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow"
      }
    ]
  }
  EOF
}

# ----------------------------------------
# EC2 역할에 S3 전체 접근 권한 부여
# ----------------------------------------
resource "aws_iam_role_policy_attachment" "s3_full_access" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

# ----------------------------------------
# EC2 역할에 SSM 접근 권한 부여 (Session Manager 등 사용 가능)
# ----------------------------------------
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM"
}

# ----------------------------------------
# 인스턴스 프로파일 생성 (IAM 역할을 EC2에 연결하기 위한 래퍼)
# ----------------------------------------
resource "aws_iam_instance_profile" "instance_profile" {
  name = "${var.prefix}-instance-profile"
  role = aws_iam_role.ec2_role.name
}

# ----------------------------------------
# EC2 User Data (인스턴스 부팅 시 실행될 스크립트)
# 1. Docker, Git 세팅
# 2. Swap 메모리 4GB 추가
# 3. EBS 18GB 마운트
# 4. docker-compose 세팅
# 5. Git Repository에서 docker-compose.yml 복사
# 6. docker-compose 실행
# ----------------------------------------
locals {
  ec2_user_data_base = <<-END_OF_FILE
#!/bin/bash

# 1. Docker, Git 설치
  yum install docker -y
  systemctl enable docker
  systemctl start docker

  yum install git -y

# 2. 스왑 메모리 추가
  dd if=/dev/zero of=/swapfile bs=128M count=32
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile swap swap defaults 0 0" >> /etc/fstab

# 3. EBS 마운트
  yum install -y xfsprogs
  mkfs -t xfs /dev/xvdf
  mkdir /data
  mount /dev/xvdf /data
  echo "/dev/xvdf /data xfs defaults,nofail 0 2" >> /etc/fstab
  mkdir -p /data/mysql /data/mongodb /data/redis

# 4. docker-compose 설치
  curl -L "https://github.com/docker/compose/releases/download/v2.34.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  chmod +x /usr/local/bin/docker-compose

# 5. Git Repository에서 docker-compose.yml 복사
  git clone https://github.com/prgrms-web-devcourse-final-project/WEB3_4_Pinocchio_BE.git /app
  cd /app

# 6. docker-compose 실행
  docker-compose up -d

END_OF_FILE
}

# ----------------------------------------
# 최신 Amazon Linux 2 AMI 가져오기
# ----------------------------------------
data "aws_ami" "latest_amazon_linux" {
  most_recent = true
  owners = ["amazon"]

  filter {
    name = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }

  filter {
    name = "virtualization-type"
    values = ["hvm"]
  }
}

# ----------------------------------------
# EC2 인스턴스 생성
# ----------------------------------------
resource "aws_instance" "ec2" {
  ami                         = data.aws_ami.latest_amazon_linux.id
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.subnet_2.id
  vpc_security_group_ids = [aws_security_group.sg.id]
  associate_public_ip_address = true

  iam_instance_profile = aws_iam_instance_profile.instance_profile.name

  tags = {
    Name         = "${var.prefix}-ec2"
    (var.tagKey) = var.tagValue
  }

  # root 볼륨 크기 12GB 설정
  root_block_device {
    volume_type = "gp3"
    volume_size = 12
  }

  user_data = <<-EOF
${local.ec2_user_data_base}
EOF
}

# ----------------------------------------
# Elastic IP 생성
# ----------------------------------------
resource "aws_eip" "web_eip" {
  vpc = true

  tags = {
    Name         = "${var.prefix}-eip"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# Elastic IP와 EC2 인스턴스 연결
# ----------------------------------------
resource "aws_eip_association" "web_eip_assoc" {
  instance_id   = aws_instance.ec2.id
  allocation_id = aws_eip.web_eip.id
}

# ----------------------------------------
# 추가 EBS 볼륨 생성
# ----------------------------------------
resource "aws_ebs_volume" "extra_storage" {
  availability_zone = aws_instance.ec2.availability_zone
  size              = 18
  type              = "gp3"

  tags = {
    Name         = "${var.prefix}-extra-storage"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# EC2에 EBS 볼륨 연결
# ----------------------------------------
resource "aws_volume_attachment" "ebs_attach" {
  device_name = "/dev/xvdf"
  volume_id   = aws_ebs_volume.extra_storage.id
  instance_id = aws_instance.ec2.id
}
