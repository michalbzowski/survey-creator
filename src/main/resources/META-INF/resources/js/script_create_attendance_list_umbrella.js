
// Obsługa submit formularza
u('#attendance-list-form').on('submit', async function(event) {
    event.preventDefault();
    const name = u('#name').nodes[0].value.trim() || ""; //Wydaje mi się, że ta nazwa do niczego mi nie służy

//    if (!name) {
//        alert('Uzupełnij nazwę!');
//        return;
//    }

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

    const attendanceList = { name, events };

    try {
        const response = await fetch('/web/attendance_list', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(attendanceList),
        });

        if (response.ok) {
            const result = await response.json();
            window.location.href = '/web/attendance_list/' + result.id + '/details';
        } else {
            alert('Błąd przy zapisie listy obecności');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});

// Dodawanie nowego wydarzenia do listy obecności
function addEvent() {
    const containerSelection = u('#events-container');
    if (containerSelection.length === 0) {
        alert('Brak kontenera na wydarzenia!');
        return;
    }

    const container = containerSelection.nodes[0];
    const selectedIds = u('select', container).nodes.map(s => s.value);
    const availableOptions = getAvailableEventsOptions(selectedIds);

    if (availableOptions.length === 0) {
        alert('Brak dostępnych nowych wydarzeń do dodania');
        return;
    }

    const div = document.createElement('article');
    div.className = 'event-entry';

    const select = document.createElement('select');
    select.name = `events[${container.children.length}]`;
    select.required = true;
    select.onchange = function() {
        checkDuplicateEvents(this);
    };

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '-- wybierz --';
    select.appendChild(defaultOption);

    availableOptions.forEach(event => {
        const option = document.createElement('option');
        option.value = event.id;
        option.textContent = event.nameWithFormatedLocalDateTime ?? event.name;
        select.appendChild(option);
    });

    div.appendChild(document.createElement('label')).textContent = 'Wybierz wydarzenie: ';
    div.appendChild(select);

    const par = document.createElement('p');
    const btn = document.createElement('a');
    btn.href = '#';
    btn.textContent = 'Usuń wydarzenie z tej listy obecności';
    btn.onclick = function(e) {
        e.preventDefault();
        removeEvent(btn);
    };

    div.appendChild(par).appendChild(btn);

    container.appendChild(div);
}

// Usuwanie wydarzenia
function removeEvent(button) {
    const entry = u(button).parent().parent().nodes[0];
    const container = u(entry).parent().nodes[0];
    container.removeChild(entry);

    u('select', container).each((select, index) => {
        select.name = `events[${index}]`;
    });
}

// Sprawdzanie duplikatów wybranych wydarzeń
function checkDuplicateEvents(changedSelect) {
    const containerSelection = u('#events-container');
    if (containerSelection.length === 0) {
        console.error('Brak elementu #events-container w DOM');
        return;
    }

    const container = containerSelection.nodes[0];
    const selectsArray = u('select', container).nodes;

    const values = selectsArray.map(s => s.value).filter(v => v);
    const duplicates = values.filter((item, idx) => values.indexOf(item) !== idx);

    if (duplicates.length > 0) {
        alert('Nie możesz dodać tego samego wydarzenia więcej niż raz!');
        changedSelect.value = '';
    }
}

// Pomocnicza funkcja do pobrania dostępnych eventów (niezaznaczonych)
function getAvailableEventsOptions(selectedIds) {
    return availableEventsJson.filter(ev => !selectedIds.includes(ev.id));
}

document.addEventListener('DOMContentLoaded', () => {
  u('.event-entry select').each(function(select) {
    // Pobierz wszystkie opcje poza domyślną ("-- wybierz --")
    const options = Array.from(select.options).filter(o => o.value !== '');

    // Szukaj opcji o wartości równej eventId
      const optionToSelect = options.find(o => o.value === selectEventId);

      if(optionToSelect) {
        select.value = optionToSelect.value;
        // Opcjonalnie możesz wywołać zmianę (np. update UI, walidacja)
        select.dispatchEvent(new Event('change'));
      }
  });
});
