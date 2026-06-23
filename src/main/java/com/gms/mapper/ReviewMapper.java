package com.gms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gms.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    List<Review> selectByTeacherId(@Param("teacherId") String teacherId);

    List<Review> selectBySubmissionId(@Param("submissionId") Integer submissionId);

    List<Review> selectBySubmissionIds(@Param("submissionIds") java.util.List<Integer> submissionIds);
}
