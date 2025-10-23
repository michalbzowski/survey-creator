// service-worker.js
const CACHE_NAME = 'my-pwa-v1';
const URLS_TO_CACHE = [
  '/',
  '/css/pico.min.css',
  '/css/style.css',
  '/js/app.js'
];

// Instalacja SW — cachowanie plików początkowych
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(URLS_TO_CACHE))
  );
  self.skipWaiting();
});

// Aktywacja SW — czyszczenie starych cache
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Pobieranie — obsługa cache fallback
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});
