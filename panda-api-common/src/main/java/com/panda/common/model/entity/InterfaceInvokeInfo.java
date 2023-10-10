package com.panda.common.model.entity;

import lombok.Data;

/**
 * @author humeng
 */
@Data
public class InterfaceInvokeInfo {
    private Long id;
    private String name;
    private String method;
    private String url;
    private Integer status;
    private Integer totalNum;
}
