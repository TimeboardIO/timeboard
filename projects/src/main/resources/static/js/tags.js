Vue.component('data-table', {
    props: ['config'],
    template: `
          <table class="ui celled table">
              <thead>
                  <tr>
                      <th v-for="col in config.cols">{{col.label}}</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="(row, i) in config.data">
                      <td v-for="(col, j) in config.cols">
                          <slot :name="col.slot" v-bind:row="config.data[i]">
                          </slot>
                      </td>
                  </tr>
              </tbody>
          </table>`
});

$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');


    var app = new Vue({
        el: '#components-demo',
        data: {
            table: {
                cols: [
                    {
                        "slot": "tagkey",
                        "label": "Tag Key"
                    },
                    {
                        "slot": "tagvalue",
                        "label": "Tag Value"
                    },
                    {
                        "slot": "tagactions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
            addTag: function () {
                var self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "/projects/" + projectID + "/tags",
                    data: {
                        "tagKey": "New Tag",
                        "tagValue": "New Value"
                    },
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            },
            updateTag: function (row) {
                var self = this;
                $.ajax({
                    type: "PATCH",
                    dataType: "json",
                    data: row,
                    url: "/projects/" + projectID + "/tags/" + row.id,
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            },
            removeTag: function (row) {
                var self = this;
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "/projects/" + projectID + "/tags/" + row.id,
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            }
        },
        mounted: function () {
            var self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "/projects/" + projectID + "/tags/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
