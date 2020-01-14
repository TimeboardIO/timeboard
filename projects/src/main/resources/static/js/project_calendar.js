let app = new Vue({
    el: '#teamCalendar',
    data: {
        teamCalendars : [
            {
                name : "Bob Moranne",
                events : [
                    {
                        date : new Date('2020-01-20'),
                        color : "yellow",
                        type : 0
                    },
                    {
                        date : new Date('2020-01-17'),
                        color : "yellow",
                        type : 1
                    },

                ]
            },
            {
                name : "Léa Chacal",
                events : [
                    {
                        date : new Date('2020-01-03'),
                        color : "blue",
                        type : 1
                    },
                    {
                        date : new Date('2020-01-11'),
                        color : "yellow",
                        type : 2
                    },

                ]
            },
        ]
    },
    mounted : function () {

    }
});