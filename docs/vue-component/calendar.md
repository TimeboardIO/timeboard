# Datatable component
Calendar is a Timeboard VueJS component deployable on view on a Vue app to render calendar view. 

## Month calendar  
Here is a simple example of datatable implementation for a table with 3 columns : 2 data columns and an action column.
![Capture du 2020-02-27 12-14-50](https://user-images.githubusercontent.com/15018911/75439793-c5e0e380-595a-11ea-9fcd-ef22a75d3a21.png)

### Template 
```HTML
<calendar v-for="(calendar, i) in teamCalendars"
  :name="calendar.name"
  :events="calendar.events"
  :show-header="false"
  :year="year"
  :month="month">
</calendar>
```
### Javascript 
```javascript
const CustomCalendar = BaseCalendar.extend({
    methods : {
        selectColor: function(event) {
            let color = "";
            if (event.customData > 0) {
                color = "#FBBD08";
            } else {
                color = "#5BCA7E";
            }
            return color;
        }
    }
});

Vue.component("calendar", CustomCalendar);

let app = new Vue({
    el: '#teamCalendar',
    data: {
        year : new Date().getFullYear(),
        month : new Date().getMonth(),
        teamCalendars : [],   
    },
    methods : {
        loadMonthData : function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: ".." + this.year + "/" + this.month,
                success: function (d) {
                    self.teamCalendars = [];
                    for (let [key, value] of Object.entries(d)) {
                        self.teamCalendars.push({
                            name : key,
                            events : value
                        });
                    }
                }
            });
        }
    },
    mounted : function () {
        this.loadMonthData();
    }
});
```

#### Behavior 
You can extends the default selecting color behavior of component

#### Configuration 
|   Name | Type | Req | Description |
|--------|-----------|-----------|-----------|
| month   | Number | Yes |  month to display   | 
| year   | Number | Yes |  year to display   | 
| name    | String | Yes |  Name of your the calendar display on the left cell    | 
| events   | List | No |  List of events   | 
| showHeader   | Boolean | No |
  Show header on top (day num in month)    | 
| showColName   | Boolean | No |  Show columns names on left (day num in month)    | 


#### Events fields 
|   Name | Type | Req | Description |
|--------|-----------|-----------|-----------|
| date    | Date | Yes |  Name of your the calendar display on the left cell    | 
| type   | Number | Yes |  // 0 MORNING - 1 FULL DAY - 2 AFTERNOON)    | 
| label   | String | No |  Event label (display in a tooltip on hover)   | 
| events   | List | No |  List of events   | 

You can also add any custom data to your event to be able to select a color.

## Year calendar  

![Capture du 2020-02-27 12-16-19](https://user-images.githubusercontent.com/15018911/75439919-fc1e6300-595a-11ea-8b66-d221ffc9b7b4.png)


### Template 
```HTML
<year-calendar :year="calendarYear" :events="calendarData"></year-calendar>
```
### Javascript 
```javascript
const CustomCalendar = BaseCalendar.extend({
    methods : {
        selectColor: function(event) {
            let color = "";
            if (event.customData > 0) {
                color = "#FBBD08";
            } else {
                color = "#5BCA7E";
            }
            return color;
        }
    }
});

Vue.component("calendar", CustomCalendar);

let app = new Vue({
    el: '#teamCalendar',
    data: {
        year : new Date().getFullYear(),
        calendarData : {},   
    },
    methods : {
        loadCalendar: function() {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "..",
            success: function (d) {
                self.calendarData = d;
            }
        });
                }
    },
    mounted : function () {
        this.loadCalendar();
    }
});
```