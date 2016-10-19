FROM j7beck/x1-haproxy
MAINTAINER joe <joe@mail.x1>

ADD stomp-test.toml /etc/confd/conf.d/stomp-test.toml
ADD haproxy.tmpl /etc/confd/templates/haproxy.tmpl

ENV SERVICE_NAME stomp-test
