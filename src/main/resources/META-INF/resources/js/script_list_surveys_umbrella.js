// Obsługa generowania linków z potwierdzeniem
u(document).on('click', 'a.generate-links', function(event) {
    event.preventDefault();
    const id = u(this).data('id');

    if (confirm('Wygenerować linki dla tego zapytania?')) {
        // Przekieruj spodziewając się GET, można użyć location.href
        window.location.href = `/api/v1/links/${id}`;
    }
});

// Obsługa usuwania listy obecności z potwierdzeniem i fetch POST z _method=delete
u(document).on('click', 'a.delete-survey', async function(event) {
    event.preventDefault();
    const id = u(this).data('id');

    if (!confirm('Czy na pewno chcesz usunąć to zapytanie?')) {
        return;
    }

    try {
        const response = await fetch(`/web/surveys/${id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: '_method=delete',
        });

        if (response.ok) {
            window.location.reload();
        } else {
            alert('Błąd przy usuwaniu zapytania');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});
