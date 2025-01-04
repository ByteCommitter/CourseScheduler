#!/bin/bash

# Create deployment directory
DEPLOY_DIR="ec2-deploy"
mkdir -p $DEPLOY_DIR

# Copy only the required files
cp -r target/quarkus-app $DEPLOY_DIR/

# Create startup script
cat > $DEPLOY_DIR/start.sh << 'EOF'
#!/bin/bash
java -jar quarkus-app/quarkus-run.jar \
  -Dquarkus.http.port=8080 \
  -Dquarkus.http.host=0.0.0.0
EOF

chmod +x $DEPLOY_DIR/start.sh

echo "Created deployment package in $DEPLOY_DIR"
