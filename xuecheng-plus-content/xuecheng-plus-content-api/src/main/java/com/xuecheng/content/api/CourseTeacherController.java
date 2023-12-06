package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "教师相关接口",tags = "教师相关接口接口")
@RequestMapping("/courseTeacher")
public class CourseTeacherController {
    @Autowired
    private CourseTeacherService courseTeacherService;

    @GetMapping("/list/{id}")
    public CourseTeacher queryCourseTeacher(@PathVariable Long id){
        CourseTeacher courseTeacher = courseTeacherService.queryCourseTeacher(id);
        return courseTeacher;
    }

    @PostMapping
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto){
        CourseTeacher courseTeacher = courseTeacherService.addCourseTeacher(courseTeacherDto);
        return courseTeacher;
    }

    @PutMapping
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto){
        CourseTeacher courseTeacher = courseTeacherService.updateCourseTeacher(courseTeacherDto);
        return courseTeacher;
    }

    @DeleteMapping("/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long id){
        courseTeacherService.deleteCourseTeacher(courseId, id);
    }
}
