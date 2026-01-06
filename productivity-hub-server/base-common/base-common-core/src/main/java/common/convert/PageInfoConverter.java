package common.convert;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import common.util.SpringCopyUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具类，用于将包含类型为 T 的元素的 PageInfo 对象转换为包含类型为 R 的 PageInfo 对象，并可选地进行空值检查。
 *
 * @param <T> 源 PageInfo 中元素的类型。
 * @param <R> 目标 PageInfo 中元素的类型。
 * @author: pbad
 * @date: 2023/9/7 17:41
 * @version: 1.0
 */
public class PageInfoConverter<T, R> {

    /**
     * 将包含类型为 T 的元素的 PageInfo 对象转换为包含类型为 R 的 PageInfo 对象，可选进行空值检查。
     *
     * @param sourcePageInfo 包含类型为 T 的元素的源 PageInfo 对象。
     * @param targetClass    目标类型 R 所对应的类。
     * @return 包含类型为 R 的元素的 PageInfo 对象。
     */
    public PageInfo<R> convertAndCheckEmpty(PageInfo<T> sourcePageInfo, Class<R> targetClass) {
        if (CollUtil.isEmpty(sourcePageInfo.getList())) {
            return new PageInfo<>();
        }
        return convertPageInfo(sourcePageInfo, targetClass);
    }

    /**
     * 将包含类型为 T 的元素的 PageInfo 对象转换为包含类型为 R 的 PageInfo 对象，不进行空值检查。
     *
     * @param sourcePageInfo 包含类型为 T 的元素的源 PageInfo 对象。
     * @param targetClass    目标类型 R 所对应的类。
     * @return 包含类型为 R 的元素的 PageInfo 对象。
     */
    public PageInfo<R> convertPageInfo(PageInfo<T> sourcePageInfo, Class<R> targetClass) {
        PageInfo<R> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(sourcePageInfo.getPageNum());
        pageInfo.setPageSize(sourcePageInfo.getPageSize());
        pageInfo.setSize(sourcePageInfo.getSize());
        pageInfo.setStartRow(sourcePageInfo.getStartRow());
        pageInfo.setEndRow(sourcePageInfo.getEndRow());
        pageInfo.setTotal(sourcePageInfo.getTotal());
        pageInfo.setPrePage(sourcePageInfo.getPrePage());
        pageInfo.setNextPage(sourcePageInfo.getNextPage());
        List<R> collect = sourcePageInfo.getList().stream()
                .map(e -> SpringCopyUtil.convert(e, targetClass))
                .collect(Collectors.toList());
        pageInfo.setList(collect);
        return pageInfo;
    }
}
