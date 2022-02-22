FROM centos:centos7
ENV LANG=zh_CN.UTF-8 \
    TZ=Asia/Shanghai \
    PS1="\u@\h:\w \$ "
RUN yum -y install \
       subversion \
       httpd \
       mod_dav_svn \
       java-1.8.0-openjdk \
    && yum clean all
COPY target/svnWebUI-*.jar /home/svnWebUI.jar
COPY svn.conf /etc/httpd/conf.d/svn.conf
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT /usr/local/bin/entrypoint.sh