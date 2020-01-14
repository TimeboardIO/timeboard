Vue.component('calendar', {
    props: {
        year : String,
        month : String
    },
    template: `
        <table style="margin: 0" class="ui celled table">
            <tr>
              <th v-for="day in daysInMonth"  v-bind:data-label="day.toDateString()" >{{ day.getDate() }}</th>        
            </tr>
            <tr>
              <td v-for="day in daysInMonth" v-bind:style="[day.getDay() === 0 || day.getDay() === 6 ? { 'background-color' : 'lightgrey' } : { 'background-color' : 'white' } ] " v-bind:data-label="day.toDateString()" ></td>        
            </tr>
            <tr>
              <td v-for="day in daysInMonth" v-bind:style="[day.getDay() === 0 || day.getDay() === 6 ? { 'background-color' : 'lightgrey' } : { 'background-color' : 'white' } ] " v-bind:data-label="day.toDateString()" ></td>        
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
