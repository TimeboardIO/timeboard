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
        step: {
            type: Number,
        default: 1,
        },
        value: {
            type: Number,
        default: NaN,
        }
    },
    template: `
    <div
        :class="{
        'number-input--inline': inline,
            'number-input--center': center,
            'number-input--controls': controls,
        }"
        class="number-input"
        v-on="listeners"
        >
            <input
                :disabled="disabled"
                :max="max"
                :min="min"
                :name="name"
                :placeholder="placeholder"
                :readonly="readonly || !inputtable"
                :step="step"
                :value="currentValue"
                @change="change"
                @paste="paste"
                autocomplete="off"
                ref="input"
                type="number"
                v-bind="attrs"
            >
    </div>
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
            let newValue = this.rounded ? Math.round(value) : value;
            if (this.min <= this.max) {
                newValue = Math.min(this.max, Math.max(this.min, newValue));
            }
            this.currentValue = newValue;
            if (newValue === oldValue) {
                // Force to override the number in the input box (#13).
                this.$refs.input.value = newValue;
            }
            this.$emit('change', newValue, oldValue);
        },
    }
});


