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
                    date : new Date().toDateString().substr(0,10),
                    value : 1,
                    type : 1
                }
            ])
        }
    },
    methods : {
        selectColor : function(event) {
            return "lightblue";
        }
    },
    template: `
        <table style="margin: 0; table-layout:fixed;" class="ui celled table calendar">
            <tr v-if="showHeader === true">
              <th style="width: 10rem; white-space: nowrap;"  v-if="showColName === true" ></th>
              <th v-for="day in daysInMonth" v-bind:data-label="day.date.toDateString()" >{{ day.date.getDate() }}</th>    
              
              <th v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: rgba(0,0,0,.05)"></th>        
            </tr>
            <tr>
              <td style="width: 10rem; white-space: nowrap;" rowspan="2" v-if="showColName === true" > {{ name }}</td>
              <td v-for="day in daysInMonth" v-bind:style="[
               (day.morningEvent !== undefined && selectColor(day.morningEvent)) ? {'background-color' : selectColor(day.morningEvent) } : {},
               (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {} 
               ] " v-bind:data-label="day.date.toDateString()" ></td>  
              
               <td v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: rgba(0,0,0,.05)" rowspan="2"></td>        
            </tr>
            <tr>
              <td v-for="day in daysInMonth" v-bind:style="[
               (day.afternoonEvent !== undefined && selectColor(day.afternoonEvent)) ? {'background-color' : selectColor(day.afternoonEvent) } : {},
               (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {} 
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
                    morningEvent : this.events.find(e => {
                        let jDate = new Date(e.date);
                        return (e.type <=1) && (jDate.toDateString() === date.toDateString());
                    }),
                    afternoonEvent : this.events.find(e => {
                        let jDate = new Date(e.date);
                        return (e.type >=1) && (jDate.toDateString() === date.toDateString());
                    }),
                });
                date.setDate(date.getDate() + 1);
            }
            return days;
        }
    },
    data : function () {
        return {
        }

    }
});

Vue.component('year-calendar', {
    props: {
        year : Number,
        events :  {
            type : Array,
            default : () => ([
                {
                    date : new Date().toDateString().substr(0,10),
                    value : 1,
                    type : 1
                }
            ])
        }
    },
    template: `
        <div>
            <calendar v-for="month in monthsInYear" :name="monthNames[month]" :show-header="month === 0" :year="year" :month="month" :events="events"></calendar>
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