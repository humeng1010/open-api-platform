package com.panda.springbootinit.model.dto.userInterfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 */
@Data
public class UserInterfaceInfoQueryRequest implements Serializable {
    
    private static final long serialVersionUID = -462230389276216026L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 调用用户 id
     */
    private Long userId;

    /**
     * 接口 id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 0-正常，1-禁用
     */
    private Integer status;


}