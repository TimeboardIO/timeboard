Vue.component('demo-grid', {
   template: '#grid-template',
   props: {
     tasks: Array,
     columns: Array,
     filterKey: Array,
   },
   data: function () {
     var sortOrders = {}
     this.columns.forEach(function (key) {
       sortOrders[key] = 1
     })
     return {
       sortKey: '',
       sortOrders: sortOrders
     }
   },
   computed: {
     filteredTasks: function () {
       var sortKey = this.sortKey
       var filterKey = this.filterKey.filter(function (f) { return f.value != '' });
       var order = this.sortOrders[sortKey] || 1
       var tasks = this.tasks
       var keepThis = this;
       if (filterKey.length > 0) {
         tasks = tasks.filter(function (row) {
            return filterKey.some(function(f, i, array){
               return String(row[f.key]).toLowerCase().indexOf(String(f.value).toLowerCase()) > -1
            });
         })
       }
       if (sortKey) {
         tasks = tasks.slice().sort(function (a, b) {
           a = a[sortKey]
           b = b[sortKey]
           return (a === b ? 0 : a > b ? 1 : -1) * order
         })
       }
       return tasks
     }
   },
   filters: {
     capitalize: function (str) {
       return str.charAt(0).toUpperCase() + str.slice(1)
     }
   },
   methods: {
     sortBy: function (key) {
       this.sortKey = key
       this.sortOrders[key] = this.sortOrders[key] * -1
     }
   }
 })

 // bootstrap the demo
var app = new Vue({
    el: '#tasksList',
    data: {
     searchQuery: '',
     searchQueries: [{key : 'taskName', value: ''}, {key : 'taskComment', value: ''}, {key : 'startDate', value: ''},
      {key : 'endDate', value: ''}, {key : 'originalEstimate', value: ''}, {key : 'assignee', value: ''}],
     gridColumns: ['taskName', 'taskComment', 'startDate', 'endDate', 'originalEstimate', 'assignee'],
     gridData: [ ]
    }
});

$(document).ready(function(){
    const project = $("meta[property='tasks']").attr('project');

    $.get("/projects/tasks/api?action=getTasks&project="+project)
    .then(function(data){
         app.gridData = data;
    });
});