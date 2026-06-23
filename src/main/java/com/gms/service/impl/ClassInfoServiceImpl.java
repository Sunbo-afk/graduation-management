package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.ClassInfo;
import com.gms.mapper.ClassInfoMapper;
import com.gms.service.ClassInfoService;
import org.springframework.stereotype.Service;

@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {
}
