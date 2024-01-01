package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;



    @Autowired
    TeachplanService teachplanService;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseById(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        // 约束管理
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        // 当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)){
            XueChengPlusException.cast("当前为待审核状态，审核完成可以再次提交。");
        }
        // 本机构只允许提交本机构的课程
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("不允许提交其他机构的课程");
        }
        // 课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        // 添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        // 课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseById(courseId);
        BeanUtil.copyProperties(courseBaseInfo, coursePublishPre);
        // 课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        // 将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        // 查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size() == 0){
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        // 将课程计划json加入预发布表
        coursePublishPre.setTeachplan(teachplanTreeJson);
        // 设置预发布记录状态为已提交
        coursePublishPre.setStatus("202003");
        // 教学机构id
        coursePublishPre.setCompanyId(companyId);
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 查询预发布表
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null){
            // 没有记录  添加
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            // 预发布信息已存在  修改
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程信息基本表的发布状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        // 查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        // 本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        // 审核状态
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布");
        }
        // 将预发布表信息写入发布表
        saveCoursePublish(courseId);

        // 保存消息表
        saveCoursePublishMessage(courseId);
        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @description 保存课程发布信息
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublish(Long courseId){
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        // 拷贝预发布表信息到发布表
        BeanUtil.copyProperties(coursePublishPre, coursePublish);
        // 修改发布状态为已发布
        coursePublish.setStatus("203002");

        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null){
            // 发布信息不存在 插入
            coursePublishMapper.insert(coursePublish);
        }else {
            // 发布信息已经存在 修改
            coursePublishMapper.updateById(coursePublish);
        }
        // 修改课程信息基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * @description 保存消息表记录，稍后实现
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage == null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(122L);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);

        }catch (Exception e){
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String objectName = "course/"+courseId+".html";
        String course = mediaServiceClient.uploadFile(multipartFile, objectName);
        if(course==null){
            XueChengPlusException.cast("上传静态文件异常");
        }
    }
}











