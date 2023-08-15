package com.panda.springbootinit.service.impl.inner;

import com.panda.common.common.ErrorCode;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.service.InnerInterfaceInfoService;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        if (StringUtils.isAnyBlank(path, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.query().eq("url", path).eq("method", method).one();
    }
}




