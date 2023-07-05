FROM alpine:3.17
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache \
       subversion \
       apache2 \
       apache2-webdav \
       apache2-utils \
       mod_dav_svn \
       openjdk8-jre \
       ttf-dejavu \
       fontconfig \
       tzdata \
       tini \
    && fc-cache -f -v \
    && ln -sf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && rm -rf /var/cache/apk/* /tmp/*
COPY target/svnWebUI-*.jar /home/svnWebUI.jar
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN ["chmod", "+x", "/usr/local/bin/entrypoint.sh"]
VOLUME ["/home/svnWebUI"]
ENTRYPOINT ["tini", "entrypoint.sh"]