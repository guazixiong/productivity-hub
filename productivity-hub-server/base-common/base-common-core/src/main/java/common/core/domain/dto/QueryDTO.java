package common.core.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: pangdi
 * @date: 2023/9/7 16:55
 * @description: 查询基础传输类
 */
@Data
@Accessors(chain=true)
@ApiModel("查询基础传输类")
@NoArgsConstructor
@AllArgsConstructor
public class QueryDTO {

    /**
     * 分页查询的起始页数，默认值为1  使用分页插件时使用
     */
    @ApiModelProperty(value="分页查询的起始页数，默认值为1  使用分页插件时使用")
    protected int pageNum = 1;

    /**
     * 分页查询的pageSize，默认值为10
     */
    @ApiModelProperty(value="分页查询的pageSize，默认值为10")
    protected int pageSize = 10;
}
