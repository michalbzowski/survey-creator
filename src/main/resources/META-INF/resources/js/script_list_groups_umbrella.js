u(document).on('click', 'a.delete-group', async function(event) {
    event.preventDefault();

    const tagId = u(this).data('id');

    if (!confirm('Czy na pewno chcesz usunąć tą grupę?')) {
        return;
    }

    try {
        const response = await fetchWithLoader(`/web/groups/${tagId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: '_method=delete',
        });

        if (response.ok) {
            window.location.reload();
        } else {
            alert('Błąd przy usuwaniu grupy');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});
