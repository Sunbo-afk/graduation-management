package com.gms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Deadline;
import com.gms.entity.Selection;
import com.gms.entity.Teacher;
import com.gms.entity.Topic;
import com.gms.mapper.DeadlineMapper;
import com.gms.mapper.SelectionMapper;
import com.gms.mapper.TeacherMapper;
import com.gms.mapper.TopicMapper;
import com.gms.service.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SelectionServiceImpl extends ServiceImpl<SelectionMapper, Selection> implements SelectionService {

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private DeadlineMapper deadlineMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    @Transactional
    public Map<String, Object> selectTopic(String stuId, Integer topicId) {
        Map<String, Object> result = new HashMap<>();

        // 1. Check if deadline for "选题" stage exists and is still valid
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LambdaQueryWrapper<Deadline> dlw = new LambdaQueryWrapper<>();
        dlw.eq(Deadline::getStage, "选题");
        Deadline deadline = deadlineMapper.selectOne(dlw);
        if (deadline != null) {
            if (today.isBefore(deadline.getStartDate()) || today.isAfter(deadline.getEndDate())) {
                result.put("success", false);
                result.put("message", "当前不在选题时间内");
                return result;
            }
        }

        // 2. Check if student already has a selection
        LambdaQueryWrapper<Selection> selw = new LambdaQueryWrapper<>();
        selw.eq(Selection::getStuId, stuId);
        if (baseMapper.selectCount(selw) > 0) {
            result.put("success", false);
            result.put("message", "您已经选过题目了");
            return result;
        }

        // 3. Check if topic exists and status is 0 (available)
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }
        if (topic.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "该题目已被选择");
            return result;
        }

        // 4. Check if the topic's teacher hasn't exceeded max_students
        Teacher teacher = teacherMapper.selectById(topic.getTeacherId());
        if (teacher != null && teacher.getMaxStudents() != null) {
            LambdaQueryWrapper<Topic> tpw = new LambdaQueryWrapper<>();
            tpw.eq(Topic::getTeacherId, topic.getTeacherId())
               .eq(Topic::getStatus, 1);
            Long usedCount = topicMapper.selectCount(tpw);
            if (usedCount >= teacher.getMaxStudents()) {
                result.put("success", false);
                result.put("message", "该教师指导学生数已满");
                return result;
            }
        }

        // 5. Update topic status to 1 (selected)
        topic.setStatus(1);
        topicMapper.updateById(topic);

        // 6. Insert selection record
        Selection selection = new Selection();
        selection.setStuId(stuId);
        selection.setTopicId(topicId);
        selection.setSelectTime(LocalDateTime.now());
        selection.setStatus("进行中");
        baseMapper.insert(selection);

        result.put("success", true);
        result.put("message", "选题成功");
        return result;
    }

    @Override
    public Selection getDetailByStuId(String stuId) {
        return baseMapper.selectDetailByStuId(stuId);
    }
}
