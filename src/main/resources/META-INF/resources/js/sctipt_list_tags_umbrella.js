u(document).on('click', 'a.delete-tag', async function(event) {
    event.preventDefault();

    const tagId = u(this).data('id');

    if (!confirm('Czy na pewno chcesz usunąć ten tag?')) {
        return;
    }

    try {
        const response = await fetchWithLoader(`/web/tags/${tagId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: '_method=delete',
        });

        if (response.ok) {
            window.location.reload();
        } else {
            alert('Błąd przy usuwaniu tagu');
        }
    } catch {
        alert('Błąd połączenia z serwerem');
    }
});
