package com.panda.pandaapigateway.service;

import com.panda.common.model.dto.userInterfaceInfo.InvokeCountRequest;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.model.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author humeng
 */
@FeignClient("PANDA-API-BACKEND")
public interface PandaBackendClient {
    /**
     * 从数据库中获取接口信息是否存在
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return 接口信息
     */
    @RequestMapping(value = "/api/interfaceInfo/getInterfaceInfo", method = GET)
    InterfaceInfo getInterfaceInfo(@RequestParam("path") String path, @RequestParam("method") String method);


    /**
     * 调用统计接口
     *
     * @param invokeCountRequest
     * @return
     */
    @RequestMapping(value = "/api/user_interfaceInfo/invokeCount", method = PUT)
    boolean invokeCount(@RequestBody InvokeCountRequest invokeCountRequest);

    /**
     * 数据库中查询是否已经分配给用户密钥
     *
     * @param accessKey ak
     * @return 用户信息
     */
    @RequestMapping(value = "/api/user/getInvokeUser", method = GET)
    User getInvokeUser(@RequestParam("accessKey") String accessKey);
}
