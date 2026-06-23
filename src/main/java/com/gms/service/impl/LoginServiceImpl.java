package com.gms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gms.entity.Student;
import com.gms.entity.SysUser;
import com.gms.entity.Teacher;
import com.gms.mapper.StudentMapper;
import com.gms.mapper.SysUserMapper;
import com.gms.mapper.TeacherMapper;
import com.gms.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> authenticate(String userId, String password, String role) {
        Map<String, Object> result = new HashMap<>();

        if ("STUDENT".equals(role)) {
            Student student = studentMapper.selectById(userId);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学号不存在");
                return result;
            }
            // For placeholder passwords during development, accept direct match
            if ("$2a$10$placeholder".equals(student.getPassword())
                    || passwordEncoder.matches(password, student.getPassword())) {
                result.put("success", true);
                result.put("userId", student.getStuId());
                result.put("userName", student.getStuName());
                result.put("role", "STUDENT");
                return result;
            }
            result.put("success", false);
            result.put("message", "密码错误");
            return result;

        } else if ("TEACHER".equals(role)) {
            Teacher teacher = teacherMapper.selectById(userId);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "工号不存在");
                return result;
            }
            if ("$2a$10$placeholder".equals(teacher.getPassword())
                    || passwordEncoder.matches(password, teacher.getPassword())) {
                result.put("success", true);
                result.put("userId", teacher.getTeacherId());
                result.put("userName", teacher.getTeacherName());
                result.put("role", "TEACHER");
                return result;
            }
            result.put("success", false);
            result.put("message", "密码错误");
            return result;

        } else if ("ADMIN".equals(role)) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, userId);
            SysUser sysUser = sysUserMapper.selectOne(wrapper);
            if (sysUser == null) {
                result.put("success", false);
                result.put("message", "用户名不存在");
                return result;
            }
            if ("$2a$10$placeholder".equals(sysUser.getPassword())
                    || passwordEncoder.matches(password, sysUser.getPassword())) {
                result.put("success", true);
                result.put("userId", String.valueOf(sysUser.getUserId()));
                result.put("userName", sysUser.getRealName() != null ? sysUser.getRealName() : sysUser.getUsername());
                result.put("role", "ADMIN");
                return result;
            }
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        result.put("success", false);
        result.put("message", "无效的角色类型");
        return result;
    }
}
