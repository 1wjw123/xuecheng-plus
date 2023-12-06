package com.xuecheng.content.model.dto;

import lombok.Data;

@Data
public class CourseTeacherDto {
    // id
    private Long id;
    // 课程id
    private Long courseId;
    // 教师简介
    private String introduction;
    // 教师职位
    private String position;
    // 教师姓名
    private String teacherName;
}
