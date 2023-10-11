# panda胖达api接口平台

> openfegin+eureka版本
>
> 启动前不需要额外启动注册中心

项目分为5个模块分别是:

- `panda-api-common` 项目公共模块,包含了Dubbo调用的公共接口
- `panda-api-gateway` 项目网关服务,做统一的权限校验
- `panda-api-interface` 项目接口服务,为平台提供开放的接口
- `panda-api-sdk-boot-starter` 接口调用的sdk,一行代码调用接口
- `panda-api-server` 接口平台服务,项目的后端服务
- `panda-api-eureka` eureka注册中心

项目的运行前提条件(需要提前启动的服务):

- MySQL 8.x
- Redis

dubbo+nacos改为openfegin+eureka实现远程调用：

- 首先移除掉dubbo相关的内容：
    - 删除依赖
    - 移除相关注解
- 删除掉nacos相关的内容：
    - 删除依赖
    - 删除配置
- 添加eureka注册中心的server端
- 把server后端项目和gateway项目添加到注册中心中(引入client客户端)
- 删除server中的dubbo接口的实现类，并且转移到对应的controller中，方便openfegin进行http远程调用
- 在gateway中引入openfegin的依赖，定义一个远程调用的客户端编写需要远程调用的方法
- 在对应的位置进行调用

> 目前存在的问题：由于gateway的全局过滤器使用的是异步编程的方法，但是openfegin实现远程调用的是同步方法，编辑器首先会提示：
>
> Possibly blocking call in non-blocking context could lead to thread starvation 
>
> 在非阻塞上下文中可能阻塞调用可能会导致线程不足
>
> ![image-20231011164817562](https://cdn.jsdelivr.net/gh/humeng1010/cloud-images/blog-images/202310111648471.png)
>
> 并且在openfegin进行远程调用的时候出现如下报错：
>
> ![image-20231011164916335](https://cdn.jsdelivr.net/gh/humeng1010/cloud-images/blog-images/202310111649513.png)
>
> 原因大概是：gateway网关Feign调用微服务异常，spring boot 2.7.0 WebFlux必须使用异步调用，同步会报错
>
> https://blog.csdn.net/qq_21480329/article/details/125126024
> 解决办法：使用线程池来包装Feign调用可以解决报错问题
>
> 因为线程池会将Feign的调用从Reactor的非阻塞线程切换到线程池的线程中执行，从而避免了Reactor中的非阻塞要求。这样，您可以在线程池中执行Feign调用，而不会触发Reactor的"可能的阻塞调用"警告。
>
> 在Reactive编程中，异步和非阻塞是关键概念。Reactor使用的是事件循环线程，而且它会监控阻塞调用。如果您在Reactor线程中执行可能导致阻塞的操作（如Feign同步调用），Reactor会发出警告，因为这可能导致线程饥饿和性能问题。
>
> 通过将Feign调用放入线程池中，您将其从Reactor线程中隔离开，这就是为什么不再触发"可能的阻塞调用"警告。但是，请注意，这种方法也有一些**潜在的问题**：
>
> 1. 线程切换开销：将Feign调用放入线程池可能会引入线程切换的开销，这在高负载的情况下可能会影响性能。
> 2. 长时间阻塞：如果Feign调用由于某种原因长时间阻塞，线程池中的线程可能会被耗尽，从而影响整体应用程序的可用性。
> 3. 异常处理：在线程池中执行的Feign调用引发的异常可能不会传播到Reactor的异常处理链中，因此需要小心处理异常情况。
>
> 如果您选择使用线程池，确保根据应用程序的性能和可用性需求进行适当的线程池配置，并考虑异常处理策略。另外，您还可以尝试使用Spring WebClient等Reactive编程工具，以避免直接使用Feign的同步调用。这有助于更好地符合Reactive编程的模型。