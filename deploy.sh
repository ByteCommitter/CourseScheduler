#!/bin/bash

# Configuration
ECR_REPO="your-account.dkr.ecr.your-region.amazonaws.com"
REPO_NAME="course-scheduler"
EC2_HOST="ec2-user@your-ec2-public-ip"
PEM_FILE="path/to/your-key.pem"

# Build and push image
docker build -t $REPO_NAME .
docker tag $REPO_NAME:latest $ECR_REPO/$REPO_NAME:latest
aws ecr get-login-password --region your-region | docker login --username AWS --password-stdin $ECR_REPO
docker push $ECR_REPO/$REPO_NAME:latest

# Deploy to EC2
ssh -i $PEM_FILE $EC2_HOST "aws ecr get-login-password --region your-region | docker login --username AWS --password-stdin $ECR_REPO && \
    docker pull $ECR_REPO/$REPO_NAME:latest && \
    docker stop $REPO_NAME || true && \
    docker rm $REPO_NAME || true && \
    docker run -d --name $REPO_NAME -p 8080:8080 $ECR_REPO/$REPO_NAME:latest"
