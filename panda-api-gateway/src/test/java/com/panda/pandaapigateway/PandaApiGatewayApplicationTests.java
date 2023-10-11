package com.panda.pandaapigateway;

import com.panda.common.model.dto.userInterfaceInfo.InvokeCountRequest;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.model.entity.User;
import com.panda.pandaapigateway.service.PandaBackendClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
class PandaApiGatewayApplicationTests {

    @Resource
    private PandaBackendClient pandaBackendClient;

    @Test
    void contextLoads() {
        User panda = pandaBackendClient.getInvokeUser("panda");
        log.info("{}", panda);
        Assertions.assertNotNull(panda);

    }

    @Test
    void test2() {
        InterfaceInfo interfaceInfo = pandaBackendClient.getInterfaceInfo("http://localhost:8090/api/name/get?name=zs", "GET");
        log.info("{}", interfaceInfo);
        Assertions.assertNotNull(interfaceInfo);

    }

    @Test
    void test3() {
        InvokeCountRequest invokeCountRequest = new InvokeCountRequest();
        invokeCountRequest.setUserId(1696865098796904450L);
        invokeCountRequest.setInterfaceInfoId(7L);
        boolean b = pandaBackendClient.invokeCount(invokeCountRequest);
        log.info("{}", b);
        Assertions.assertTrue(b);

    }

}
