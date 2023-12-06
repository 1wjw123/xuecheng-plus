package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public CourseTeacher queryCourseTeacher(Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, id);
        CourseTeacher courseTeacher = courseTeacherMapper.selectOne(queryWrapper);
        return courseTeacher;
    }

    @Override
    public CourseTeacher addCourseTeacher(CourseTeacherDto courseTeacherDto) {
        // 添加教师
        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtil.copyProperties(courseTeacherDto, courseTeacher);
        courseTeacher.setCreateDate(LocalDateTime.now());
        courseTeacherMapper.insert(courseTeacher);
        return courseTeacher;

    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto) {
        // 修改教师
        CourseTeacher courseTeacher = courseTeacherMapper.selectById(courseTeacherDto.getId());
        if (courseTeacher == null) {
            XueChengPlusException.cast("教师不存在");
        }
        BeanUtil.copyProperties(courseTeacherDto, courseTeacher);
        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long id) {
        courseTeacherMapper.deleteById(id);
    }
}
