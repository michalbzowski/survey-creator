const CACHE_NAME = 'app-cache-v1';
const URLS_TO_CACHE = [
  '/css/style.css',
  '/css/pico.min.css',
  '/js/umbrellajs.js',
  // … inne statyczne pliki, które chcesz cache'ować
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(URLS_TO_CACHE))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );

 self.addEventListener('message', (event) => {
     if (event.data === 'SKIP_WAITING') {
         self.skipWaiting();
     }
 });
});