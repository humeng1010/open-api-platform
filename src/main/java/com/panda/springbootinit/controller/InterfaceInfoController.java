package com.panda.springbootinit.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.client.PandaApiClient;
import com.panda.springbootinit.annotation.AuthCheck;
import com.panda.springbootinit.common.*;
import com.panda.springbootinit.constant.UserConstant;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.exception.ThrowUtils;
import com.panda.springbootinit.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.panda.springbootinit.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.panda.springbootinit.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.panda.springbootinit.model.entity.InterfaceInfo;
import com.panda.springbootinit.model.entity.User;
import com.panda.springbootinit.model.enums.InterfaceInfoStatusEnum;
import com.panda.springbootinit.model.vo.InterfaceInfoVO;
import com.panda.springbootinit.service.InterfaceInfoService;
import com.panda.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;


    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);

        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());

        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);

        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(current, size);

        Page<InterfaceInfo> page = interfaceInfoService.page(new Page<>(current, size), interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        List<InterfaceInfoVO> interfaceInfoVOS = page.getRecords().stream().map(record -> BeanUtil.copyProperties(record, InterfaceInfoVO.class)).collect(Collectors.toList());
        BeanUtil.copyProperties(page, interfaceInfoVOPage);
        interfaceInfoVOPage.setRecords(interfaceInfoVOS);
        return ResultUtils.success(interfaceInfoVOPage);
    }


    @Resource
    private PandaApiClient pandaApiClient;

    /**
     * 发布（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();

        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // TODO 下面是固定的,将来需要根据url进行调用,判断是否可以调用
        com.panda.model.entity.User user = new com.panda.model.entity.User();
        user.setUsername("pandas");
        String res = pandaApiClient.getNameByPost(user);

        if (StrUtil.isBlank(res)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getStatus());

        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();

        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);


        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getStatus());

        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

}
