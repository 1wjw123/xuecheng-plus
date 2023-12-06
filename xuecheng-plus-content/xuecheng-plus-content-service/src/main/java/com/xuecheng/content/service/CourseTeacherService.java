package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;


public interface CourseTeacherService {

    /**
     * 查询教师
     * @param id
     * @return
     */
    CourseTeacher queryCourseTeacher(Long id);

    /**
     * 添加教师
     * @param courseTeacherDto
     * @return
     */
    CourseTeacher addCourseTeacher(CourseTeacherDto courseTeacherDto);

    /**
     * 修改教师信息
     * @param courseTeacherDto
     * @return
     */
    CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto);

    /**
     * 删除教师
     * @param courseId
     * @param id
     */
    void deleteCourseTeacher(Long courseId, Long id);
}
