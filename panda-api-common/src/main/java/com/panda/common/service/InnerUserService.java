package com.panda.common.service;


import com.panda.common.model.entity.User;

/**
 * 用户服务
 */
public interface InnerUserService {

    /**
     * 数据库中查询是否已经分配给用户密钥
     *
     * @param accessKey ak
     * @param secretKey sk
     * @return 用户信息
     */
    User getInvokeUser(String accessKey, String secretKey);

}
