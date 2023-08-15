package com.panda.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.common.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.panda.common.model.entity.InterfaceInfo;


/**
 * @author humeng
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2023-07-23 14:37:54
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

}
