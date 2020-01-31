const _PROJECT_ID = $("meta[name='projectID']").attr('value');
const _BASE_URL = $("meta[name='baseURL']").attr('value');

$(document).ready(function () {

    let app = new Vue({
        el: '#timesheetValidationApp',

        data: {
            baseURL: _BASE_URL,
            timesheets : [],
            config: {
                cols: [
                    {
                        "slot": "week",
                        "label": "Week",
                        "sortKey": "week",
                        "primary" : true,
                        "class" : "collapsing"
                    },
                    {
                        "slot": "period",
                        "label": "Period",
                        "sortKey": "week",
                        "primary" : true

                    },
                    {
                        "slot": "submitted",
                        "label": "S",
                        "sortKey": "validated",
                        "primary" : true,
                        "class" : "collapsing"

                    },
                    {
                        "slot": "validated",
                        "label": "V",
                        "sortKey": "submitted",
                        "primary" : true,
                        "class" : "collapsing"

                    }],
                data: [],
                name: 'tableTimesheetValidation',
                configurable : false,
                sortable: false
            }
        },
        methods: {
            getDateOfWeek: function (w, y, n) {
                let simple = new Date(y, 0, n + 1 + (w - 1) * 7);
                let dow = simple.getDay();
                let isoWeekStart = simple;
                if (dow <= 4)
                    isoWeekStart.setDate(simple.getDate() - simple.getDay() + 1);
                else
                    isoWeekStart.setDate(simple.getDate() + 8 - simple.getDay());
                return isoWeekStart;
            }
        },
        mounted: function () {
            let self = this;
            $('.ui.dimmer').addClass('active');
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/" + _PROJECT_ID + "/timesheets/listProjectMembersTimesheets",
                success: function (d) {
                    self.timesheets = d;
                    $('.ui.dimmer').removeClass('active');

                }
            });
        }
    });

    $('.ui.accordion')
        .accordion();
});