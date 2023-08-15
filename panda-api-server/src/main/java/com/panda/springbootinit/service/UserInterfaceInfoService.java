package com.panda.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.common.model.entity.UserInterfaceInfo;

/**
 * @author humeng
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
 * @createDate 2023-07-30 22:08:25
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean save);

}
