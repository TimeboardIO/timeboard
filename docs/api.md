## Internal API Routes descriptions
| Route         | Type   |  Description     |  Args     | Return     |
|-------------|:------:|------------------|----------|----------|
| api/tasks     | GET    | Get all project tasks  | *long* projectID | *Array\<Task>*
| api/tasks     | POST    | Create task  | *TaskWrapper* task  | *UpdateTaskResult*

|
 

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