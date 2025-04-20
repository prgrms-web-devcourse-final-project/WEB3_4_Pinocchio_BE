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
# RSA 키 쌍 생성 (.pem 용도)
# ----------------------------------------
resource "tls_private_key" "team_key" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

# ----------------------------------------
# AWS에 키페어 등록 (공개키만 등록)
# ----------------------------------------
resource "aws_key_pair" "team_key" {
  key_name   = "${var.prefix}-api-server-pem-key"
  public_key = tls_private_key.team_key.public_key_openssh

  tags = {
    Name         = "${var.prefix}-pem-key"
    (var.tagKey) = var.tagValue
  }
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

  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 3000
    to_port   = 3000
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 6379
    to_port   = 6379
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 27017
    to_port   = 27017
    protocol  = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port = 3306
    to_port   = 3306
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
# EC2 User Data (도커 허브 이미지 기반으로 서버 실행)
# ----------------------------------------
locals {
  env_file_content = file(var.env_file_path)
  ec2_user_data_base = <<-END_OF_FILE
#!/bin/bash

# 로그 설정
exec > /var/log/user_data.log 2>&1
set -x

# 1. Docker, Git, xfsprogs 설치
yum install docker git xfsprogs -y

# 2. Docker 서비스 설정
systemctl enable docker
systemctl start docker

# 3. 스왑 메모리 추가
dd if=/dev/zero of=/swapfile bs=128M count=32
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo "/swapfile swap swap defaults 0 0" >> /etc/fstab

# 4. EBS 마운트
mkfs -t xfs /dev/xvdf
mkdir /data
mount /dev/xvdf /data
echo "/dev/xvdf /data xfs defaults,nofail 0 2" >> /etc/fstab
mkdir -p /data/mysql /data/mongodb /data/redis

# 5. devuser 계정 생성 + Docker 권한 부여 + 환경변수 설정
useradd -m devuser
echo "devuser:devteam9" | chpasswd
echo "devuser ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
usermod -aG docker devuser
echo 'export PATH=$PATH:/usr/local/bin' >> /home/devuser/.bash_profile
chown devuser:devuser /home/devuser/.bash_profile

# 6. /data 디렉토리 소유권 변경
chown -R devuser:devuser /data

# 7. docker-compose 설치
curl -L "https://github.com/docker/compose/releases/download/v2.34.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 8. docker-compose.yml 직접 작성 및 실행 (도커 허브 이미지 사용)
sudo -u devuser -i bash <<'EOC'
mkdir -p ~/app && cd ~/app

cat <<EOF2 > docker-compose.yml

services:
    mysql:
        image: mysql:8.0
        container_name: mysql-container
        restart: always
        environment:
            MYSQL_ROOT_PASSWORD: devteam9
            MYSQL_DATABASE: pino
            MYSQL_USER: dev
            MYSQL_PASSWORD: devteam9
        ports:
            - "3306:3306"
        volumes:
            - /data/mysql:/var/lib/mysql
        networks:
            - app-network
        healthcheck:
            test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
            interval: 10s
            timeout: 5s
            retries: 5

    mongodb:
        image: mongo:6.0
        container_name: mongodb-container
        restart: always
        environment:
            MONGO_INITDB_ROOT_USERNAME: dev
            MONGO_INITDB_ROOT_PASSWORD: devteam9
            MONGO_INITDB_DATABASE: pino
        ports:
            - "27017:27017"
        volumes:
            - /data/mongodb:/data/db
        networks:
            - app-network
        healthcheck:
            test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
            interval: 10s
            timeout: 5s
            retries: 5

    redis:
        image: redis:7
        container_name: redis-container
        restart: always
        ports:
            - "6379:6379"
        command: ["redis-server", "--requirepass", "devteam9"]
        volumes:
            - /data/redis:/data
        networks:
            - app-network
        healthcheck:
            test: ["CMD", "redis-cli", "-a", "devteam9", "ping"]
            interval: 10s
            timeout: 5s
            retries: 5

    spring:
        image: jeong6/spring-app:latest
        container_name: spring-app-container
        restart: always
        ports:
            - "8080:8080"
        env_file:
            - .env
        depends_on:
            mysql:
                condition: service_healthy
            mongodb:
                condition: service_healthy
            redis:
                condition: service_healthy
        environment:
            SPRING_DATASOURCE_URL: jdbc:mysql://mysql-container:3306/pino
            SPRING_DATASOURCE_USERNAME: dev
            SPRING_DATASOURCE_PASSWORD: devteam9
            SPRING_DATA_MONGODB_URI: mongodb://dev:devteam9@mongodb-container:27017/pino?authSource=admin
            SPRING_REDIS_HOST: redis-container
            SPRING_REDIS_PORT: 6379
            SPRING_REDIS_PASSWORD: devteam9
        networks:
            - app-network
        healthcheck:
            test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
            interval: 15s
            timeout: 5s
            retries: 5

    nginx:
        image: nginx:latest
        container_name: nginx-container
        restart: always
        ports:
            - "80:80"
        volumes:
            - ./nginx/conf.d:/etc/nginx/conf.d
            - ./nginx/html:/usr/share/nginx/html
        depends_on:
            spring:
                condition: service_healthy
        networks:
            - app-network

networks:
    app-network:
        driver: bridge
EOF2

# 9. Nginx conf 파일 작성
sudo -u devuser -i bash <<'EOC_NGINX'
mkdir -p ~/app/nginx/conf.d

cat <<EOF_CONF > ~/app/nginx/conf.d/default.conf
server {
    listen 80;
    server_name pinoccheat.shop www.pinoccheat.shop;

    location / {
        proxy_pass http://spring:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF_CONF
EOC_NGINX

# 10. .env 파일 복사
mkdir -p /home/devuser/app
cat <<EOF_ENV > /home/devuser/app/.env
${replace(local.env_file_content, "$", "\\$")}
EOF_ENV
chown devuser:devuser /home/devuser/app/.env

# 11. docker 이미지 pull & 실행
docker-compose pull
docker-compose up -d
EOC

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
  key_name = aws_key_pair.team_key.key_name

  iam_instance_profile = aws_iam_instance_profile.instance_profile.name

  tags = {
    Name         = "${var.prefix}-ec2"
    (var.tagKey) = var.tagValue
  }

  metadata_options {
    http_tokens   = "required"
    http_endpoint = "enabled"
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
  domain = "vpc"

  tags = {
    Name         = "${var.prefix}-eip"
    (var.tagKey) = var.tagValue
  }

  depends_on = [aws_instance.ec2]
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

# ----------------------------------------
# S3 버킷 - 이미지 저장용
# ----------------------------------------
resource "aws_s3_bucket" "image_bucket" {
  bucket        = "${var.tagValue}-image-bucket"
  force_destroy = true

  tags = {
    Name         = "${var.tagValue}-image-bucket"
    (var.tagKey) = var.tagValue
  }
}

# ----------------------------------------
# 퍼블릭 접근 차단 해제 (정적 이미지 접근을 위함)
# ----------------------------------------
resource "aws_s3_bucket_public_access_block" "image_bucket_public_block" {
  bucket = aws_s3_bucket.image_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# ----------------------------------------
# 모든 사용자에게 이미지 읽기 허용 (s3:GetObject)
# ----------------------------------------
resource "aws_s3_bucket_policy" "image_bucket_policy" {
  bucket = aws_s3_bucket.image_bucket.id

  depends_on = [aws_s3_bucket_public_access_block.image_bucket_public_block]

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "AllowPublicRead",
        Effect    = "Allow",
        Principal = "*",
        Action    = "s3:GetObject",
        Resource  = "${aws_s3_bucket.image_bucket.arn}/*"
      }
    ]
  })
}

# ----------------------------------------
# S3 버킷 내 디렉토리 생성
# ----------------------------------------
resource "aws_s3_object" "post_image_folder_post_image" {
  bucket = aws_s3_bucket.image_bucket.id
  key    = "post-image/"
}

resource "aws_s3_object" "post_image_folder_post_profile" {
  bucket = aws_s3_bucket.image_bucket.id
  key    = "post-profile/"
}

resource "aws_s3_object" "post_image_folder_user_profile" {
  bucket = aws_s3_bucket.image_bucket.id
  key    = "user-profile/"
}

