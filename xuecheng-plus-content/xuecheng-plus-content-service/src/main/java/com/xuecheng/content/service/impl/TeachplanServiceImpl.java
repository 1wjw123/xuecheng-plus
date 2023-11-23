package com.xuecheng.content.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

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

}
