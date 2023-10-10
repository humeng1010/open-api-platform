package com.panda.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.common.model.entity.InterfaceInvokeInfo;
import com.panda.common.model.entity.UserInterfaceInfo;

import java.util.List;


public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    /**
     * select ii.id, ii.name,ii.method,ii.url, sum(totalNum) as 'totalNum'
     * from user_interface_info
     *          left join interface_info ii on user_interface_info.interfaceInfoId = ii.id
     * group by interfaceInfoId order by sum(totalNum) desc limit 5;
     */
    List<InterfaceInvokeInfo> listTopInvokeInterfaceInfo(int limit);

}




