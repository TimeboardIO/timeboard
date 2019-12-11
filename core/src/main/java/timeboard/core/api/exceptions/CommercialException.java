package timeboard.core.api.exceptions;

public class CommercialException  extends Exception {

    private String errCause;
    private String errMsg;

    public String getErrCode() {
        return errCause;
    }

    public void setErrCode(String errCode) {
        this.errCause = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public CommercialException(String errCause, String errMsg) {
        this.errCause = errCause;
        this.errMsg = errMsg;
    }
}
