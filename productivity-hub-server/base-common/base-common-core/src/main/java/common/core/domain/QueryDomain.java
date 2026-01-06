package common.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 查询基础域.
 *
 * @author: pbad
 * @date: 2023/11/15 00:01:24
 * @version: 1.0
 */
@Data
public class QueryDomain {

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 删除标志: 0:正常,1:删除
     */
    private String delFlag;

    /**
     * 分页查询的起始页数，默认值为1  使用分页插件时使用
     */
    protected int pageNum = 1;

    /**
     * 分页查询的pageSize，默认值为10
     */
    protected int pageSize = 10;

    /**
     * 操作人 varchar(50)
     */
    private String operateBy;
}
