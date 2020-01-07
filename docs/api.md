## Internal API Routes descriptions
| Route         | Type   |  Description     |  Args     | Return     |
|-------------|:------:|------------------|----------|----------|
| api/tasks     | GET    | Get all project tasks  | *long* projectID | *Array\<Task>*
| api/tasks/graph    | GET    | Get data for graph  | *long* taskID | *TaskGraphWrapper*
| api/tasks     | POST    | Create task  | *TaskWrapper* task  | *UpdateTaskResult*
| api/tasks/delete     | GET    | Delete  task | *long* taskID | ACK
| api/tasks/approve     | GET    | Approve task  | *long* taskID | ACK
| api/tasks/deny     | GET    | Deny task  | *long* taskID | ACK
| api/timesheet | GET    | Get timesheet data  | *int* week, int year | *TimesheetData*
| api/timesheet | POST    | update imputations  or effort left | *String* type,  *Date* day, *long* task, *String* imputation  | ACK
| api/timesheet/validate  | GET    | Validate timesheet  | *int* week, int year | ACK
 

###Normalized JSON data objects


|   TaskWrapper | |
|--------|-----------|
| long   | taskID    |
| long   | projectID |
| String |  taskName |
| String | taskComments|
| Date   |  startDate | 
| Date   |  endDate   |
| double   | originalEstimate|
| long   | typeID|
| String | assignee |
| long   | assigneeID|
| String | status|
| long   |  milestoneID|