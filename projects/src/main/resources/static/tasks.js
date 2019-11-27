Vue.component('demo-grid', {
   template: '#grid-template',
   props: {
     tasks: Array,
     columns: Array,
     filterKey: String
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
       var filterKey = this.filterKey && this.filterKey.toLowerCase()
       var order = this.sortOrders[sortKey] || 1
       var tasks = this.tasks
       if (filterKey) {
         tasks = tasks.filter(function (row) {
           return Object.keys(row).some(function (key) {
             return String(row[key]).toLowerCase().indexOf(filterKey) > -1
           })
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