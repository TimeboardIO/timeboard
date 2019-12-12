Vue.component('data-table', {
    props: ['config'],
    template: `
        <div>
          <table class="ui celled table">
              <thead>
                  <tr>
                      <th v-for="col in config.cols">{{col}}</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="row in config.data">
                      <td v-for="col in config.cols">
                          <slot :name="col" :data="row[col]"></slot>
                      </td>
                  </tr>
              </tbody>
          </table>
        </div>`
})

$(document).ready(function(){
    var app = new Vue({
      el: '#components-demo',
      data: {
        table : {
          cols: ["hello", "world"],
          data: [
            {
              "hello":"test",
              "world":"bidule"
            },
            {
              "hello":"test2",
              "world":"bidule2"
            }
          ]
        }
      }
    });
});
