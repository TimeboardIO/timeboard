package timeboard.core.notification.model;

import java.io.Serializable;
import java.util.Date;

public class TimeboardEvent  implements Serializable {

    private Date eventDate;

    protected TimeboardEvent(Date date){
        this.eventDate =  date;
    }

}
