package com.panda.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.common.model.entity.InterfaceInvokeInfo;
import com.panda.common.model.entity.UserInterfaceInfo;
import com.panda.common.model.vo.UserInterfaceInvokeInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    /**
     * select ii.id, ii.name,ii.method,ii.url, sum(totalNum) as 'totalNum'
     * from user_interface_info
     * left join interface_info ii on user_interface_info.interfaceInfoId = ii.id
     * group by interfaceInfoId order by sum(totalNum) desc limit 5;
     */
    List<InterfaceInvokeInfo> listTopInvokeInterfaceInfo(int limit);

    /**
     * select ii.name, uii.totalNum
     * from user_interface_info uii
     * left join interface_info ii on uii.interfaceInfoId = ii.id
     * where uii.userId = 1682950828199796738
     * and ii.isDelete = 0
     * order by uii.totalNum desc
     * limit 10;
     *
     * @param userId 登录的用户id
     * @param limit  前几条数据
     * @return 当前用户调用接口次数统计(排序)
     */
    List<UserInterfaceInvokeInfo> listTopUserInterfaceInvokeInfo(@Param("userId") long userId, @Param("limit") int limit);

}




