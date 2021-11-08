#
# Builds a container image containing an nginx server configured to serve the files
# provided in /statics.  These files will be processed for environment variable
# substitutions during container startup.  For example, the string `${AUTH_URL}`
# appearing in any of the files in statics will be replaced by the value of the
# environment variable `AUTH_URL` at container runtime.
#
# See also the ./src/environments/environment.prod.docker.ts and ./example.deployment.yaml files.
#
# To build and push this Dockerfile to your container registry, run the following commands in the
# angular workspace root directory (i.e., the directory containing angular.json):
#
#     npm run build:employee-app:docker
#     docker build -f ./projects/employee-app/Dockerfile -t your-container-registry.example.com/employee-app .
#     docker push your-container-registry.example.com/employee-app
#

FROM repo.backbase.com/backbase-docker-releases/web-base:0.0.7

COPY ./dist/employee-app /statics
