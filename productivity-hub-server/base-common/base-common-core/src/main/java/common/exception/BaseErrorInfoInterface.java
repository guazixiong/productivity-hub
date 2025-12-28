package common.exception;

/**
 * 异常基础服务接口类.
 *
 * @author: pbad
 * @date: 2023/9/8 14:57
 * @version: 1.0
 */
public interface BaseErrorInfoInterface {

    /**
     * 错误码
     *
     * @return 错误码
     */
    String getErrorCode();

    /**
     * 错误描述
     *
     * @return 错误描述
     */
    String getErrorMessage();
}
