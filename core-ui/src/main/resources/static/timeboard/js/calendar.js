Vue.component('calendar', {
    props: {
        year : Number,
        month : Number,
        name : String,
        showHeader : {
            type : Boolean,
            default : true
        },
        showColName : {
            type : Boolean,
            default : true
        },
        events :  {
            type : Array,
            default : () => ([
                {
                    date : new Date(),
                    color : "lightblue",
                    type : 1
                }
            ])
        }
    },
    template: `
        <table style="margin: 0; table-layout:fixed;" class="ui celled table calendar">
            <tr v-if="showHeader === true">
              <th style="width: 8rem; white-space: nowrap;"  v-if="showColName === true" ></th>
              <th v-for="day in daysInMonth" v-bind:data-label="day.date.toDateString()" >{{ day.date.getDate() }}</th>    
              
              <th v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: grey"></th>        
            </tr>
            <tr>
              <td style="width: 8rem; white-space: nowrap;" rowspan="2" v-if="showColName === true" > {{ name }}</td>
              <td v-for="day in daysInMonth" v-bind:style="[
               (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {}, 
               (day.event !== undefined && day.event.type <= 1) ? {'background-color' : day.event.color } : {}
               ] " v-bind:data-label="day.date.toDateString()" ></td>  
              
               <td v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: grey" rowspan="2"></td>        
            </tr>
            <tr>
              <td v-for="day in daysInMonth" v-bind:style="[
               (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {}, 
               (day.event !== undefined && day.event.type >= 1) ? {'background-color' : day.event.color } : {}
               ] " v-bind:data-label="day.date.toDateString()" ></td>        
            </tr>
        </table>
       `,
    computed: {
        daysInMonth : function() {
            let date = new Date(this.year, this.month, 1);
            let days = [];
            while (date.getMonth() === this.month) {
                days.push( {
                    date : new Date(date.getTime()),
                    event : this.events.find(e => {
                        return (e.date.getFullYear() === date.getFullYear() &&
                            e.date.getMonth() === date.getMonth() &&
                            e.date.getDate() === date.getDate());
                    })
                });
                date.setDate(date.getDate() + 1);
            }
            return days;
        }
    },
    methods: {


    },
    data : function () {
        return {
        }

    }
});

Vue.component('year-calendar', {
    props: {
        year : Number
    },
    template: `
        <div>
            <calendar v-for="month in monthsInYear" :name="monthNames[month]" :show-header="month === 0" :year="year" :month="month"></calendar>
        </div>
       `,
    computed: {
        monthsInYear : function() {
            let date = new Date(this.year, 0, 1);
            let months = [];
            while (date.getFullYear() === this.year) {
                months.push(date.getMonth());
                date.setMonth(date.getMonth() + 1);
            }
            return months;
        },
    },
    data : function () {
        return {
            monthNames : ["January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ]
        }

    }
});