package com.xuecheng.content.model.dto;


import lombok.Data;
import lombok.ToString;

/**
 * 课程查询条件模型类
 */
@Data
@ToString
public class QueryCourseParamsDto {
    // 课程名称
    private String courseName;
    // 审核状态
    private String auditStatus;
    // 发布状态
    private String publishStatus;
}
