package com.gms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gms.entity.SysUser;
import com.gms.mapper.SysUserMapper;
import com.gms.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
