package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     */
//    传入的是一个list集合，使用动态sql

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
