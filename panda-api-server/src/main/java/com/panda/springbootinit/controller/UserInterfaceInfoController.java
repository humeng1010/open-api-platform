package com.panda.springbootinit.controller;

import cn.hutool.core.bean.BeanUtil;
import com.panda.common.common.BaseResponse;
import com.panda.common.common.DeleteRequest;
import com.panda.common.common.ErrorCode;
import com.panda.common.common.ResultUtils;
import com.panda.common.constant.UserConstant;
import com.panda.common.model.dto.userInterfaceInfo.InvokeCountRequest;
import com.panda.common.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.panda.common.model.dto.userInterfaceInfo.UserInterfaceInfoUpdateRequest;
import com.panda.common.model.entity.User;
import com.panda.common.model.entity.UserInterfaceInfo;
import com.panda.common.model.vo.UserInterfaceInfoLeftCountVO;
import com.panda.springbootinit.annotation.AuthCheck;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.exception.ThrowUtils;
import com.panda.springbootinit.service.UserInterfaceInfoService;
import com.panda.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 接口管理
 *
 * @author humeng
 */
@RestController
@RequestMapping("/user_interfaceInfo")
@Slf4j
@CrossOrigin

public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;


    /**
     * 远程调用方法 producer
     * 请求接口成功后减少当前用户的请求次数
     *
     * @param invokeCountRequest 请求体
     * @return 是否成功
     */
    @PutMapping("/invokeCount")
    public boolean invokeCount(@RequestBody InvokeCountRequest invokeCountRequest) {
        Long interfaceInfoId = invokeCountRequest.getInterfaceInfoId();
        Long userId = invokeCountRequest.getUserId();

        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userInterfaceInfoService.update()
                .setSql("leftNum = leftNum - 1, totalNum = totalNum + 1")
                .eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .gt("leftNum", 0)
                .update();
    }

    @GetMapping("/left/{id}")
    public BaseResponse<UserInterfaceInfoLeftCountVO> getCurrentUserInterfaceInfoLeftCount(
            @PathVariable("id") Long interfaceInfoId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.query().select("totalNum", "leftNum").eq("userId", userId)
                .eq("interfaceInfoId", interfaceInfoId).one();
        
        UserInterfaceInfoLeftCountVO userInterfaceInfoLeftCountVO = BeanUtil.copyProperties(userInterfaceInfo, UserInterfaceInfoLeftCountVO.class);
        return ResultUtils.success(userInterfaceInfoLeftCountVO);
    }

    /**
     * 创建
     *
     * @param userInterfaceInfoAddRequest 新增
     * @param request                     请求
     * @return id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);

        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);

        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        userInterfaceInfo.setInterfaceInfoId(userInterfaceInfo.getInterfaceInfoId());

        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest 删除
     * @param request       请求
     * @return boolean
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param userInterfaceInfoUpdateRequest 更新请求
     * @return boolean
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);

        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        long id = userInterfaceInfo.getId();
        // 判断是否存在
        UserInterfaceInfo oldInterfaceInfo = userInterfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);

        return ResultUtils.success(result);
    }


}
