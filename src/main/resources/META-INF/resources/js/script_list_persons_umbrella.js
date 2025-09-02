// Delegacja zdarzenia click na linki .delete-person
u(document).on('click', 'a.delete-person', async function(event) {
    event.preventDefault();
    const personId = u(this).data('id');

    if (!confirm('Czy na pewno chcesz usunąć tę osobę?')) {
        return;
    }

    try {
        const response = await fetch(`/web/persons/${personId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: '_method=delete'
        });

        if (response.ok) {
            // Można np. usunąć element z DOM lub przeładować stronę
            window.location.reload();
        } else {
            alert('Błąd przy usuwaniu osoby');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});
