// Obsługa kliknięcia przycisku usuwania
u(document).on('click', '.delete-event', async function(event) {
    const button = u(event.currentTarget);
    const eventId = button.data('id');

    if (!confirm('Czy na pewno chcesz usunąć to wydarzenie?')) {
        return;
    }

    try {
        const response = await fetch(`/web/events/${eventId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: '_method=delete'
        });

        if (response.ok) {
            // Opcjonalnie usuń element z DOM albo przeładuj stronę
            // np. button.closest('.event-row').remove();
            window.location.reload();
        } else {
            alert('Błąd przy usuwaniu wydarzenia');
        }
    } catch (err) {
        alert('Błąd połączenia z serwerem');
    }
});
