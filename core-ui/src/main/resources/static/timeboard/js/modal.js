Vue.component('tmodal', {
    props: ['title', 'id'],
    template: `
         <div v-bind:id="id" class="ui modal">
            <i class="close icon"></i>
                <div class="header">
                    {{ title }}
                </div>
            <i class="close icon"></i>
            <div class="content">
                <slot name="content"></slot>
            </div>

        </div>`,
    computed: {

    },
    methods: {


    }
});

Vue.component('tmodal-confirm', {
    props: ['title', 'text', 'icon' ],
    template: `
        <div v-bind:id="id" class="ui mini modal">
            <div class="ui icon header">
                <i :class="icon" class="icon"></i>
                {{ title }}
            </div>
            <div class="content">
                <p>{{ text }}</p>
            </div>
            <div class="actions">
                <div class="ui red cancel inverted button">
                    <i class="remove icon"></i>
                    No
                </div>
                <div class="ui green ok inverted button">
                    <i class="checkmark icon"></i>
                    Yes
                </div>
            </div>
        </div>
    `,
    data: function () {
        return {
            id: Math.floor(Math.random() * 10000),
            callback : function () { },
        }
    },
    methods: {
        yes : function () {
            this.callback();
            this.close();
        },
        no : function () {
            this.close();

        },
        confirm :  function (element, callback) {

            let self = this;

            this.text = element;

            $('#'+this.id).modal({ detachable : true, centered: true }).modal({
                onDeny    : function(){
                    self.close();
                },
                onApprove : function() {
                    self.callback();
                    self.close();
                }
            }).modal('show');

            this.callback = callback;
        },
        close : function () {
            $('#'+this.id).modal({ detachable : true, centered: true }).modal('hide');
            this.callback = function () { }
        }
    }
});