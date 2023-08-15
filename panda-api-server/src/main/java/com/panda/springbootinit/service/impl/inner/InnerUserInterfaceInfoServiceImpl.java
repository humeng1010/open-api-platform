package com.panda.springbootinit.service.impl.inner;

import com.panda.common.common.ErrorCode;
import com.panda.common.service.InnerUserInterfaceInfoService;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.service.UserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean update = userInterfaceInfoService.update()
                .setSql("leftNum = leftNum - 1, totalNum = totalNum + 1")
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .gt("leftNum", 0)
                .update();
        return update;
    }
}




