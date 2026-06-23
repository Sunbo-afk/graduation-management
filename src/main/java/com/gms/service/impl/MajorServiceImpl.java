package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Major;
import com.gms.mapper.MajorMapper;
import com.gms.service.MajorService;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
}
