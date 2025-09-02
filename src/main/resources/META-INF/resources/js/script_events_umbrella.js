u('#survey-form').on('submit', submitSurvey);

async function submitSurvey(event) {
    event.preventDefault();

    const name = u('#name').val();

    if (!name) {
        alert('Uzupełnij nazwę!');
        return;
    }

    const events = [];
    u('.event-entry select').each(select => {
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
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(survey),
    });

    if (response.ok) {
        window.location.href = '/web/surveys'; // przekierowanie po sukcesie
    } else {
        alert('Błąd przy zapisie listy obecności');
    }
}

function addEvent() {
    const container = u('#events-container').get(0);

    // Pobierz wybrane wartości
    const selectedIds = u('select', container).map(s => s.value);
    const availableOptions = getAvailableEventsOptions(selectedIds);

    if (availableOptions.length === 0) {
        alert('Brak dostępnych nowych wydarzeń do dodania');
        return;
    }

    // Tworzymy element div.article
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
    const entry = u(button).parent().parent().get(0);
    const container = u(entry).parent().get(0);
    container.removeChild(entry);

    // Uaktualnij nazwy selectów
    u('select', container).each((select, index) => {
        select.name = `events[${index}]`;
    });
}

function checkDuplicateEvents(changedSelect) {
    const container = u('#events-container').get(0);
    const values = u('select', container).map(s => s.value).filter(v => v);

    const duplicates = values.filter((item, idx) => values.indexOf(item) !== idx);
    if (duplicates.length > 0) {
        alert('Nie możesz dodać tego samego wydarzenia więcej niż raz!');
        changedSelect.value = '';
    }
}

function getAvailableEventsOptions(selectedIds) {
    const allEvents = availableEventsJson || [];
    return allEvents.filter(ev => !selectedIds.includes(ev.id));
}
