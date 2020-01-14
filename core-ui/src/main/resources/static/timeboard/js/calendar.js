Vue.component('calendar', {
    props: {
        year : String,
        month : String,
        name : String,
        showHeader : {
            type : Boolean,
            default : true
        },
        showColName : {
            type : Boolean,
            default : true
        }
    },
    template: `
        <table style="margin: 0;" class="ui celled table ">
            <tr v-if="showHeader === true">
              <th style="width: 10rem;"  v-if="showNameCol === true" >Name</th>
              <th v-for="day in daysInMonth" v-bind:data-label="day.toDateString()" >{{ day.getDate() }}</th>    
              <th v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: grey"></th>        
            </tr>
            <tr>
              <td style="width: 10rem;" rowspan="2" v-if="showNameCol === true" > {{ name }}</td>
              <td v-for="day in daysInMonth" v-bind:style="[ day.getDay() === 0 || day.getDay() === 6 ? { 'background-color' : 'lightgrey' } : { 'background-color' : 'white' } ] " v-bind:data-label="day.toDateString()" ></td>        
              <td v-for="index in (31 - daysInMonth.length)" :key="index" style="background-color: grey" rowspan="2"></td>        
            </tr>
            <tr>
              <td v-for="day in daysInMonth" v-bind:style="[ day.getDay() === 0 || day.getDay() === 6 ? { 'background-color' : 'lightgrey' } : { 'background-color' : 'white' } ] " v-bind:data-label="day.toDateString()" ></td>        
            </tr>
        </table>
       `,
    computed: {
        daysInMonth : function() {
            let date = new Date(this.y, this.m, 1);
            let days = [];
            while (date.getMonth() === this.m) {
                days.push(new Date(date.getTime()));
                date.setDate(date.getDate() + 1);
            }
            return days;
        }
    },
    methods: {


    },
    data : function () {
        return {
            y : parseInt(this.year, 10),
            m : parseInt(this.month, 10),
        }

    }
});
