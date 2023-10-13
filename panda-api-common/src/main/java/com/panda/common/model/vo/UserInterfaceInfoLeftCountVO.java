package com.panda.common.model.vo;

import lombok.Data;

/**
 * @author humeng
 */
@Data
public class UserInterfaceInfoLeftCountVO {
    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;
    
}
