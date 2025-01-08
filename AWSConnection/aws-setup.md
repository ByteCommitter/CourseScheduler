# EC2 Deployment Instructions

## 1. Create EC2 Instance
1. Go to AWS Console > EC2
2. Launch new instance:
   - Choose Amazon Linux 2023
   - t2.micro (free tier)
   - Create new key pair (save the .pem file)
   - Allow HTTP (80), HTTPS (443), and Custom TCP (8080) in security group

## 2. Install Docker on EC2
Connect to your EC2 instance:
```bash
chmod 400 your-key.pem
ssh -i your-key.pem ec2-user@your-ec2-public-ip

Login Details:
docker login
gurumurthyv  
Ma#vem_A8SWFA7E
```

Install Docker:
```bash
sudo yum update -y
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user
```

## 3. Deploy Container
Option 1 - Using Docker Hub:
```bash
# On your local machine
# Assuming your image name is 'coursescheduler' and tag is 'latest'
docker tag quarkus-timetabling:latest gurumurthyv/coursescheduler:latest
docker push gurumurthyv/coursescheduler:latest

# On EC2
docker pull gurumurthyv/coursescheduler:latest
docker run -d -p 8081:8081 gurumurthyv/coursescheduler:latest
```

Option 2 - Using Amazon ECR:
```bash
# Configure AWS CLI locally
aws configure

# Create ECR repository
aws ecr create-repository --repository-name coursescheduler

# Login to ECR
aws ecr get-login-password --region your-region | docker login --username AWS --password-stdin your-account.dkr.ecr.your-region.amazonaws.com

# Tag and push image
docker tag coursescheduler:latest your-account.dkr.ecr.your-region.amazonaws.com/coursescheduler:latest
docker push your-account.dkr.ecr.your-region.amazonaws.com/coursescheduler:latest

# On EC2
aws ecr get-login-password --region your-region | docker login --username AWS --password-stdin your-account.dkr.ecr.your-region.amazonaws.com
docker pull your-account.dkr.ecr.your-region.amazonaws.com/coursescheduler:latest
docker run -d -p 8080:8080 your-account.dkr.ecr.your-region.amazonaws.com/coursescheduler:latest
```

## 4. Access Application
Your application will be available at:
```
http://your-ec2-public-ip:8081
```

## Troubleshooting
If you get port binding errors, run these commands:
```bash
# Check running containers
docker ps

# Stop all containers
docker stop $(docker ps -a -q)

# Remove stopped containers
docker rm $(docker ps -a -q)

# Run container again
docker run -d -p 8081:8081 gurumurthyv/coursescheduler:latest
```

## Note
- Replace placeholders (your-key.pem, your-ec2-public-ip, etc.) with actual values
- Ensure your EC2 security group allows inbound traffic on port 8080
- Consider using HTTPS for production deployments
- If port 8081 is still occupied, you can try another port (e.g., 8082)
- You can check container logs using: docker logs <container_id>
- To see all containers (including stopped ones): docker ps -a
