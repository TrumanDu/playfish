playfish是一个采用java技术，综合应用多个开源java组件实现的网页抓取工具，通过XML配置文件实现高度可定制性与可扩展性的网页抓取工具

应用开源jar包包括httpclient(内容读取),dom4j（配置文件解析）,jericho（html解析），已经在war包的lib下。

这个项目目前还很不成熟，但是功能基本都完成了。要求使用者熟悉XML，熟悉正则表达式。目前通过这个工具可以抓取各类论坛，贴吧，以及各类CMS系统。像Discuz!,phpbb,还有javaeye的论坛跟博客的文章，通过本工具都可以轻松抓取。抓取定义完全采用XML，适合Java开发人员使用。

本人并非高手，欢迎广大群众提出各类意见与建议。

使用方法，
1.下载右边的.war包导入到eclipse中，
2.使用WebContent/sql下的wcc.sql文件建立一个范例数据库，
3.修改src包下wcc.core的dbConfig.txt，将用户名与密码设置成你自己的mysql用户名密码。
4.然后运行SystemCore,运行时候会在控制台，无参数会执行默认的example.xml的配置文件，带参数时候名称为配置文件名。

系统自带了3个例子，分别为baidu.xml抓取百度知道，example.xml抓取我的javaeye的博客，bbs.xml抓取一个采用discuz论坛的内容。

具体请参考下载内的使用方法文档。其中xml配置方法文档为重点。

欢迎联系我
QQ:31697555
email:playfish@163.com
blog:playfish.javaeye.com

特别声明：本项目同时也是毕业设计，请勿用于相同用途。