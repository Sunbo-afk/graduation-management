package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.Deadline;
import com.gms.mapper.DeadlineMapper;
import com.gms.service.DeadlineService;
import org.springframework.stereotype.Service;

@Service
public class DeadlineServiceImpl extends ServiceImpl<DeadlineMapper, Deadline> implements DeadlineService {
}
