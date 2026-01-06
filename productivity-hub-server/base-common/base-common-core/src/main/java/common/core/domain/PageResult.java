package common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果封装.
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从 1 开始）
     */
    private int pageNum;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> items;

    /**
     * 构造一个空的分页结果
     */
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    /**
     * 构造一个分页结果
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> items) {
        return new PageResult<>(pageNum, pageSize, total, items);
    }
}


