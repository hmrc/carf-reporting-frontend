document.addEventListener('DOMContentLoaded', function(event) {

    // handle print click
    var printLink = document.querySelector('.print-link');
    if (printLink !== null) {
        var html = printLink.innerHTML;
        printLink.innerHTML = '<a class="govuk-link" href="#">' + html + '</a>';

        printLink.addEventListener('click', function(e){
            e.preventDefault();
            e.stopPropagation();
            window.print();
        });
    }

});
