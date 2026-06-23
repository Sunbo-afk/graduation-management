package com.gms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gms.entity.ClassInfo;
import com.gms.entity.Department;
import com.gms.entity.Major;
import com.gms.entity.Selection;
import com.gms.entity.Student;
import com.gms.entity.Teacher;
import com.gms.entity.Topic;
import com.gms.mapper.ClassInfoMapper;
import com.gms.mapper.DepartmentMapper;
import com.gms.mapper.MajorMapper;
import com.gms.mapper.SelectionMapper;
import com.gms.mapper.StudentMapper;
import com.gms.mapper.TeacherMapper;
import com.gms.mapper.TopicMapper;
import com.gms.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private MajorMapper majorMapper;

    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Override
    public List<Map<String, Object>> scoreRankingByDept(Integer deptId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Selection> selections = selectionMapper.selectList(null);

        for (Selection sel : selections) {
            if (sel.getFinalScore() != null) {
                Student stuWithInfo = studentMapper.selectByStuIdWithInfo(sel.getStuId());
                if (stuWithInfo != null && stuWithInfo.getDeptName() != null) {
                    if (deptId != null) {
                        Department dept = departmentMapper.selectById(deptId);
                        if (dept == null || !dept.getDeptName().equals(stuWithInfo.getDeptName())) {
                            continue;
                        }
                    }

                    Map<String, Object> row = new HashMap<>();
                    row.put("stuId", sel.getStuId());
                    row.put("stuName", stuWithInfo.getStuName());
                    row.put("deptName", stuWithInfo.getDeptName());
                    row.put("majorName", stuWithInfo.getMajorName());
                    row.put("className", stuWithInfo.getClassName());

                    Topic topic = topicMapper.selectById(sel.getTopicId());
                    row.put("topicTitle", topic != null ? topic.getTitle() : "");
                    row.put("finalScore", sel.getFinalScore());
                    result.add(row);
                }
            }
        }

        // Sort descending by final score
        result.sort((a, b) -> {
            BigDecimal sa = (BigDecimal) a.get("finalScore");
            BigDecimal sb = (BigDecimal) b.get("finalScore");
            return sb.compareTo(sa);
        });

        return result;
    }

    @Override
    public List<Map<String, Object>> teacherStudentCount() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Teacher> teachers = teacherMapper.selectList(null);

        for (Teacher teacher : teachers) {
            // Count topics selected from this teacher
            LambdaQueryWrapper<Topic> tw = new LambdaQueryWrapper<>();
            tw.eq(Topic::getTeacherId, teacher.getTeacherId());
            List<Topic> topics = topicMapper.selectList(tw);

            int selectedCount = 0;
            for (Topic topic : topics) {
                LambdaQueryWrapper<Selection> selw = new LambdaQueryWrapper<>();
                selw.eq(Selection::getTopicId, topic.getTopicId());
                selectedCount += selectionMapper.selectCount(selw);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("teacherId", teacher.getTeacherId());
            row.put("teacherName", teacher.getTeacherName());
            row.put("studentCount", selectedCount);
            row.put("maxStudents", teacher.getMaxStudents());
            result.add(row);
        }

        return result;
    }

    @Override
    public Map<String, Object> deptCompletionStats(Integer deptId) {
        Map<String, Object> result = new HashMap<>();
        List<Department> departments;

        if (deptId != null) {
            Department dept = departmentMapper.selectById(deptId);
            departments = dept != null ? Collections.singletonList(dept) : Collections.emptyList();
        } else {
            departments = departmentMapper.selectList(null);
        }

        List<Map<String, Object>> deptStats = new ArrayList<>();
        for (Department dept : departments) {
            Map<String, Object> deptStat = new HashMap<>();
            int total = 0;
            int completed = 0;

            // Navigate: Department -> Major -> ClassInfo -> Student -> Selection
            LambdaQueryWrapper<Major> mw = new LambdaQueryWrapper<>();
            mw.eq(Major::getDeptId, dept.getDeptId());
            List<Major> majors = majorMapper.selectList(mw);

            for (Major major : majors) {
                LambdaQueryWrapper<ClassInfo> cw = new LambdaQueryWrapper<>();
                cw.eq(ClassInfo::getMajorId, major.getMajorId());
                List<ClassInfo> classes = classInfoMapper.selectList(cw);

                for (ClassInfo clazz : classes) {
                    LambdaQueryWrapper<Student> sw = new LambdaQueryWrapper<>();
                    sw.eq(Student::getClassId, clazz.getClassId());
                    List<Student> students = studentMapper.selectList(sw);
                    total += students.size();

                    for (Student student : students) {
                        LambdaQueryWrapper<Selection> selw = new LambdaQueryWrapper<>();
                        selw.eq(Selection::getStuId, student.getStuId())
                             .eq(Selection::getStatus, "已完成");
                        completed += selectionMapper.selectCount(selw);
                    }
                }
            }

            deptStat.put("deptName", dept.getDeptName());
            deptStat.put("totalStudents", total);
            deptStat.put("completedStudents", completed);
            deptStat.put("completionRate", total > 0 ? Math.round(completed * 10000.0 / total) / 100.0 : 0);
            deptStats.add(deptStat);
        }

        result.put("deptStats", deptStats);
        return result;
    }

    @Override
    public Map<String, Object> dashboardStats() {
        Map<String, Object> result = new HashMap<>();

        result.put("studentCount", studentMapper.selectCount(null));
        result.put("teacherCount", teacherMapper.selectCount(null));
        result.put("topicCount", topicMapper.selectCount(null));

        LambdaQueryWrapper<Selection> selw = new LambdaQueryWrapper<>();
        selw.eq(Selection::getStatus, "已完成");
        long completedCount = selectionMapper.selectCount(selw);
        long totalSelection = selectionMapper.selectCount(null);

        result.put("completedCount", completedCount);
        result.put("completionRate",
                totalSelection > 0 ? Math.round(completedCount * 10000.0 / totalSelection) / 100.0 : 0);

        return result;
    }
}
