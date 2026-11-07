ARG NEXUS_VERSION=latest
FROM sonatype/nexus3:${NEXUS_VERSION}


USER root
RUN chown -R nexus:nexus /nexus-data
COPY --chmod=755 entrypoint.sh /usr/local/bin/nexus-entrypoint.sh
USER nexus

# 8081: Nexus UI + Maven repositories
# 8082: Docker hosted repository connector (push/pull)
# 8083: Docker group repository connector (pull: hosted + Docker Hub proxy)
EXPOSE 8081 8082 8083

# builds the secret-encryption key file from .env, then starts Nexus
ENTRYPOINT ["/usr/local/bin/nexus-entrypoint.sh"]
CMD ["/opt/sonatype/nexus/bin/nexus", "run"]

# made for my testing in my laptop, as podman ignores healthchecks in the dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=10 \
  CMD curl -fs http://localhost:8081/service/rest/v1/status || exit 1
