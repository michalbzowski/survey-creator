    function showLoader() {
      u('#loader').each(el => el.style.display = 'flex'); // np. flex dla wyrównania
    }

    function hideLoader() {
      u('#loader').each(el => el.style.display = 'none');
    }

    function fetchWithLoader(url, options) {
      showLoader();
      return fetch(url, options)
        .then(response => {
          hideLoader();
          return response;
        })
        .catch(error => {
          hideLoader();
          throw error;
        });
    }

    async function getUsername() {
        if (localStorage.getItem('registeredUserId')) {
            return;
        }
        try {
            const response = await fetch(`/sec/username`, {
                method: 'GET',
            });

            if (response.ok) {
                const text = await response.text();
                const json = JSON.parse(text);
                u('#registered-user').text(json.username);
                u('#registered-uuid').text(json.uuid);
            } else {
                alert('Błąd przy pobieraniu nazwy usera');
            }
        } catch {
            alert('Błąd kot połączenia z serwerem');
        }
    }

    document.addEventListener("DOMContentLoaded", function() {
        const toggle = document.getElementById("nav-toggle");
        const menu = document.getElementById("nav-menu");
        getUsername()
        toggle.addEventListener("click", () => {
            menu.classList.toggle("show");
            // Dla poprawy dostępności aria-expanded:
            const expanded = toggle.getAttribute("aria-expanded") === "true";
            toggle.setAttribute("aria-expanded", String(!expanded));
        });
    });

    document.addEventListener('DOMContentLoaded', function() {
      u('a[href]').on('click', function(event) {
        const link = event.currentTarget;

        // Możesz tu dodać warunki np. wykluczyć # lub linki z target="_blank"
        if (!link.target || link.target === '_self') {
          showLoader();
          // Zezwól na normalne przejście, loader zostanie pokazany
          // Nie blokujemy zdarzenia, nie wywołujemy preventDefault
        }
      });
    });

    window.addEventListener('pageshow', function(event) {
      // jeśli strona ładowana z cache, ukryj loader
      if (event.persisted) {
        hideLoader();
      }
    });

function userHandler() {
    return {
        username: "",
        uuid: "",
        async loadUser() {
            let uuid = localStorage.getItem('uuid')
            if (uuid) {
                // tu można np. pobrać username z localStorage (jeśli zapisujesz)\
                this.connectWebSocket(uuid);
                return {
                    'username': localStorage.getItem('username'),
                    'uuid': uuid
                    };
                }
            try {
                const response = await fetch('/sec/username', { method: 'GET' });
                if (response.ok) {
                    const json = await response.json();
                    this.username = json.username;
                    this.uuid = json.uuid;
                    localStorage.setItem('username', this.username)
                    localStorage.setItem('uuid',  this.uuid)
                    this.connectWebSocket(json.uuid);
                } else {
                    alert('Błąd przy pobieraniu nazwy usera');
                }
            } catch {
                alert('Błąd kot połączenia z serwerem');
            }
        },
        connectWebSocket(userId) {
                    if (!userId) return;
                    this.ws = new WebSocket(`ws://localhost:8080/wss/${userId}`);
                    this.ws.onopen = () => {
                        console.log('WebSocket connected for', userId);
                    };
                    this.ws.onmessage = (evt) => {
                        // tutaj obsłuż swoje komunikaty
                        console.log('WS msg:', evt.data);
                    };
                    this.ws.onclose = () => {
                        console.log('WebSocket closed');
                        // Możesz dodać auto-reconnect, jeśli chcesz
                    };
                }
    }
}
