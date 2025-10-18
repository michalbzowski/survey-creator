document.addEventListener('DOMContentLoaded', function() {

const withTeam = document.getElementById('withTeam');
    const teamOptions = document.getElementById('teamOptions');
    const groupsSelect = document.getElementById('groupsSelect');
    const personsSelect = document.getElementById('personsSelect');
    const radios = teamOptions.querySelectorAll('input[type="radio"][name="teamType"]');

    function updateVisibility() {
        if (withTeam.checked) {
            teamOptions.style.display = 'block';
            const selected = [...radios].find(r => r.checked)?.value;
            groupsSelect.style.display = selected === 'group' ? 'block' : 'none';
            personsSelect.style.display = selected === 'person' ? 'block' : 'none';
        } else {
            teamOptions.style.display = 'none';
            groupsSelect.style.display = 'none';
            personsSelect.style.display = 'none';
        }
    }

    withTeam.addEventListener('change', updateVisibility);
    radios.forEach(radio => radio.addEventListener('change', updateVisibility));
    updateVisibility(); // initial
 });