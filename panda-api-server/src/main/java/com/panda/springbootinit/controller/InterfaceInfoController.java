package com.panda.springbootinit.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.client.InvokeApiClient;
import com.panda.common.common.*;
import com.panda.common.constant.UserConstant;
import com.panda.common.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.panda.common.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.panda.common.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.panda.common.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.model.entity.User;
import com.panda.common.model.entity.UserInterfaceInfo;
import com.panda.common.model.enums.InterfaceInfoStatusEnum;
import com.panda.common.model.vo.InterfaceInfoVO;
import com.panda.springbootinit.annotation.AuthCheck;
import com.panda.springbootinit.exception.BusinessException;
import com.panda.springbootinit.exception.ThrowUtils;
import com.panda.springbootinit.service.InterfaceInfoService;
import com.panda.springbootinit.service.UserInterfaceInfoService;
import com.panda.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.panda.utils.HeaderUtil.getRequestHeaderMap;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
@CrossOrigin
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;


    /**
     * 远程调用的方法 Producer
     * 根据 URL 和 method 获取接口信息
     *
     * @param path   url
     * @param method 方法
     * @return 接口信息
     */
    @GetMapping("/getInterfaceInfo")
    public InterfaceInfo getInterfaceInfoByUrlAndMethod(@RequestParam("path") String path, @RequestParam("method") String method) {
        if (StringUtils.isAnyBlank(path, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.query().like("url", path).eq("method", method).one();
    }

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


    @GetMapping("/{id}")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(@PathVariable("id") Long id) {

        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        InterfaceInfoVO interfaceInfoVO = BeanUtil.copyProperties(interfaceInfo, InterfaceInfoVO.class);

        return ResultUtils.success(interfaceInfoVO);

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


    /**
     * 发布（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        InterfaceInfo oldInterfaceInfo = getInterfaceInfoById(idRequest);

        // 单纯的url不包含查询字符串(GET)
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        String requestParams = oldInterfaceInfo.getRequestParams();

        // 如果是GET请求需要手动拼接上查询字符串
        if (Method.GET.equals(Method.valueOf(method))) {
            HttpResponse response = HttpRequest.get(url + (requestParams.equals("null") ? "" : requestParams))
                    .addHeaders(getRequestHeaderMap("i am admin", "online interface", requestParams))
                    .charset(StandardCharsets.UTF_8)
                    .execute();
            log.info("检查接口是否可用...status:{}", response.getStatus());
            if (!response.isOk()) {
                log.error("{}接口不可使用", url);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败: GET接口不能调用");
            }
        }
        // POST 未使用 未测试 !
        if ("POST".equalsIgnoreCase(method)) {
            HttpResponse httpResponse = HttpRequest
                    .post(url)
                    .addHeaders(getRequestHeaderMap("i am admin", "online interface", requestParams))
                    .charset(StandardCharsets.UTF_8)
                    .body(requestParams)
                    .execute();
            log.info("检查接口是否可用...status:{}", httpResponse.getStatus());
            if (!httpResponse.isOk()) {
                log.error("{}接口不可使用", url);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败: POST接口不能调用");
            }
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(oldInterfaceInfo.getId());
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
        InterfaceInfo interfaceInfo = getInterfaceInfoById(idRequest);

        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getStatus());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    private InterfaceInfo getInterfaceInfoById(IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();

        // 判断是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        return interfaceInfo;
    }


    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    /**
     * 调用接口
     *
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = interfaceInfoInvokeRequest.getId();

        // 根据id获取接口信息
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (Objects.equals(interfaceInfo.getStatus(), InterfaceInfoStatusEnum.OFFLINE.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }


        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 查询中间表中是否有该用户与接口的信息,如果没有则创建并且设置leftNum=100免费100次调用次数(后面开启定时任务每天重置为100)
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.query().eq("userId", userId).eq("interfaceInfoId", id).one();
        // 创建接口与用户关系数据
        if (userInterfaceInfo == null) {
            userInterfaceInfo = createInterfaceInfo(userId, id);
        }
        // 状态如果不是0则是被禁用了
        if (!Objects.equals(userInterfaceInfo.getStatus(), 0)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        // 判断是否还有次数
        Integer leftNum = userInterfaceInfo.getLeftNum();
        if (leftNum <= 0) {
            // 次数不足
            throw new BusinessException(ErrorCode.API_COUNT_ERROR);
        }


        // 准备调用接口
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        String requestParams = interfaceInfo.getRequestParams();
        // 判断请求是否需要参数
        if (!requestParams.equals("null")) {
            // 如果用户没有传递请求参数,但是该请求需要参数,我们就使用示例的值
            userRequestParams = StrUtil.isBlank(userRequestParams) ? requestParams : userRequestParams;
        }


        InvokeApiClient invokeApiClient = new InvokeApiClient(accessKey, secretKey);
        // 调用接口参数是中文会调用失败 方案一 统一转换为URL编码前端拿到后自己转换为中文
        HttpResponse httpResponse = invokeApiClient.invokeApi(method, url, userRequestParams);
        if (!httpResponse.isOk()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用接口失败");
        }
        return ResultUtils.success(httpResponse.body());
    }

    /**
     * 创建用户-接口信息
     *
     * @param userId 用户id
     * @param id     接口信息
     * @return user-interface info
     */
    private UserInterfaceInfo createInterfaceInfo(Long userId, Long id) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(id);
        // 初始100次调用次数
        userInterfaceInfo.setLeftNum(100);
        boolean save = userInterfaceInfoService.save(userInterfaceInfo);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return userInterfaceInfoService.getById(userInterfaceInfo.getId());
    }

}
