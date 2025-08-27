document.getElementById('survey-form').addEventListener('submit', submitSurvey);

async function submitSurvey(event) {
    event.preventDefault();

    const name = document.getElementById('name').value;

    if (!name) {
        alert('Uzupełnij nazwę!');
        return;
    }

    const events = [];
    document.querySelectorAll('.event-entry select').forEach(select => {
        if (select.value) {
            events.push(select.value);
        }
    });

    if (events.length === 0) {
        alert('Dodaj co najmniej jedno wydarzenie!');
        return;
    }

    const survey = {
        name,
        events
    };

    const response = await fetch('/web/surveys', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(survey),
    });

    if (response.ok) {
        window.location.href = '/web/surveys'; // przekierowanie po sukcesie
    } else {
        alert('Błąd przy zapisie ankiety');
    }
}

function addEvent() {
    const container = document.getElementById('events-container');

    // Pobierz dostępne eventy, które nie są aktualnie wybrane
    const selectedIds = Array.from(container.querySelectorAll('select')).map(s => s.value);
    const availableOptions = getAvailableEventsOptions(selectedIds);

    if (availableOptions.length === 0) {
        alert('Brak dostępnych nowych wydarzeń do dodania');
        return;
    }

    // Tworzymy element div
    const div = document.createElement('article');
    div.className = 'event-entry';

    // Tworzymy select
    const select = document.createElement('select');
    select.name = `events[${container.children.length}]`;
    select.required = true;
    select.onchange = function() {
        checkDuplicateEvents(select);
    };

    // Opcja domyślna
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '-- wybierz --';
    select.appendChild(defaultOption);

    // Dodajemy opcje do selecta
    availableOptions.forEach(event => {
        const option = document.createElement('option');
        option.value = event.id;
        option.textContent = event.name;
        select.appendChild(option);
    });

    div.appendChild(document.createElement('label')).textContent = 'Wybierz wydarzenie: ';
    div.appendChild(select);

    // Przycisk usuń
    const par = document.createElement('p');
    const btn = document.createElement('a');
    btn.textContent = 'Usuń wydarzenie';
    btn.onclick = function() {
        removeEvent(btn);
    };

    div.appendChild(par).appendChild(btn);

    container.appendChild(div);
}

function removeEvent(button) {
    const entry = button.parentNode.parentNode;
    const container = entry.parentNode;
    container.removeChild(entry);

    // Uaktualnij nazwy selectów, by indeksy były spójne
    Array.from(container.children).forEach((child, index) => {
        const select = child.querySelector('select');
        select.name = `events[${index}]`;
    });
}

function checkDuplicateEvents(changedSelect) {
    const container = document.getElementById('events-container');
    const values = Array.from(container.querySelectorAll('select')).map(s => s.value).filter(v => v);

    const duplicates = values.filter((item, idx) => values.indexOf(item) !== idx);
    if (duplicates.length > 0) {
        alert('Nie możesz dodać tego samego wydarzenia więcej niż raz!');
        changedSelect.value = '';
    }
}

function getAvailableEventsOptions(selectedIds) {
    // Dostępne eventy musi dostarczyć serwer w globalnej zmiennej JS lub można pobrać osobnym wywołaniem API (przykład statyczny tutaj)
    // Przykładowo (należy zastąpić dynamicznymi danymi z serwera):
    const allEvents =  availableEventsJson|| [];

    return allEvents.filter(ev => !selectedIds.includes(ev.id));
}
