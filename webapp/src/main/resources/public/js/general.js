
var copyToClipboard = function (textToCopy) {
    var el = document.createElement('input');
    el.value = textToCopy;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);

    alert("Text copied: " + textToCopy);
};

