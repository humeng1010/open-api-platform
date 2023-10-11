# panda胖达api接口平台

> Dubbo + nacos 版本
> 需要提前启动nacos!

项目分为5个模块分别是:

- `panda-api-common` 项目公共模块,包含了Dubbo调用的公共接口
- `panda-api-gateway` 项目网关服务,做统一的权限校验
- `panda-api-interface` 项目接口服务,为平台提供开放的接口
- `panda-api-sdk-boot-starter` 接口调用的sdk,一行代码调用接口
- `panda-api-server` 接口平台服务,项目的后端服务

项目的运行前提条件(需要提前启动的服务):

- MySQL 8.x
- Redis
- nacos

项目整合nacos+dubbo踩坑总结:

1. 开发环境nacos使用docker部署的,需要同时开放8848,9848,9849端口,命令如下:
   > docker run --name mynacos -d -p 8848:8848 -p 9848:9848 -p 9849:9849 --privileged=true --restart=always -e
   MODE=standalone -e PREFER_HOST_MODE=hostname nacos/nacos-server:2.1.1
2. 远程调用接口必须在同一个包下,推荐单独抽出来作为一个模块
3. 建议分步骤整合,先整合nacos,把provider和consumer都能成功注册到nacos中再整合dubbo
4. 整合dubbo可以根据官网的案例整合,先保证能调用成功
5. 注意版本的问题
6. 遇到问题多百度
 