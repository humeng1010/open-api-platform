package com.panda.springbootinit.rpc;

import com.panda.NacosService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class NacosServiceImpl implements NacosService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}
