$(document).ready(function () {

    const _PROJECT_ID = $("meta[name='projectID']").attr('value');
    const _BASE_URL = $("meta[name='baseURL']").attr('value');

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
                        "primary" : true,
                        "class" : "collapsing"

                    },
                    {
                        "slot": "validated",
                        "label": "V",
                        "primary" : true,
                        "class" : "collapsing"

                    },
                    {
                        "slot": "actions",
                        "label": "Actions",
                        "primary" : true,
                        "class":"right aligned collapsing"
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
            },
            forceValidate: function(event, target, week) {
                let self = this;
                event.target.classList.toggle('loading');
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    contentType: "application/json",
                    url: "/projects/" + projectID + "/timesheets/forceValidate/" + target.id + "/" + week.year + "/" + week.week,
                    data:  JSON.stringify(target.weeks),
                    success: function (data) {
                        event.target.classList.toggle('loading');
                    },
                    error: function (data) {
                        console.log(data);
                    }
                });
            },
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