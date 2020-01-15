const projectID = $("meta[name='projectID']").attr('content');

let app = new Vue({
    el: '#teamCalendar',
    data: {
        teamCalendars : [
            {
                name : "Bob Moranne",
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
                name : "Léa Chacal",
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