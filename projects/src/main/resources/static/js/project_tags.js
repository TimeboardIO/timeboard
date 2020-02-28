$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('value');

    let app = new Vue({
        el: '#projectTags',
        data: {
            tagsListConfig: {
                cols: [
                    {
                        "slot": "tagkey",
                        "label": "Tag Key",
                        "sortKey": "tagKey",
                        "primary" : true

                    },
                    {
                        "slot": "tagvalue",
                        "label": "Tag Value",
                        "sortKey": "tagValue",
                        "primary" : true

                    },
                    {
                        "slot": "tagactions",
                        "label": "Actions",
                        "primary" : true
                    }],
                name: 'tableTag',
                configurable : false
            },
            tagsListData: []
        },
        methods: {
            addTag: function () {
                let self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "projects/" + projectID + "/tags",
                    data: {
                        "tagKey": "New Tag",
                        "tagValue": "New Value"
                    },
                    success: function (d) {
                        d.forEach(r => r.edition = false);
                        self.tagsListData = d;
                    }
                });
            },
            updateTag: function (row) {
                let self = this;
                $.ajax({
                    type: "PATCH",
                    dataType: "json",
                    data: row,
                    url: "projects/" + projectID + "/tags/" + row.id,
                    success: function (d) {
                        d.forEach(r => r.edition = false);
                        self.tagsListData = d;
                    }
                });
            },
            removeTag: function (row) {
                let self = this;
                this.$refs.confirmModal.confirm("Are you sure you want to delete "+ row.tagKey + "?", function() {
                    $.ajax({
                        type: "DELETE",
                        dataType: "json",
                        url: "projects/" + projectID + "/tags/" + row.id,
                        success: function (d) {
                            d.forEach(r => r.edition = false);
                            self.tagsListData = d;
                        }
                    });
                });
            }

        },
        mounted: function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/" + projectID + "/tags/list",
                success: function (d) {
                    d.forEach(r => r.edition = false);
                    self.tagsListData = d;
                }
            });
        }
    });
});
