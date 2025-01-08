#!/bin/bash

# Run these commands on your EC2 instance after connecting
# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Install Docker
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user

# Create app directory
mkdir -p ~/timetabling/target
cd ~/timetabling

# Download files from S3
aws s3 cp s3://timetabling-app-files/timetabling/Dockerfile .
aws s3 cp s3://timetabling-app-files/timetabling/target/quarkus-app target/quarkus-app --recursive

# Build and run Docker container
sudo docker build -t timetabling .
sudo docker run -d -p 8080:8080 timetabling
