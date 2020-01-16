const projectID = $("meta[name='projectID']").attr('content');

const BaseCalendar = Vue.options.components["calendar"];

const CustomCalendar = BaseCalendar.extend({
    methods : {
        selectColor: function(event) {
            let color = "";
            if (event.value > 0) {
                color = "orange";
            }
            if (event.value >= 1) {
                color = "lightgreen";
            }
            return color;
        }
    }
});

Vue.component("calendar", CustomCalendar);




let app = new Vue({
    el: '#teamCalendar',
    data: {
        teamCalendars : [
            {
                name : "User 1",
                events : [
                    {
                        date : new Date('2020-01-20'),
                        type : 0,
                        value : 0
                    },
                    {
                        date : new Date('2020-01-17'),
                        type : 1,
                        value : 0
                    },

                ]
            },
            {
                name : "User 2",
                events : [
                    {
                        date : new Date('2020-01-03'),
                        color : "lightgreen",
                        type : 1,
                        value : 0
                    },
                    {
                        date : new Date('2020-01-11'),
                        type : 2,
                        value : 0
                    },

                ]
            },
        ]
    },
    mounted : function () {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "projects/" + projectID + "/calendar/list",
            success: function (d) {
                self.teamCalendars = [];
                for (let [key, value] of Object.entries(d)) {
                    let mergedEvents = [];
                    value.forEach(e1 => {
                        let duplicatesEvents = value.filter(e2 => e1.date === e2.date);
                        if(duplicatesEvents.length > 1) {
                            let mergedEvent = {};
                            duplicatesEvents.forEach(e2 => {
                                mergedEvent.date = e2.date;
                                mergedEvent.value = e2.value;
                                if((e2.type === 1)
                                    || (e2.type === 0 && mergedEvent.type !== undefined && mergedEvent.type === 2)
                                    || (e2.type === 2 && mergedEvent.type !== undefined && mergedEvent.type === 0)) {
                                    mergedEvent.type = 1;
                                }
                                if(mergedEvent.type === undefined){
                                    mergedEvent.type = e2.type;
                                }
                            });
                            mergedEvents.push(mergedEvent);
                        } else {
                            mergedEvents.push(e1);
                        }
                    });

                    self.teamCalendars.push({
                        name : key,
                        events : mergedEvents
                    });
                }
            }
        });
    }
});