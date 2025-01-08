#!/bin/bash

# S3 bucket name - replace with your desired name
BUCKET_NAME="timetabling-app-files"
APP_DIR="timetabling"

# Create S3 bucket
aws s3api create-bucket \
    --bucket ${BUCKET_NAME} \
    --region eu-north-1 \
    --create-bucket-configuration LocationConstraint=eu-north-1

# Upload application files to S3
aws s3 cp Dockerfile s3://${BUCKET_NAME}/${APP_DIR}/
aws s3 cp ../use-cases/school-timetabling/target/quarkus-app s3://${BUCKET_NAME}/${APP_DIR}/target/quarkus-app --recursive

# Create IAM role for EC2 to access S3
aws iam create-role \
    --role-name EC2S3AccessRole \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {
                "Service": "ec2.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }]
    }'

# Attach S3 access policy to the role
aws iam attach-role-policy \
    --role-name EC2S3AccessRole \
    --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess

# Create instance profile and add role to it
aws iam create-instance-profile --instance-profile-name EC2S3Profile
aws iam add-role-to-instance-profile \
    --instance-profile-name EC2S3Profile \
    --role-name EC2S3AccessRole
