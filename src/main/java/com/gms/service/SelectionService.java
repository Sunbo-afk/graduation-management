package com.gms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gms.entity.Selection;

import java.util.Map;

public interface SelectionService extends IService<Selection> {

    /**
     * Student selects a topic
     * @param stuId student ID
     * @param topicId topic ID
     * @return result map with "success" boolean and "message" string
     */
    Map<String, Object> selectTopic(String stuId, Integer topicId);

    /**
     * Get selection details by student ID
     * @param stuId student ID
     * @return Selection with related entity details
     */
    Selection getDetailByStuId(String stuId);
}
