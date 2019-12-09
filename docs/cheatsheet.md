# Timeboard Cheats

## Where is my database configuration ?

    [KRONOPS_HOME]/etc/org.ops4j.datasource-timeboard-core-ds.cfg

    
## How to not redeploy everythings ?

This command auto redeploy new installed maven artifacts 
    
    @Autowired
    public UserInfo userInfo;
    ...
    final User actor = this.userInfo.getCurrentUser();
