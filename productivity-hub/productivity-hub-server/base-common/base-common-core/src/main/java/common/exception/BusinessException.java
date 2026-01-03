package common.exception;

/**
 * 业务异常类.
 *
 * @author: pbad
 * @date: 2023/8/30 15:46
 * @version: 1.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -788314347541917090L;

    /**
     * 错误码
     */
    protected String errorCode;

    /**
     * 错误信息
     */
    protected String errorMessage;

    public BusinessException() {
        super();
    }

    public BusinessException(BaseErrorInfoInterface errorInfoInterface) {
        super(String.valueOf(errorInfoInterface.getErrorCode()));
        this.errorCode = String.valueOf(errorInfoInterface.getErrorCode());
        this.errorMessage = errorInfoInterface.getErrorMessage();
    }

    public BusinessException(BaseErrorInfoInterface errorInfoInterface, Throwable cause) {
        super(String.valueOf(errorInfoInterface.getErrorCode()), cause);
        this.errorCode = String.valueOf(errorInfoInterface.getErrorCode());
        this.errorMessage = errorInfoInterface.getErrorMessage();
    }

    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorCode, String errorMessage) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
