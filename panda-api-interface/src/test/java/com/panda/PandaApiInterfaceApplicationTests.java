package com.panda;

import com.panda.client.PandaApiClient;
import com.panda.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PandaApiInterfaceApplicationTests {

    @Test
    void contextLoads() {

    }

    @Autowired
    private PandaApiClient pandaApiClient;

    @Test
    void testClient() {
        User user = new User();
        user.setUsername("安保");
        String res = pandaApiClient.getNameByPost(user);
        System.out.println("res = " + res);
    }

}
