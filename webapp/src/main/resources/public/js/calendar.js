/* Doc for this component available at
{@link https://github.com/TimeboardIO/timeboard/blob/master/docs/vue-component/calendar.md  } */


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
                    type : 1,
                    label : ""
                }
            ])
        }
    },
    methods : {
        // To be override
        selectColor : function(event) {
            return "lightblue";
        }
    },
    template: `
            <table style="margin: 0; table-layout:fixed;" class="ui celled table unstackable calendar">
                <!-- Header with day num  (hidden if showHeader is false) -->
                <tr v-if="showHeader === true">
                  <!-- Empty lef top cells  -->
                  <th style="width: 10rem; white-space: nowrap;"  v-if="showColName === true" ></th>
                  <!-- Day num in month  -->
                  <th class="calendar-cell" v-for="day in daysInMonth" v-bind:data-label="day.date.toDateString()" >{{ day.date.getDate() }}</th>               
                  <!-- Non existing days cells to fix line cells number to 31 days -->
                  <th class="calendar-cell" v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: rgba(0,0,0,.05)"></th>        
                </tr>
                <!-- Morning event -->
                <tr>
                    <!-- Line name (hidden if showColName is false) -->
                    <td rowspan="2" v-if="showColName === true" style="width: 10rem; word-wrap: break-word" >
                    {{ name }}
                    </td>
                    <!-- Morning cell (deduce cell color from selectColor function) -->
                    <td 
                        v-for="day in daysInMonth"             
                        v-bind:style="[
                            (day.morningEvent !== undefined && selectColor(day.morningEvent)) ? {'background-color' : selectColor(day.morningEvent) } : {},
                            (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {} 
                            ] " 
                        v-bind:data-label="day.date.toDateString()" 
                        class="calendar-cell" 
                        data-position="left center" 
                        :data-tooltip="day.morningEvent !== undefined ? day.morningEvent.label : false" 
                    ></td>  
                   <!-- Non existing days cells to fix line cells number to 31 days -->
                   <td class="calendar-cell" v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: rgba(0,0,0,.05)" rowspan="2"></td>        
                </tr>
                <!-- Afternoon event -->
                <tr>
                    <!-- Afternoon cell (deduce cell color from selectColor function) -->
                    <td
                        v-for="day in daysInMonth" 
                        v-bind:style="[
                            (day.afternoonEvent !== undefined && selectColor(day.afternoonEvent)) ? {'background-color' : selectColor(day.afternoonEvent) } : {},
                            (day.date.getDay() === 0 || day.date.getDay() === 6) ? { 'background-color' : 'lightgrey' } : {} 
                            ] " 
                        v-bind:data-label="day.date.toDateString()" 
                        class="calendar-cell" 
                        data-position="left center" 
                        :data-tooltip="day.morningEvent !== undefined ? day.morningEvent.label : false" 
                    ></td>        
                </tr>
            </table>
       `,
    computed: {
        // Generate day in mouth
        daysInMonth : function() {
            // First day of month
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

// Year calendar is composed by 12 month calendar
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
    //overflow-x : scroll is user to enhance mobile experience
    template: `
        <div style="overflow-x: scroll;">
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
        // Currently static array of month name, must find a way to enhance i18n
        return {
            monthNames : ["January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            ]
        }

    }
});