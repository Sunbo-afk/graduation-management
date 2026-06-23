package com.gms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gms.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    List<Topic> selectAvailableTopics(@Param("keyword") String keyword,
                                       @Param("direction") String direction,
                                       @Param("teacherId") String teacherId);

    List<Topic> selectByTeacherId(@Param("teacherId") String teacherId);

    List<String> selectDistinctDirections();
}
