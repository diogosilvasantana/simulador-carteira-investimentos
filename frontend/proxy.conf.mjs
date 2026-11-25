// Proxy do `ng serve` para o backend. A porta vem de BACKEND_PORT, que o dev.cmd preenche quando a
// 8080 está ocupada (ex.: Apache do XAMPP); sem ela, usa a 8080.
const target = `http://127.0.0.1:${process.env['BACKEND_PORT'] ?? '8080'}`;

export default {
  '/api': { target, secure: false },
  '/actuator': { target, secure: false },
  '/ws': { target, secure: false, ws: true },
};
