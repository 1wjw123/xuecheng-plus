package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RequestMapping("/teachplan")
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @GetMapping("//{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        List<TeachplanDto> teachplanDtoList = teachplanService.findTeachplanTree(courseId);
        return teachplanDtoList;
    }

    @PostMapping
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @DeleteMapping("/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }

    @PostMapping("/moveup/{id}")
    public void moveup(@PathVariable Long id){
        teachplanService.moveup(id);
    }

    @PostMapping("/movedown/{id}")
    public void movedown(@PathVariable Long id){
        teachplanService.movedown(id);
    }


    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "删除媒资绑定关系")
    @DeleteMapping("/association/media/{teachplanId}/{mediaId}")
    public void deleteBindingMediaInfo(@PathVariable Long teachplanId, @PathVariable String mediaId){
        teachplanService.deleteBindingMediaInfo(teachplanId, mediaId);
    }

}
