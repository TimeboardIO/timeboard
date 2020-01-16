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
                        color : "lightgreen",
                        type : 0
                    },
                    {
                        date : new Date('2020-01-17'),
                        color : "lightgreen",
                        type : 1
                    },

                ]
            },
            {
                name : "User 2",
                events : [
                    {
                        date : new Date('2020-01-03'),
                        color : "lightgreen",
                        type : 1
                    },
                    {
                        date : new Date('2020-01-11'),
                        color : "lightgreen",
                        type : 2
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
                    self.teamCalendars.push({
                        name : key,
                        events : value
                    });
                }
            }
        });
    }
});