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
    props: ['title', 'id', 'text', 'icon' ],
    template: `
        <div class="ui basic modal">
            <div class="ui icon header">
                <i :class="icon" class="icon"></i>
                {{ title }}
            </div>
            <div class="content">
                <p>{{ text }}</p>
            </div>
            <div class="actions">
                <div class="ui red basic cancel inverted button">
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
    data: {
        id : Math.floor(Math.random() * 10000)
    },
    methods: {
        yes : function () {

        },
        no : function () {

        }
    }
});