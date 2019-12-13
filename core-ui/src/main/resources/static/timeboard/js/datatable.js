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