document.addEventListener('DOMContentLoaded', function() {

    flatpickr("#datetimeInput", {
        locale: "pl",
        enableTime: true,
        time_24hr: true,
        dateFormat: "Y-m-d\\TH:i",    // format wysyłany do backendu (value elementu)
        altInput: true,               // włącz pole alternatywne do wyświetlania
        altFormat: "d F Y H:i"        // format widoczny dla użytkownika (np. 10 September 2025 18:55)
    });

});

// Po załadowaniu DOM
u('#eventForm').on('submit', function(e) {
    e.preventDefault(); // Nie wysyłaj klasycznie

    // Pobierz dane z formularza jako FormData lub obiekt
    const form = e.target;
    const data = new FormData(form);

    // Zamień na URL-encoded (np. application/x-www-form-urlencoded)
    const payload = new URLSearchParams();
    for (const pair of data) {
        payload.append(pair[0], pair[1]);
    }

    // Prześlij AJAX-em
    fetch(form.action, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: payload
    })
    .then(response => {
        if (!response.ok) {
            // Błąd (np. 400), nie odświeżaj strony
            return response.text().then(msg => {
                Toastify({
                  text: msg,
                  close: true,
                  gravity: "top", // `top` or `bottom`
                  position: "right", // `left`, `center` or `right`
                  stopOnFocus: true, // Prevents dismissing of toast on hover
                  style: {
                     background: "linear-gradient(to right, #ff5f6d, #ffc371)",
                  },
                  onClick: function(){} // Callback after click
                }).showToast();
            });
        }
        // Sukces: np. przekieruj lub wyświetl info
        if (response.redirected) {
           const location = response.url;
           if (location) window.location.href = location;
           return;
        }
    })
    .catch(err => {
        Toastify({
          text: err,
          close: true,
          gravity: "top", // `top` or `bottom`
          position: "right", // `left`, `center` or `right`
          stopOnFocus: true, // Prevents dismissing of toast on hover
          style: {
             background: "linear-gradient(to right, #ff5f6d, #ffc371)",
          },
          onClick: function(){} // Callback after click
        }).showToast();
    });
});
