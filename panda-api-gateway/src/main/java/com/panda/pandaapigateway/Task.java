package com.panda.pandaapigateway;

import com.panda.NacosService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Task implements CommandLineRunner {
    @DubboReference
    private NacosService nacosService;

    @Override
    public void run(String... args) throws Exception {
        System.out.print("Receive result ======> " + nacosService.sayHello("dubbo"));

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println(nacosService.sayHello(new Date() + nacosService.sayHello("dubbo")));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
