FROM java:8

RUN mkdir -p /opt/mytunesrss
COPY target/mytunesrss-*-bin/mytunesrss-* /opt/mytunesrss/
COPY docker-settings.xml /root/.MyTunesRSS-6/settings.xml

WORKDIR /opt/mytunesrss
CMD ["./startup.sh"]

EXPOSE 8080 9090
