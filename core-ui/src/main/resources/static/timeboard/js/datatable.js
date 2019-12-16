Vue.component('data-table', {
    props: ['config'],
    template: `
            <table class="ui celled table">
                <thead>
                    <tr>
                      <th class="collapsing" ><i class="cog icon" @click="showConfigModal()"></i></th>
                      <th v-for="col in config.cols" v-if="col.sortKey" @click="sortBy(col.slot)" >{{col.label}} <i class="icon caret" :class="sortOrders[col.slot] > 0 ? 'up' : 'down'"> </th>
                      <th v-for="col in config.cols" v-if="!col.sortKey" >{{col.label}} </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(row, i) in sortedItems">
                      <td></td>
                      <td v-for="(col, j) in config.cols">
                          <slot :name="col.slot" v-bind:row="config.data[i]">
                          </slot>
                      </td>
                    </tr>
                </tbody>
                <tmodal 
                    v-bind:title="'Column config'"
                    v-bind:id="'configModal'">
                    <template v-slot:content>
                        <table class="ui celled table">
                            <thead>
                                <tr>
                                  <th>Column</th>
                                  <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(col, j) in config.cols" >
                                  <td >
                                     {{col.slot}}
                                  </td>
                                   <td >
                                     <input type="checkbox" v-bind:name="col.slot" checked>
                                  </td>
                                </tr>
                            </tbody>
                        </tmodal>
                        </table>
                    </template>
                </tmodal>
            </table>
            

`,
    data: function () {
        let sortOrders = {};
        this.config.cols.forEach(function (key) {
            sortOrders[key.slot] = 1;
        });
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "config/" + this.config.name + "/table",
            success: function (d) {
                self.config.cols
                   .forEach(function(c) {
                       let visible = d.colNames.includes(c.slot) ;
                       c.visible = (c.primary === true  || visible);
                   });
            }
        });

        return {
            sortKey: '',
            sortOrders: sortOrders
        }
    },
    computed: {
        sortedItems: function () {
            let sortKey = this.sortKey+'';
            let order = this.sortOrders[sortKey] || 1 ;
            let sortedData = this.config.data;
            if (sortKey) {
                let sortAttr = this.config.cols.find(item => item.slot === sortKey).sortKey;
                sortedData = this.config.data.slice().sort(function (a, b) {
                    a = a[sortAttr];
                    b = b[sortAttr];
                    return (a === b ? 0 : a > b ? 1 : -1) * order;
                });
            }
            this.config.data = sortedData;
            return sortedData;
        }
    },
    methods: {
        sortBy: function (key) {
            this.sortKey = key;
            this.sortOrders[key] = this.sortOrders[key] * -1
        },
        showConfigModal: function() {
            $('#configModal').modal({
                onApprove : function($element) {

                },
                detachable : true, centered: true
            }).modal('show');
        }

    }
});