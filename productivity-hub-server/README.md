# base-project

## 介绍

简化项目开发,按功能封装模块,即拿即用,减少开发工作

## 软件架构

+ jdk 8
+ mysql 5.7
+ springboot 2.2.0.RELEASE
+ mybatis 2.2.0
+ redis 2.7.1

+ Springboot + Mybatis + Mysql + Redis
+ 全局异常
+ 发号器(雪花算法)

## 模块介绍: 

### base-common 公共包

### base-business-demo 业务包

+ `pbad/cache` : 封装SpringCache相关存储与使用
+ `pbad/customer`: 基于用户的CRUD操作
+ `pbad/generattor`: 基于雪花算法封装的获取分布式id
+ `httpclientTemplate`: 封装请求第三方的http请求模板

#### apifox 接口文档
[新增客商 - 个人项目](https://apifox.com/apidoc/shared-2189974c-13ea-4068-a3c1-de4614cb49f8)

#### 待实现

* 封装基础组件
  + 工具类
    + 同一实体不同对象的字段变化
+ 框架
  + Quartz
  + xxljob
  + 权限管理
  + 审批流
* 增加限流工具类
* 增加全局日志切面
* 增加导出工具类
