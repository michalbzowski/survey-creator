// Obsługa generowania linków z potwierdzeniem
u(document).on('click', 'a.generate-links', function(event) {
    event.preventDefault();
    const id = u(this).data('id');

    if (confirm('Czy wybrać wszystkie osoby do tej listy obecności?')) {
        // Przekieruj spodziewając się GET, można użyć location.href
        window.location.href = `/api/v1/links/${id}`;
    }
});

// Obsługa usuwania listy obecności z potwierdzeniem i fetch POST z _method=delete
u(document).on('click', 'a.delete-attendance-list', async function(event) {
    event.preventDefault();
    const id = u(this).data('id');

    if (!confirm('Czy na pewno chcesz usunąć to zapytanie?')) {
        return;
    }

    try {
        const response = await fetchWithLoader(`/web/attendance_list/${id}`, {
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

u(document).on('click', 'a.send-email-button', async function(e) {
    e.preventDefault(); // zapobiega domyślnemu wysłaniu formy
    const attendanceListId = u(this).data('attendance-list-id');
    const linkPersonId = u(this).data('link-person-id');

    try {
        const response = await fetchWithLoader(`/api/v1/links/${attendanceListId}/email/${linkPersonId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });

        if (response.ok) {
            window.location.reload();
        } else {
            alert('Błąd przy wysyłaniu e-mail');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});

u(document).on('click', '#send-to-all', async function(event) {
    event.preventDefault();
    showLoader()
    if (!confirm('Czy na pewno wysłać e-mail do wszystkich osób z listy?')) {
        hideLoader()
        return;
    }

    // Pobieramy wszystkie linki z klasą send-email-button
    const buttons = u('a.send-email-button');
    if (buttons.length === 0) {
        alert('Brak osób do wysyłki email.');
        hideLoader()
        return;
    }

    // Wysyłamy emaile kolejno do każdej osoby
    for (let i = 0; i < buttons.length; i++) {
        const btn = u(buttons.nodes[i]);
        const attendanceListId = btn.data('attendance-list-id');
        const linkPersonId = btn.data('link-person-id');

        try {
            const response = await fetchWithLoader(`/api/v1/links/${attendanceListId}/email/${linkPersonId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            });

            if (!response.ok) {
                alert(`Błąd wysyłki e-mail do osoby o ID: ${linkPersonId}`);
            }
        } catch (err) {
            alert(`Błąd połączenia przy wysyłaniu e-mail do osoby o ID: ${linkPersonId}`);
            hideLoader()
        }
    }
    hideLoader()
    alert('Wiadomości e-mail zostały wysłane do wszystkich osób.');
    // Opcjonalne odświeżenie strony po zakończeniu

    window.location.reload();
});
