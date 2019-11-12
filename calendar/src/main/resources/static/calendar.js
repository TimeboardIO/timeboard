$("input:text").click(function() {
  $(this).parent().find("input:file").click();
});

$('input:file')
  .on('change', function(e) {
    var name = e.target.files[0].name;
    $('input:text', $(e.target).parent()).val(name);
  });
