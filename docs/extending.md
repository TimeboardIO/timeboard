# Extended Timeboard

## Branding

Timeboard base deployment prodived a bundle named "theme". 
This bundle implements some "core" services to customize application look and fell.

To customize branding you have to provide your own theme bundle.


| Service         | Description                       |
|-----------------|-----------------------------------|
| BrandingService | Allow to provide application name |


## Tabs

Timeboard UI Layout provide a primary nav menu (home, projects, timesheet).
To add a new tab, you have to provide **NavigationExtPoint** service implementation.