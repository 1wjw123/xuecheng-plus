package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        // 模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        // 根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        // 根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        // 分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();

        PageResult<CourseBase> result = new PageResult<>(items, total, pageParams.getPageNo());
        //System.out.println(result);
        return result;
    }

    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        /*//合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
            //throw new RuntimeException("课程名称为空");
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            //throw new RuntimeException("课程分类为空");
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            //throw new RuntimeException("课程分类为空");
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            //throw new RuntimeException("课程等级为空");
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            //throw new RuntimeException("教育模式为空");
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            //throw new RuntimeException("适应人群为空");
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            //throw new RuntimeException("收费规则为空");
            throw new XueChengPlusException("收费规则为空");
        }*/
        CourseBase courseBaseNew = new CourseBase();
        BeanUtil.copyProperties(addCourseDto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //修改时间
        courseBaseNew.setChangeDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0){
            //throw new RuntimeException("课程添加失败");
            throw new XueChengPlusException("课程添加失败");
        }
        //向课程营销表保存课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        BeanUtil.copyProperties(addCourseDto, courseMarketNew);
        courseMarketNew.setId(courseBaseNew.getId());
        int i = saveCourseMarket(courseMarketNew);
        if (i <= 0){
            //throw new RuntimeException("保存课程营销信息失败");
            throw new XueChengPlusException("保存课程营销信息失败");
        }
        return getCourseBaseById(courseBaseNew.getId());
    }


    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            //throw new RuntimeException("收费规则没有选择");
            throw new XueChengPlusException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                //throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
            }
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarket == null){
            // 新增
            return courseMarketMapper.insert(courseMarketNew);
        }else {
            // 修改
            BeanUtil.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarket);
        }
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseById(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtil.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtil.copyProperties(courseMarket, courseBaseInfoDto);

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //课程id
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        //校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        BeanUtil.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        BeanUtil.copyProperties(editCourseDto, courseMarket);
        int i1 = courseMarketMapper.updateById(courseMarket);
        if (i <= 0 || i1 <= 0){
            XueChengPlusException.cast("修改课程失败");
        }
        CourseBaseInfoDto courseBaseInfo = getCourseBaseById(courseId);
        return courseBaseInfo;
    }
}
