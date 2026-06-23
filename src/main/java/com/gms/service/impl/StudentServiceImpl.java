package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Student;
import com.gms.mapper.StudentMapper;
import com.gms.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
}
