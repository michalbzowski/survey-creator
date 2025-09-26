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
    e.preventDefault();

    const button = this;
    const attendanceListId = u(button).data('attendance-list-id');
    const linkPersonId = u(button).data('link-person-id');
    const attendanceEntryId = u(button).data('id');

    // Zamień link na ikonę klepsydry
    u(button).html(`
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
           stroke="#cc2828" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
           class="lucide lucide-hourglass-icon lucide-hourglass spin-animation">
           <path d="M5 22h14"/>
           <path d="M5 2h14"/>
           <path d="M17 22v-4.172a2 2 0 0 0-.586-1.414L12 12l-4.414 4.414A2 2 0 0 0 7 17.828V22"/>
           <path d="M7 2v4.172a2 2 0 0 0 .586 1.414L12 12l4.414-4.414A2 2 0 0 0 17 6.172V2"/>
      </svg>
    `);

    try {
        const response = await fetchWithLoader(`/api/v1/links/${attendanceListId}/send/${linkPersonId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });

        if (!response.ok) {
            alert('Błąd przy wysyłaniu e-mail');
            // Przywróć stan linku
            u(button).text('Wyślij e-mail');
            return;
        }

        // Polling: co sekundę sprawdzaj czy e-mail wysłany
        const checkInterval = setInterval(async () => {
            try {
                const statusResp = await fetch(`/api/v1/links/${attendanceEntryId}/status`, {
                    method: 'GET',
                    headers: { 'Accept': 'application/json' }
                });
                if (!statusResp.ok) {
                    throw new Error('Błąd pobierania statusu');
                }
                const statusJson = await statusResp.json();
                if (statusJson.status === 'SENT') {
                    clearInterval(checkInterval);

                    // Zamień klepsydrę na ikonę zielonego ptaszka
                    u(button).html(`
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                            stroke="#13aa2c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                            class="lucide lucide-check-icon lucide-check">
                            <path d="M20 6 9 17l-5-5"/>
                        </svg>
                    `);
                    // Opcjonalnie zdeaktywuj link
                    u(button).removeClass('send-email-button');
//                    u(button).removeAttr('href');
                }
            } catch (err) {
                console.error('Błąd podczas sprawdzania statusu e-mail:', err);
                clearInterval(checkInterval);
                u(button).text('Wyślij e-mail');
                alert('Błąd podczas sprawdzania statusu e-mail');
            }
        }, 1000);

    } catch (error) {
        alert('Błąd połączenia z serwerem');
        u(button).text('Wyślij e-mail'); // Przywróć tekst w razie błędu
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

    // Wysyłamy wiadomosc kolejno do każdej osoby
    for (let i = 0; i < buttons.length; i++) {
        const btn = u(buttons.nodes[i]);
        const attendanceListId = btn.data('attendance-list-id');
        const linkPersonId = btn.data('link-person-id');

        try {
            const response = await fetchWithLoader(`/api/v1/links/${attendanceListId}/send/${linkPersonId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
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
