package com.panda.common.service;

public interface InnerUserInterfaceInfoService {


    /**
     * 调用接口统计
     *
     * @param interfaceInfoId 接口id
     * @param userId          用户id
     * @return 是否调用成功
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
