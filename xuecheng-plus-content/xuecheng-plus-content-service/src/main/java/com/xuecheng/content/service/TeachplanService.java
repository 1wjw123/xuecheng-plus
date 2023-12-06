package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TeachplanService {

    /**
     * 查询课程计划树型结构
     * @param courseId 课程id
     * @return
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 保存或修改课程计划
     * @param saveTeachplanDto 课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param id
     */
    void deleteTeachplan(Long id);

    /**
     * 课程计划上移
     * @param id
     */
    void moveup(Long id);

    /**
     * 课程计划下移
     * @param id
     */
    void movedown(Long id);

}
