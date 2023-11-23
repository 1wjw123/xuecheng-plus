package com.xuecheng.base.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分页参数类
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {

    // 页码
    @ApiModelProperty("页码")
    private Long pageNo = 1L;
    // 每页记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = 10L;
}
