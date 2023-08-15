package com.panda.springbootinit.service.impl.inner;

import com.panda.common.common.ErrorCode;
import com.panda.common.model.entity.User;
import com.panda.common.service.InnerUserService;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public User getInvokeUser(String accessKey, String secretKey) {
        if (StringUtils.isAnyBlank(accessKey, secretKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userService.query().eq("accessKey", accessKey)
                .eq("secretKey", secretKey).one();
    }
}
