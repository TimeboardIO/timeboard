
var copyToClipboard = function (textToCopy) {
    var el = document.createElement('input');
    el.value = textToCopy;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);

    alert("Text copied: " + textToCopy);
};

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js')
  .then((reg) => {
    // registration worked
    console.log('Enregistrement réussi');
  }).catch((error) => {
    // registration failed
    console.log('Erreur : ' + error);
  });
}