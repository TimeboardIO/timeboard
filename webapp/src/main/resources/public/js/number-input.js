const isNaN = Number.isNaN || window.isNaN;
const REGEXP_NUMBER = /^-?(?:d+|d+.d+|.d+)(?:[eE][-+]?d+)?$/;
const REGEXP_DECIMALS = /.d*(?:0|9){10}d*$/;
const normalizeDecimalNumber = (value, times = 100000000000) => (
    REGEXP_DECIMALS.test(value) ? (Math.round(value * times) / times) : value
);

Vue.component('number-input', {
    name: 'NumberInput',
    model: {
        event: 'change',
    },
    props: {
        attrs: {
            type: Object,
            default: undefined,
        },
        center: Boolean,
        controls: Boolean,
        disabled: Boolean,
        inputtable: {
            type: Boolean,
            default: true,
        },
        min: {
            type: Number,
            default: -Infinity,
        },
        max: {
            type: Number,
            default: Infinity,
        },
        name: {
            type: String,
            default: undefined,
        },
        placeholder: {
            type: String,
            default: undefined,
        },
        readonly: Boolean,
        rounded: Boolean,
        size: {
            type: String,
            default: undefined,
        },
        trigger: {
            type: Function,
            default: undefined,
        },
        validationStep: {
            type: Number,
            default: 1,
        },
        inputStep: {
            type: Number,
            default: 1,
        },
        value: {
            type: Number,
            default: NaN,
        }
    },
    template: `

            <input
                class="number-input"
                :disabled="disabled"
                :max="max"
                :min="min"
                :name="name"
                :placeholder="placeholder"
                :readonly="readonly || !inputtable"
                :step="inputStep"
                :value="currentValue"
                @change="change"
                @paste="paste"
                autocomplete="off"
                ref="input"
                type="number"
                v-bind="attrs"
                v-on="listeners"
            >

    `,

    data() {
        return {
            currentValue: NaN,
        };
    },
    computed: {
        /**
         * Filter listeners
         * @returns {Object} Return filtered listeners.
         */
        listeners() {
            const listeners = { ...this.$listeners };
            delete listeners.change;
            listeners.click = function (event) {
                const input = event.target;
                input.focus();
                input.select();
            };
            let self = this;
            listeners.keypress = (event) => {
                if (event.which === 13 ||event.keyCode === 13) {
                    self.trigger(event);
                }
            };
            listeners.focusout = this.trigger;
            return listeners;
        },
    },
    watch: {
        value: {
            immediate: true,
            handler(newValue, oldValue) {
                if (
                    // Avoid triggering change event when created
                    !(isNaN(newValue) && typeof oldValue === 'undefined')
                    // Avoid infinite loop
                    && newValue !== this.currentValue
                ) {
                    this.setValue(newValue);
                }
            },
        },
    },
    methods: {
        /**
         * Change event handler.
         * @param event{string} value - The new value.
         */
        change(event) {
            this.setValue(Math.min(this.max, Math.max(this.min, event.target.value)));
        },
        /**
         * Paste event handler.
         * @param {Event} event - Event object.
         */
        paste(event) {
            const clipboardData = event.clipboardData || window.clipboardData;
            if (clipboardData && !REGEXP_NUMBER.test(clipboardData.getData('text'))) {
                event.preventDefault();
            }
        },
        /**
         * Set new value and dispatch change event.
         * @param {number} value - The new value to set.
         */
        setValue(value) {
            const oldValue = this.currentValue;
            let newValue = Math.round(value * (1 / this.validationStep)) / (1 / this.validationStep) ;
            if (this.min <= this.max) {
                newValue = Math.min(this.max, Math.max(this.min, newValue));
            }
            this.currentValue = newValue;
            if (newValue === oldValue) {
                // Force to override the number in the input box (#13).
                this.$refs.input.value = newValue;
            }
            this.$emit('change', newValue, oldValue);
        }
    }
});


