# Datatable component
Datatable is a Timeboard VueJS componenent depoyable on view on app to render table. 
## Basic 
Here is a simple example of datatable implementation for a table with 3 columns : 2 data columns and an action column.
### Template 
```HTML
<data-table v-bind:config="table">
    <template v-slot:col1="{row}">
        {{ row.val1 }}
    </template>
    <template v-slot:col1="{row}">
        {{ row.val2 }}
    </template>
    <template v-slot:actions="{row}">
        <button v-on:click="remove(row)">
            Remove
        </button>
        <button v-on:click="update(row)">
            Update
        </button>
    </template>
</data-table>
    
```
### Javascript 
```javascript
let app = new Vue({
        el: '#components-demo',
        data: {
            table: {
                cols: [
                    {
                        "slot": "col1",
                        "label": "Value 1",
                        "sortKey": "val1",
                        "primary" : true
                    },
                    {
                        "slot": "col2",
                        "label": "Value 2",
                        "sortKey": "val2",
                    },
                    {
                        "slot": "actions",
                        "label": "Actions",
                        "primary" : true
                    }],
                data: [],
                name: 'My table',
                configurable : true
            }
        },
        methods: {
            update: function (row) {
                ...
            },
            remove: function (row) {
                ...
            }
        },
        mounted: function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "...",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
});
```
## Configuration 
#### Global
|   Name | Description |
|--------|-----------|
| configurable   | Boolean : Add or remove possibility to configure columns in HMI    | 
| name   | String : Name of your table use to display in configuration modal and key for database configuration    | 
| filters   | List of filters  : Optional list of filters   | 
#### Columns
|   Name | Description |
|--------|-----------|
| slot   | String : slot name in template **SHOULD BE LOWERCASE** | 
| label   | String : label show for column in table header  | 
| primary   | Boolean : if true this columns can not be hidden by configurations    | 
| sortKey   | String : data key use to filter data | 
