const projectID = $("meta[name='projectID']").attr('content');

const BaseCalendar = Vue.options.components["calendar"];

const CustomCalendar = BaseCalendar.extend({
    methods : {
        selectColor: function(event) {
            let color = "";
            if (event.value > 0) {
                color = "#FBBD08";
            }
            if (event.value >= 1) {
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
        monthNames : ["January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ]
    },
    methods : {
        lastMonth : function() {
            this.month--;
            if(this.month < 0) {
                this.year --;
                this.month = 11;
            }
            this.loadMonthData();
        },
        nextMonth : function() {
            this.month++;
            if(this.month > 11) {
                this.year ++;
                this.month = 0;
            }
            this.loadMonthData();
        },
        loadMonthData : function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/" + projectID + "/calendar/list/" + this.year + "/" + this.month,
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