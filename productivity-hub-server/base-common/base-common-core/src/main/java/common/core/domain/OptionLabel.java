package common.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 下拉列表类.
 *
 * @author: pbad
 * @date: 2023-9-12 10:11:42
 * @version: 1.0
 */
@Data
@ApiModel("下拉列表VO类")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OptionLabel {

    /**
     * 唯一标识(提供不同的参数字段)
     */
    @ApiModelProperty("唯一标识")
    private String key;

    /**
     * 文本描述(提供不同的参数字段)
     */
    @ApiModelProperty("文本描述")
    private String label;

    /**
     * 名称(提供不同的参数字段)
     */
    @ApiModelProperty("名称")
    private String name;

    /**
     * 值(提供不同的参数字段)
     */
    @ApiModelProperty("值")
    private String value;
}
