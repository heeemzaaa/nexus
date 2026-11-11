# Jenkins controller.
# Each pipeline stage runs in its own ready-made image (maven, node, docker:cli),
# so the controller only needs:
#   - the docker CLI, to start those agent containers through the Podman socket
#   - the "Docker Pipeline" plugin, which provides `agent { docker { ... } }`
FROM jenkins/jenkins:lts

COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker

RUN jenkins-plugin-cli --plugins docker-workflow
