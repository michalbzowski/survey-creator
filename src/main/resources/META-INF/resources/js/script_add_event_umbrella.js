document.addEventListener('DOMContentLoaded', function() {

    flatpickr("#datetimeInput", {
        locale: "pl",
        enableTime: true,
        time_24hr: true,
        dateFormat: "Y-m-d\\TH:i",    // format wysyłany do backendu (value elementu)
        altInput: true,               // włącz pole alternatywne do wyświetlania
        altFormat: "d F Y H:i"        // format widoczny dla użytkownika (np. 10 September 2025 18:55)
    });

});