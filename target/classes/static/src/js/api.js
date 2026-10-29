// Utilidades compartidas por todas las páginas
const $ = id => document.getElementById(id);

// Escapa texto de usuarios antes de usarlo en innerHTML
const esc = texto => String(texto ?? '').replace(/[&<>"']/g,
  c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

async function api(url, opciones) {
  const res = await fetch(url, opciones);
  const texto = await res.text();
  let datos = null;
  try { datos = texto ? JSON.parse(texto) : null; } catch { /* respuesta sin JSON */ }
  if (!res.ok) throw new Error((datos && datos.mensaje) || 'Ocurrió un error inesperado');
  return datos;
}

const enviarJson = (metodo, cuerpo) => ({
  method: metodo,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(cuerpo)
});

// Devuelve el usuario en sesión o manda al login
async function requerirSesion() {
  try { return await api('/api/auth/me'); }
  catch { location.href = '/index.html'; return null; }
}

// Barra superior: usuario común (Explorar, Publicar) o administrador (Administración)
function pintarNavbar(usuario, activa) {
  const enlaces = usuario.rol === 'ADMIN'
    ? '<a class="activo" href="/src/views/explorar.html">Administración</a>'
    : `<a class="${activa === 'explorar' ? 'activo' : ''}" href="/src/views/explorar.html">Explorar</a>
       <a class="${activa === 'publicar' ? 'activo' : ''}" href="/src/views/publicar.html">Publicar</a>`;
  $('navbar').innerHTML = `
    <nav class="nav-links">${enlaces}</nav>
    <div class="nav-user">@${esc(usuario.nombreUsuario)} <button id="salir" class="btn-sec">Salir</button></div>`;
  $('salir').onclick = async () => {
    await api('/api/auth/logout', { method: 'POST' }).catch(() => {});
    location.href = '/index.html';
  };
}
