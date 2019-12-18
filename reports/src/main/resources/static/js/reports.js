$(document).ready(function () {

    const reportID = $("meta[name='reportID']").attr('content');

    var appListReports = new Vue({
        el: '#app-create-report',
        data: {
            table: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Report Name"
                    },
                    {
                        "slot": "actions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
        },
        mounted: function () {
            var self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "reports/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });

    $( "#refreshSelectedProjects" ).click(function() {
        var filter = $( "input[name='filterProject']").val();
         $.ajax({
             type: "POST",
             dataType: "json",
             data: filter,
             contentType: "application/json",
             url: "/api/reports/refreshProjectSelection",
             success: function (listProjectsFiltered) {
                 $( "#listProjectsDiv").empty();
                 $.each(listProjectsFiltered, function( i, item ) {
                     var newNameItem = "<div class=\"header\"><i class=\"folder open icon\"></i>"+item.projectName+"</div>";
                     var comments = item.projectComments != null ? item.projectComments : "(No comments)";
                     var newCommentItem = "<div class=\"description\">"+comments+"</div>";

                     $( "#listProjectsDiv")
                        .append(newNameItem)
                        .append(newCommentItem);
                 })
              },
             errors: function (d) {
                 alert(d);
             }
         });
    });
});

