package com.xiaozhi.dao;

import java.util.List;

import com.xiaozhi.entity.SysDevice;

/**
 * 设备信息 数据层
 * 
 * @author Joey
 * 
 */
public interface DeviceMapper {
  List<SysDevice> query(SysDevice device);

  int update(SysDevice device);

  int add(SysDevice device);

}