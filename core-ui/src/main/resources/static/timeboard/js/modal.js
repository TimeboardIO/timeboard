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