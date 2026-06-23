package com.gms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gms.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    List<Submission> selectBySelectionId(@Param("selectionId") Integer selectionId);

    List<Submission> selectBySelectionIds(@Param("selectionIds") java.util.List<Integer> selectionIds);
}
