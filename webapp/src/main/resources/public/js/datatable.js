/* Doc for this component available at
{@link https://github.com/TimeboardIO/timeboard/blob/master/docs/vue-component/data-table.md  } */

Vue.component('data-table', {
    props: ['config', 'table'],
    template: `
            <table class="ui celled table" v-if="table.length > 0">
                <!-- Table header -->
                <thead>
                    <tr>
                        <th v-for="col in finalCols"  v-if="col.visible" @click="sortBy(col.slot)"  v-bind:class="col.class"> 
                            {{col.label}} 
                            <!-- Sort caret -->
                            <i v-if="col.visible && col.sortKey" class="icon caret" :class="sortOrders[col.slot] > 0 ? 'up' : 'down'"></i> 
                        </th>
                         <!-- Cogwheel icon use to show config modal -->
                        <th v-if="config.configurable === true" class="collapsing" style="border-left: none;" ><i class="cog icon" @click="showConfigModal()"></i></th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(row, i) in finalData">
                    <!-- Data cells (override by slots in template). Can add custom class to columns  -->
                      <td v-for="(col, j) in finalCols" v-if="col.visible" v-bind:class="col.class"  >
                          <slot :name="col.slot" v-bind:row="finalData[i]">
                          </slot>
                      </td>
                      <!-- Last empty column for cogwheel in header -->
                      <td v-if="config.configurable === true" style="border-left: none;" ></td>
                    </tr>
                </tbody>
                <!-- Config modal -->
                <tmodal 
                    v-bind:title="'Column config '+config.name"
                    v-bind:id=" 'configModal' + config.name.replace(/ /g,'') ">
                    <template v-slot:content>
                        <table class="ui celled table">
                            <thead>
                                <tr>
                                  <th>Column</th>
                                  <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <!-- Column config toggle  (primary cols are not configurable) -->
                                <tr v-for="(col, j) in finalCols" v-if="col.primary !== true" >
                                  <td>
                                     {{ col.slot }}
                                  </td>
                                   <td>
                                     <input type="checkbox" id="checkbox" v-model="col.visible" v-bind:name="col.slot" >
                                  </td>
                                </tr>
                            </tbody>
                        </table>
                        <div  @click="changeDataTableConfig($event)" class="ui positive submit right labeled icon button">
                            Save
                            <i class="checkmark icon"></i>
                        </div>
                    </template>
                </tmodal>
            </table>

`,
    data: function () {

        // check data-table configuration
        if(!this.table) {
            Logger.error("[DATA-TABLE] you have to specify 'table' props.");
        }
        if(!this.config) {
            Logger.error("[DATA-TABLE] you have to specify 'config' props.");
        }

        let sortOrders = {};
        let finalCols = [];

        // Data-table does not affect the data directly
        // here we make a copy of data to finalcol
        this.config.cols.forEach(function (key) {
            sortOrders[key.slot] = 1;
            key.visible = true;
            let col = Object.assign({}, key);
            finalCols.push(col);
        });

        let self = this;
        // if data-table is configurable, load column configuration from db
        if (this.config.configurable === true) {
            if(!this.config.name) {
                Logger.error("[DATA-TABLE] No name have been set for configuration saving")
            } else {
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "/api/datatable?tableID=" + this.config.name,
                    success: function (d) {
                        self.finalCols
                            .forEach(function (c) {
                                let visible = d.colNames.includes(c.slot);
                                // primary columns are always visible
                                c.visible = (c.primary === true || visible);
                            });
                    }
                });
            }
        }
        return {
            finalCols : finalCols,
            sortKey: '',
            sortOrders: sortOrders
        }
    },
    computed: {
        finalData: function () {

            let sortKey = this.sortKey+'';
            let order = this.sortOrders[sortKey] || 1 ;

            // copying raw data
            let finalData = [];
            this.table.forEach(row => {
                finalData.push(row);
            });

            //filtering
            if(this.config.filters){
                for (let [key, filter] of Object.entries(this.config.filters)) {
                    if(filter.filterValue !== '') {
                        finalData = finalData.filter(function (row) {
                            return filter.filterFunction(filter.filterValue, row[filter.filterKey]);
                        });
                    }
                }
            }

            //sorting
            if (sortKey) {
                let sortAttr = this.config.cols.find(item => item.slot === sortKey).sortKey;
                finalData = finalData.slice().sort(function (a, b) {
                    a = a[sortAttr];
                    b = b[sortAttr];
                    return (a === b ? 0 : a > b ? 1 : -1) * order;
                });
            }
            return finalData;
        }
    },
    methods: {
        sortBy: function (key) {
            this.sortKey = key;
            this.sortOrders[key] = this.sortOrders[key] * -1
        },
        showConfigModal: function() {
            $( '#configModal' + this.config.name.replace(/ /g, '' )).modal({
                onApprove : function($element) {
                },
                detachable : true, centered: true
            }).modal('show');
        },
        changeDataTableConfig: function(event) {

            let self = this;
            event.target.classList.toggle('loading');

            let cols = [];
            this.finalCols.forEach(function (col) {
               if(col.visible) cols.push(col.slot);
            });
            // config name is currently used as key in db for saving config for this table
            $.ajax({
                type: "POST",
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    colNames : cols,
                    tableID : this.config.name,
                    userID : 0
                }),
                url: "/api/datatable",
                success: function (d) {
                    event.target.classList.toggle('loading');
                    $('#configModal'+self.config.name.replace(/ /g, '' )).modal('hide');
                }
            });
        }

    }
});