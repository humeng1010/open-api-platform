package com.panda.common.service;

import com.panda.common.model.entity.InterfaceInfo;


public interface InnerInterfaceInfoService {

    /**
     * 从数据库中获取接口信息是否存在
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return 接口信息
     */
    InterfaceInfo getInterfaceInfo(String path, String method);


}
