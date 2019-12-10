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

    public CommercialException(String errCode, String errMsg) {
        this.errCause = errCode;
        this.errMsg = errMsg;
    }
}
