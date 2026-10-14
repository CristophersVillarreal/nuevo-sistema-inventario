// app.js — Lógica del frontend. Consume la API REST con fetch.
// Avance 2: la API está protegida con HTTP Basic. El navegador pide usuario y
// contraseña al abrir la página y los reenvía automáticamente en cada fetch.

const API = "/api/productos";
const API_CATEGORIAS = "/api/categorias";
let modalProducto;
let productosCache = [];
let categoriasCache = [];

document.addEventListener("DOMContentLoaded", () => {
    modalProducto = new bootstrap.Modal(document.getElementById("modal-producto"));

    // Navegacion del menu lateral
    document.querySelectorAll(".sidebar .nav-link").forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            cambiarVista(link.dataset.vista, link);
        });
    });

    // Boton nuevo producto
    document.getElementById("btn-nuevo").addEventListener("click", abrirNuevo);
    // Guardar producto (crear o editar)
    document.getElementById("form-producto").addEventListener("submit", guardarProducto);
    // Chat
    document.getElementById("chat-form").addEventListener("submit", enviarChat);

    // Carga inicial
    cargarTodo();
});

// Cambia entre vistas ocultando/mostrando secciones
function cambiarVista(vista, link) {
    document.querySelectorAll("main section").forEach(s => s.hidden = true);
    document.getElementById("vista-" + vista).hidden = false;
    document.querySelectorAll(".sidebar .nav-link").forEach(l => l.classList.remove("activo"));
    link.classList.add("activo");
}

// Llama a la API y, si responde con error (400, 401, 403, 404, 409),
// lanza un Error con el mensaje que envió el backend.
async function pedir(url, opciones = {}) {
    // URL absoluta desde location.origin (nunca incluye usuario:contraseña).
    const resp = await fetch(new URL(url, window.location.origin), opciones);
    if (!resp.ok) {
        let mensaje = `Error ${resp.status}`;
        try {
            const cuerpo = await resp.json();
            mensaje = cuerpo.mensaje || mensaje;
            if (cuerpo.detalles) {
                mensaje += ": " + Object.values(cuerpo.detalles).join(", ");
            }
        } catch (e) { /* respuesta sin JSON */ }
        throw new Error(mensaje);
    }
    return resp.status === 204 ? null : resp.json();
}

// Evita que un texto guardado en la BD se interprete como HTML.
function esc(texto) {
    const div = document.createElement("div");
    div.textContent = texto ?? "";
    return div.innerHTML;
}

// Fecha ISO (2026-11-08) -> 08/11/2026
function formatoFecha(iso) {
    if (!iso) return "—";
    const [a, m, d] = iso.split("-");
    return `${d}/${m}/${a}`;
}

// Trae los productos y categorias y refresca todas las vistas
async function cargarTodo() {
    try {
        [productosCache, categoriasCache] = await Promise.all([pedir(API), pedir(API_CATEGORIAS)]);
        renderTablaProductos();
        renderPanel();
        renderAlertas();
    } catch (err) {
        alert("No se pudo cargar el inventario: " + err.message);
    }
}

// --- PRODUCTOS ---
function renderTablaProductos() {
    const tbody = document.getElementById("tabla-productos");
    tbody.innerHTML = "";
    productosCache.forEach(p => {
        const bajo = p.stock <= p.stockMinimo;
        const tr = document.createElement("tr");
        if (bajo) tr.classList.add("fila-stock-bajo");
        tr.innerHTML = `
            <td>${p.id}</td>
            <td>${esc(p.nombre)}</td>
            <td>${esc(p.categoria?.nombre)}</td>
            <td>${esc(p.ubicacion) || "—"}</td>
            <td>${formatoFecha(p.fechaVencimiento)}</td>
            <td class="text-end">S/ ${p.precio.toFixed(2)}</td>
            <td class="text-end">${p.stock}</td>
            <td class="text-end">${p.stockMinimo}</td>
            <td class="text-center">
                ${bajo
                    ? '<span class="badge bg-danger">Stock bajo</span>'
                    : '<span class="badge bg-success">OK</span>'}
            </td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-secondary" onclick="editar(${p.id})">Editar</button>
                <button class="btn btn-sm btn-outline-danger" onclick="eliminar(${p.id})">Eliminar</button>
            </td>`;
        tbody.appendChild(tr);
    });
}

// Llena el selector de categorias del formulario
function llenarCategorias(seleccionada) {
    const select = document.getElementById("prod-categoria");
    select.innerHTML = '<option value="">Seleccione…</option>' + categoriasCache
        .map(c => `<option value="${c.id}">${esc(c.nombre)}</option>`).join("");
    select.value = seleccionada ?? "";
}

function mostrarErrorFormulario(mensaje) {
    const caja = document.getElementById("prod-error");
    caja.textContent = mensaje || "";
    caja.hidden = !mensaje;
}

function abrirNuevo() {
    document.getElementById("form-producto").reset();
    document.getElementById("prod-id").value = "";
    llenarCategorias();
    mostrarErrorFormulario(null);
    document.getElementById("modal-titulo").textContent = "Nuevo producto";
    modalProducto.show();
}

function editar(id) {
    const p = productosCache.find(x => x.id === id);
    if (!p) return;
    document.getElementById("prod-id").value = p.id;
    document.getElementById("prod-nombre").value = p.nombre;
    llenarCategorias(p.categoria?.id);
    document.getElementById("prod-ubicacion").value = p.ubicacion ?? "";
    document.getElementById("prod-vencimiento").value = p.fechaVencimiento ?? "";
    document.getElementById("prod-precio").value = p.precio;
    document.getElementById("prod-stock").value = p.stock;
    document.getElementById("prod-stockminimo").value = p.stockMinimo;
    mostrarErrorFormulario(null);
    document.getElementById("modal-titulo").textContent = "Editar producto";
    modalProducto.show();
}

async function guardarProducto(e) {
    e.preventDefault();
    const id = document.getElementById("prod-id").value;
    const producto = {
        nombre: document.getElementById("prod-nombre").value,
        categoria: { id: parseInt(document.getElementById("prod-categoria").value) },
        ubicacion: document.getElementById("prod-ubicacion").value || null,
        fechaVencimiento: document.getElementById("prod-vencimiento").value || null,
        precio: parseFloat(document.getElementById("prod-precio").value),
        stock: parseInt(document.getElementById("prod-stock").value),
        stockMinimo: parseInt(document.getElementById("prod-stockminimo").value)
    };

    const url = id ? `${API}/${id}` : API;
    const metodo = id ? "PUT" : "POST";

    try {
        await pedir(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(producto)
        });
        modalProducto.hide();
        cargarTodo();
    } catch (err) {
        // Ej.: 403 si el usuario es OPERADOR, 400 si faltan datos.
        mostrarErrorFormulario(err.message);
    }
}

async function eliminar(id) {
    if (!confirm("¿Eliminar este producto?")) return;
    try {
        await pedir(`${API}/${id}`, { method: "DELETE" });
        cargarTodo();
    } catch (err) {
        // Ej.: 403 si el usuario es OPERADOR, 409 si el producto tiene movimientos.
        alert("No se pudo eliminar: " + err.message);
    }
}

// --- PANEL ---
function renderPanel() {
    const total = productosCache.length;
    const unidades = productosCache.reduce((s, p) => s + p.stock, 0);
    const valor = productosCache.reduce((s, p) => s + p.precio * p.stock, 0);
    const bajos = productosCache.filter(p => p.stock <= p.stockMinimo);

    document.getElementById("res-total").textContent = total;
    document.getElementById("res-unidades").textContent = unidades;
    document.getElementById("res-valor").textContent = "S/ " + valor.toFixed(2);
    document.getElementById("res-alertas").textContent = bajos.length;

    const cont = document.getElementById("panel-alertas");
    if (bajos.length === 0) {
        cont.innerHTML = '<p class="text-success mb-0">✔ Todos los productos tienen stock suficiente.</p>';
    } else {
        cont.innerHTML = bajos.map(p =>
            `<div class="alert alert-warning py-2 mb-2">
                <strong>${esc(p.nombre)}</strong> — quedan ${p.stock} (mínimo ${p.stockMinimo})
            </div>`).join("");
    }
}

// --- ALERTAS (consultas JPQL del backend) ---
async function renderAlertas() {
    const [bajos, porVencer] = await Promise.all([
        pedir(`${API}/stock-bajo`),
        pedir(`${API}/por-vencer?dias=30`)
    ]);

    const tbody = document.getElementById("tabla-alertas");
    tbody.innerHTML = bajos.length === 0
        ? '<tr><td colspan="4" class="text-success">Sin alertas.</td></tr>'
        : bajos.map(p => `
            <tr class="fila-stock-bajo">
                <td>${esc(p.nombre)}</td>
                <td>${esc(p.categoria?.nombre)}</td>
                <td class="text-end">${p.stock}</td>
                <td class="text-end">${p.stockMinimo}</td>
            </tr>`).join("");

    const tbodyVencer = document.getElementById("tabla-vencer");
    tbodyVencer.innerHTML = porVencer.length === 0
        ? '<tr><td colspan="4" class="text-success">Ningún producto vence en los próximos 30 días.</td></tr>'
        : porVencer.map(p => `
            <tr>
                <td>${esc(p.nombre)}</td>
                <td>${esc(p.ubicacion) || "—"}</td>
                <td>${formatoFecha(p.fechaVencimiento)}</td>
                <td class="text-end">${p.stock}</td>
            </tr>`).join("");
}

// --- ASISTENTE (vista previa local, sin IA real) ---
function enviarChat(e) {
    e.preventDefault();
    const input = document.getElementById("chat-input");
    const pregunta = input.value.trim();
    if (!pregunta) return;
    agregarBurbuja(pregunta, "usuario");
    input.value = "";
    const respuesta = responderLocal(pregunta.toLowerCase());
    setTimeout(() => agregarBurbuja(respuesta, "bot"), 300);
}

function agregarBurbuja(texto, quien) {
    const cont = document.getElementById("chat-mensajes");
    const div = document.createElement("div");
    div.className = "chat-burbuja " + (quien === "usuario" ? "chat-usuario" : "chat-bot");
    div.textContent = texto;
    cont.appendChild(div);
    cont.scrollTop = cont.scrollHeight;
}

// Respuestas simuladas usando los datos ya cargados
function responderLocal(q) {
    if (q.includes("stock bajo") || q.includes("alerta")) {
        const bajos = productosCache.filter(p => p.stock <= p.stockMinimo);
        if (bajos.length === 0) return "No hay productos con stock bajo.";
        return "Productos con stock bajo: " + bajos.map(p => `${p.nombre} (${p.stock})`).join(", ") + ".";
    }
    if (q.includes("cuántos productos") || q.includes("cuantos productos") || q.includes("total")) {
        return `Tienes ${productosCache.length} productos registrados.`;
    }
    // Busqueda por nombre
    const encontrado = productosCache.find(p => q.includes(p.nombre.toLowerCase().split(" ")[0]));
    if (encontrado) {
        return `Quedan ${encontrado.stock} unidades de ${encontrado.nombre}.`;
    }
    return "Aún soy una vista previa local. Prueba: \"stock bajo\", \"cuántos productos\" o el nombre de un producto.";
}
