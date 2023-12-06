package com.xuecheng.content.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     *课程计划查询
     * @param courseId 课程id
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtoList = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtoList;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 课程计划id
        Long id = saveTeachplanDto.getId();
        if (id == null){
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtil.copyProperties(saveTeachplanDto, teachplan);
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplan.setOrderby(count);
            teachplanMapper.insert(teachplan);
        }else {
            // 修改
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtil.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    /**
     * 获取最新的排序号
     * @param courseId 课程id
     * @param parentid 父课程计划id
     * @return 排序等级
     */
    private int getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId , courseId).eq(Teachplan::getParentid, parentid);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count + 1;
    }

    @Override
    @Transactional
    public void deleteTeachplan(Long id) {
        // 根据id删除章节
        teachplanMapper.deleteById(id);
        // 查看是否有子章节
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, id);
        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper);
        if (teachplanList.isEmpty()){
            // 该章节已经是子章节
            // 查看章节是否有关联的 媒体资源
            LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(TeachplanMedia::getTeachplanId, id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper1);
            if (teachplanMedia != null){
                // 删除媒体资源
                Long TeachplanMediaId = teachplanMedia.getId();
                teachplanMediaMapper.deleteById(TeachplanMediaId);
            }
        }else {
            // 有子章节
            // 用来存放 子章节id
            List<Long> ids = new ArrayList<>();
            teachplanList.stream().forEach(item -> ids.add(item.getId()));
            // 删除子章节
            teachplanMapper.deleteBatchIds(ids);
            // 删除子章节关联的媒体资源
            ids.forEach(item -> {
                LambdaQueryWrapper<TeachplanMedia> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.eq(TeachplanMedia::getTeachplanId, item);
                TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper2);
                if (teachplanMedia != null){
                    // 删除媒体资源
                    Long TeachplanMediaId = teachplanMedia.getId();
                    teachplanMediaMapper.deleteById(TeachplanMediaId);
                }
            });
        }
    }

    @Override
    public void moveup(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        // 父节点id
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        // 上级排序等级
        int orderbyUp = orderby - 1;
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid).eq(Teachplan::getOrderby, orderbyUp);
        Teachplan teachplan1 = teachplanMapper.selectOne(queryWrapper);
        if (teachplan1 == null){
            XueChengPlusException.cast("已经是最上面");
        }
        teachplan1.setOrderby(orderby);
        teachplan.setOrderby(orderbyUp);
        teachplanMapper.updateById(teachplan1);
        teachplanMapper.updateById(teachplan);
    }

    @Override
    public void movedown(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        // 父节点id
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        // 下级排序等级
        int orderbyDown = orderby + 1;
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid).eq(Teachplan::getOrderby, orderbyDown);
        Teachplan teachplan1 = teachplanMapper.selectOne(queryWrapper);
        if (teachplan1 == null){
            XueChengPlusException.cast("已经是最下面");
        }
        teachplan1.setOrderby(orderby);
        teachplan.setOrderby(orderbyDown);
        teachplanMapper.updateById(teachplan1);
        teachplanMapper.updateById(teachplan);
    }


}
