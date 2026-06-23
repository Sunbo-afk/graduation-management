package com.gms.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * Get score rankings by department
     * @param deptId department ID, null for all departments
     * @return list of student score data sorted descending by final score
     */
    List<Map<String, Object>> scoreRankingByDept(Integer deptId);

    /**
     * Get teacher student count statistics
     * @return list of teacher data with current student count and max capacity
     */
    List<Map<String, Object>> teacherStudentCount();

    /**
     * Get department completion statistics
     * @param deptId department ID, null for all departments
     * @return map with "deptStats" key containing list of department completion data
     */
    Map<String, Object> deptCompletionStats(Integer deptId);

    /**
     * Get overall dashboard statistics
     * @return map with studentCount, teacherCount, topicCount, completedCount, completionRate
     */
    Map<String, Object> dashboardStats();
}
