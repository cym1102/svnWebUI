# svnWebUI


#### 介绍
Subversion的web管理界面, 搭建svn服务器的神器.

#### 功能说明

svnWebUI是一款图形化管理Subversion的配置得工具, 虽说现在已进入git的时代, 但svn依然有不少使用场景, 比如公司内的文档管理与共享, svn的概念比git的少很多, 非常适合非程序员使用.

但众所周知svn的Linux服务端软件即Subversion的用户和权限配置全部依靠手写配置文件完成, 非常繁琐且不便, 已有的几款图像界面软件已经非常古老, 安装麻烦而且依赖环境非常古老, 比如csvn还使用python2作为运行环境.

Windows上倒是有不错的svn服务端软件即VisualSVN, 但一来Windows服务器少之又少, 第二VisualSVN没有web界面, 每次配置需要开启远程桌面, 安全性不高.

经历几次失败的图形界面配置后, 萌生了写一个现代svn服务端管理软件, 让svn的服务端管理有gitea的轻松体验的想法.


#### 技术说明

本项目是基于springBoot的web系统, 数据库使用sqlite, 因此服务器上不需要安装任何数据库

项目启动时会释放一个.sqlite.db到系统用户文件夹中, 注意进行备份

使用本软件前请先安装Subversion


```
演示地址: http://svn.nginxwebui.cn:6060
用户名: admin
密码: admin
```


#### 安装说明
以Ubuntu操作系统为例,

1.安装java运行环境和Subversion

Ubuntu:

```
apt update
apt install openjdk-11-jdk
apt install subversion
```

Centos:

```
yum install java-11-openjdk
yum install subversion
```

Windows:

```
下载JDK安装包 https://www.oracle.com/java/technologies/downloads/
下载VisualSVN https://www.visualsvn.com/server/download
配置JAVA运行环境 
JAVA_HOME : JDK安装目录
Path : JDK安装目录\bin
重启电脑
```


2.下载最新版发行包jar

```
Linux: wget -O /home/svnWebUI/svnWebUI.jar http://file.nginxwebui.cn/svnWebUI-1.1.0.jar

Windows: 直接使用浏览器下载 http://file.nginxwebui.cn/svnWebUI-1.1.0.jar
```

有新版本只需要修改路径中的版本即可

3.启动程序

```
Linux: nohup java -jar -Xmx64m /home/svnWebUI/svnWebUI.jar --server.port=6060 --project.home=/home/svnWebUI/ > /dev/null &

Windows: java -jar -Xmx64m D:/home/svnWebUI/svnWebUI.jar --server.port=6060 --project.home=D:/home/svnWebUI/
```

参数说明(都是非必填)

-Xmx64m 最大分配内存数

--server.port 占用端口, 默认以6060端口启动

--project.home 项目配置文件目录，存放数据库文件，证书文件，日志等, 默认为/home/nginxWebUI/

注意命令最后加一个&号, 表示项目后台运行

#### docker安装说明

本项目制作了docker镜像, 支持 x86_64/arm64/arm v7 平台，同时包含Subversion和svnWebUI在内, 一体化管理与运行Subversion. 

1.安装docker容器环境

Ubuntu:

```
apt install docker.io
```

Centos:

```
yum install docker
```

2.拉取镜像: 

```
docker pull cym1102/svnwebui:latest
```

3.启动容器: 

```
docker run -itd -v /home/svnWebUI:/home/svnWebUI -e BOOT_OPTIONS="--server.port=6060" --privileged=true -p 6060:6060 -p 3690:3690 cym1102/svnwebui:latest
```

注意: 

1. 需要映射6060端口与3690端口, 6060为web网页端口, 3690为svn默认端口. 

2. 容器需要映射路径/home/svnWebUI:/home/svnWebUI, 此路径下存放项目所有数据文件, 包括数据库, 配置文件, 日志等, 升级镜像时, 此目录可保证项目数据不丢失. 请注意备份.

3. -e BOOT_OPTIONS 参数可填充java启动参数, 可以靠此项参数修改端口号

--server.port 占用端口, 不填默认以6060端口启动

4. 日志默认存放在/home/svnWebUI/log/svnWebUI.log


#### 编译说明

使用maven编译打包

```
mvn clean package
```

使用docker构建镜像

```
docker build -t svnwebui:latest .
```

#### 添加开机启动


1. 编辑service配置

```
vim /etc/systemd/system/svnwebui.service
```

```
[Unit]
Description=SvnWebUI
After=syslog.target
After=network.target
 
[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/home/svnWebUI
ExecStart=/usr/bin/java -jar /home/svnWebUI/svnWebUI.jar
Restart=always
 
[Install]
WantedBy=multi-user.target
```

之后执行

```
systemctl daemon-reload
systemctl enable svnwebui.service
systemctl start svnwebui.service
```

#### 使用说明

打开 http://ip:6060 进入主页

![输入图片说明](http://www.nginxwebui.cn/img/svn/注册用户.png "login.jpg")

首次打开页面, 需要注册管理员账户

![输入图片说明](http://www.nginxwebui.cn/img/svn/登录界面.png "login.jpg")

注册完毕后, 进入登录页面进行登录

![输入图片说明](http://www.nginxwebui.cn/img/svn/服务管理.png "admin.jpg")

服务管理, 可在这个页面查看Subversion服务的开启情况, 并进行停止和重启.

![输入图片说明](http://www.nginxwebui.cn/img/svn/仓库管理.png "admin.jpg")

仓库管理, 可添加仓库及修改仓库, 添加仓库后即可获得仓库的svn地址, 十分方便

![输入图片说明](http://www.nginxwebui.cn/img/svn/用户授权.png "admin.jpg")

选择对应的用户对仓库进行授权

![输入图片说明](http://www.nginxwebui.cn/img/svn/小组授权.png "admin.jpg")

选择对应的小组对仓库进行授权

![输入图片说明](http://www.nginxwebui.cn/img/svn/用户管理.png "admin.jpg")

用户管理, 可添加和编辑用户

![输入图片说明](http://www.nginxwebui.cn/img/svn/分组管理.png "admin.jpg")

小组管理, 可添加和编辑小组

#### 找回密码

如果忘记了登录密码，可按如下教程找回密码

1. 安装sqlite3命令（Docker镜像已经安装好了）

```
apt install sqlite3
```

2. 读取sqlite.db文件

```
sqlite3 /home/svnWebUI/sqlite.db
```

3. 查找user表

```
select * from user;
```

4. 退出sqlite3

```
.quit
```
