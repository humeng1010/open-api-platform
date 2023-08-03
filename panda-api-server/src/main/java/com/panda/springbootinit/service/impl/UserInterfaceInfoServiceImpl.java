package com.panda.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.springbootinit.common.ErrorCode;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.mapper.UserInterfaceInfoMapper;
import com.panda.springbootinit.model.entity.UserInterfaceInfo;
import com.panda.springbootinit.service.UserInterfaceInfoService;
import org.springframework.stereotype.Service;

/**
 * @author humeng
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-07-30 22:08:25
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {


    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean save) {
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer leftNum = userInterfaceInfo.getLeftNum();

        if (save) {
            if (userId <= 0 || interfaceInfoId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean update = this.update()
                .setSql("leftNum = leftNum - 1, totalNum = totalNum + 1")
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .gt("leftNum", 0)
                .update();
        return update;
    }
}




