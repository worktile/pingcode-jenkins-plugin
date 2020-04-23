FROM nginx:mainline-alpine
RUN rm /etc/nginx/conf.d/*

ADD nginx.conf /etc/nginx/conf.d/

COPY ./target/worktile-assistant.hpi  /etc/nginx/html/
