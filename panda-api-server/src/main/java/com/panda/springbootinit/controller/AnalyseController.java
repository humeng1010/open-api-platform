package com.panda.springbootinit.controller;

import com.panda.common.common.BaseResponse;
import com.panda.common.common.ErrorCode;
import com.panda.common.common.ResultUtils;
import com.panda.common.model.entity.InterfaceInvokeInfo;
import com.panda.springbootinit.annotation.AuthCheck;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.mapper.UserInterfaceInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 分析接口控制器
 * @author humeng
 */
@RestController
@RequestMapping("/analyse")
@Slf4j
public class AnalyseController {
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInvokeInfo>> listTopInvokeInterfaceInfo(){
        List<InterfaceInvokeInfo> interfaceInvokeInfos = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(5);
        if (interfaceInvokeInfos.isEmpty()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统中暂时没有接口被调用");
        }
        return ResultUtils.success(interfaceInvokeInfos);
    }
}
