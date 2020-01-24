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
                        "slot": "async",
                        "label": "Is Async Report"
                    },
                    {
                        "slot": "actions",
                        "label": "Actions"
                    }],
                data: [],
                name: "tableReports"
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

    $("#refreshSelectedProjects").click(function() {

        $("#refreshSelectedProjects").toggleClass("loading");

        var filter = $("textarea[name='filterProject']").val();
         $.ajax({
             type: "POST",
             dataType: "json",
             data: {
                "filter": filter
             },
             url: "/reports/refreshProjectSelection",
             success: function (listProjectsFiltered) {
                 $("#listProjectsDiv").empty();
                 if(listProjectsFiltered.length > 0){
                     $.each(listProjectsFiltered, function( i, item ) {
                         var newNameItem = "<div class=\"header\"><i class=\"folder open icon\"></i>"+item.projectName+"</div>";
                         var comments = item.projectComments != null ? item.projectComments : "(No comments)";
                         var newCommentItem = "<div class=\"description\">"+comments+"</div>";

                         $("#listProjectsDiv")
                            .append(newNameItem)
                            .append(newCommentItem);
                     });
                 }else{
                    $("#listProjectsDiv").append("No projects, please click on refresh button or modify your filter. ")
                 }
                 $("#refreshSelectedProjects").toggleClass("loading");
              },
             error: function (data, textStatus, jqXHR) {
                 $("#listProjectsDiv").empty();
                 if(data.status == 500){
                    $("#listProjectsDiv").append("Your filter is not supported by the Spring Expression Language (SpEL), please modify your filter.")
                 }else{
                    $("#listProjectsDiv").append(data.responseText)
                 }
                 $("#refreshSelectedProjects").toggleClass("loading");
             }
         });
    });
});

