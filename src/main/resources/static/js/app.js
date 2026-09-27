// Confirm before destructive actions (forms with data-confirm)
document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (event) {
        if (!window.confirm(form.dataset.confirm)) {
            event.preventDefault();
        }
    });
});

// Show only the fields for the chosen stock type on the add/edit form
(function () {
    var sections = document.querySelectorAll('[data-type-section]');
    if (!sections.length) return;

    function currentType() {
        var checked = document.querySelector('input[name="itemType"]:checked');
        if (checked) return checked.value;
        var hidden = document.querySelector('input[name="itemType"][type="hidden"]');
        return hidden ? hidden.value : 'PRINTED';
    }

    function update() {
        var type = currentType();
        sections.forEach(function (section) {
            section.hidden = section.dataset.typeSection !== type;
        });
    }

    document.querySelectorAll('input[name="itemType"]').forEach(function (input) {
        input.addEventListener('change', update);
    });
    update();
})();
