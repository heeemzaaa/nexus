# Jenkins controller.
# Each pipeline stage runs in its own ready-made image (maven, node, docker:cli),
# so the controller only needs:
#   - the docker CLI, to start those agent containers through the Podman socket
#   - the plugins in plugins.txt (Docker Pipeline, Configuration as Code, ...)
# Its whole configuration comes from casc/jenkins.yaml (Configuration as Code).
FROM jenkins/jenkins:lts

COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
