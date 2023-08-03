package com.panda.springbootinit.rpc;

import com.panda.NacosService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class NacosServiceImpl implements NacosService {
    @Override
    public void sayHello() {
        System.out.println("hello nacos & dubbo");
    }
}
