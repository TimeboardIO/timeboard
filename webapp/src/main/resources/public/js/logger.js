
const LogLevelType = {
    DEBUG : 'DEBUG',
    ERROR : 'ERROR',
    INFO : 'INFO',
    LOG : 'LOG',
    TRACE : 'TRACE',
    WARN : 'WARN',
};

 class Logger {

    static debug(message) {
        console.debug(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.DEBUG, message)
    }
    static error(message) {
        console.error(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.ERROR, message)
    }
    static info(message) {
        console.info(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.INFO, message)
    }
    static log(message) {
        console.log(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.LOG, message)
    }
    static trace(message) {
        console.trace(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.TRACE, message)
    }
    static warn(message) {
        console.warn(message); // REMOVE THIS LINE IN PRODUCTION
        this.logServer(LogLevelType.WARN, message)
    }

    static logServer(level, message) {
        $.ajax({
            method: "POST",
            url: "/js-log",
            data: {
                level : level,
                message : message
            },
        });
    }

}



