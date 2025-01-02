#!/bin/bash

# Create deployment directory
DEPLOY_DIR="ec2-deploy"
mkdir -p $DEPLOY_DIR

# Copy required files
cp run-school-timetabling.sh $DEPLOY_DIR/
cp -r use-cases/school-timetabling/target/quarkus-app $DEPLOY_DIR/

# Make scripts executable
chmod +x $DEPLOY_DIR/run-school-timetabling.sh

# Create a startup script
cat > $DEPLOY_DIR/start-application.sh << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java -jar quarkus-app/quarkus-run.jar \
  -Dquarkus.http.port=8080 \
  -Dquarkus.http.host=0.0.0.0
EOF

chmod +x $DEPLOY_DIR/start-application.sh

echo "Deployment package created in $DEPLOY_DIR"
echo "Transfer this directory to your EC2 instance"
