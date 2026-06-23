package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Selection;
import com.gms.entity.Teacher;
import com.gms.entity.Topic;
import com.gms.mapper.SelectionMapper;
import com.gms.mapper.TeacherMapper;
import com.gms.mapper.TopicMapper;
import com.gms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private SelectionMapper selectionMapper;

    @Override
    public int countStudentsForTeacher(String teacherId) {
        List<Topic> topics = topicMapper.selectByTeacherId(teacherId);
        if (topics == null || topics.isEmpty()) {
            return 0;
        }
        List<Integer> topicIds = topics.stream()
                .map(Topic::getTopicId)
                .collect(Collectors.toList());
        List<Selection> selections = selectionMapper.selectByTopicIds(topicIds);
        return selections != null ? selections.size() : 0;
    }
}
