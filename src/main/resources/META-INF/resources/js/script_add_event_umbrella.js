document.addEventListener('DOMContentLoaded', function() {
    const withAttendance = document.getElementById('withAttendanceList');
    const attendanceOptions = document.getElementById('attendanceOptions');
    const groupsSelect = document.getElementById('groupsSelect');
    const personsSelect = document.getElementById('personsSelect');
    const radios = attendanceOptions.querySelectorAll('input[type="radio"][name="attendanceType"]');

    function updateVisibility() {
        if (withAttendance.checked) {
            attendanceOptions.style.display = 'block';
            const selected = [...radios].find(r => r.checked)?.value;
            groupsSelect.style.display = selected === 'group' ? 'block' : 'none';
            personsSelect.style.display = selected === 'person' ? 'block' : 'none';
        } else {
            attendanceOptions.style.display = 'none';
            groupsSelect.style.display = 'none';
            personsSelect.style.display = 'none';
        }
    }

    withAttendance.addEventListener('change', updateVisibility);
    radios.forEach(radio => radio.addEventListener('change', updateVisibility));

    updateVisibility(); // initial
    flatpickr("#datetimeInput", {
        locale: "pl",
        enableTime: true,
        time_24hr: true,
        dateFormat: "Y-m-d\\TH:i",    // format wysyłany do backendu (value elementu)
        altInput: true,               // włącz pole alternatywne do wyświetlania
        altFormat: "d F Y H:i"        // format widoczny dla użytkownika (np. 10 September 2025 18:55)
    });

});