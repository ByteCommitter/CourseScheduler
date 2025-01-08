# See all running containers
docker ps

# Stop all running containers
docker stop $(docker ps -a -q)

# Remove all stopped containers
docker stop $(docker ps -a -q)

# Now run your container again
docker run -d -p 8080:8080 gurumurthyv/coursescheduler:latest

# Verify it's running
docker ps

# Check logs if needed
docker logs $(docker ps -q)
