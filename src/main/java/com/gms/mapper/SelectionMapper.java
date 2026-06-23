package com.gms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gms.entity.Selection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SelectionMapper extends BaseMapper<Selection> {

    Selection selectDetailByStuId(@Param("stuId") String stuId);

    List<Selection> selectByTopicIds(@Param("topicIds") java.util.List<Integer> topicIds);
}
