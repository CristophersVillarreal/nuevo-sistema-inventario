// app.js — Lógica del frontend. Consume la API REST con fetch.

const API = "/api/productos";
let modalProducto;
let productosCache = [];

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

// Trae los productos y refresca todas las vistas
async function cargarTodo() {
    try {
        const resp = await fetch(API);
        productosCache = await resp.json();
        renderTablaProductos();
        renderPanel();
        renderAlertas();
    } catch (err) {
        alert("No se pudo conectar con el servidor. ¿Está corriendo el backend?");
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
            <td>${p.nombre}</td>
            <td>${p.categoria}</td>
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

function abrirNuevo() {
    document.getElementById("form-producto").reset();
    document.getElementById("prod-id").value = "";
    document.getElementById("modal-titulo").textContent = "Nuevo producto";
    modalProducto.show();
}

function editar(id) {
    const p = productosCache.find(x => x.id === id);
    if (!p) return;
    document.getElementById("prod-id").value = p.id;
    document.getElementById("prod-nombre").value = p.nombre;
    document.getElementById("prod-categoria").value = p.categoria;
    document.getElementById("prod-precio").value = p.precio;
    document.getElementById("prod-stock").value = p.stock;
    document.getElementById("prod-stockminimo").value = p.stockMinimo;
    document.getElementById("modal-titulo").textContent = "Editar producto";
    modalProducto.show();
}

async function guardarProducto(e) {
    e.preventDefault();
    const id = document.getElementById("prod-id").value;
    const producto = {
        nombre: document.getElementById("prod-nombre").value,
        categoria: document.getElementById("prod-categoria").value,
        precio: parseFloat(document.getElementById("prod-precio").value),
        stock: parseInt(document.getElementById("prod-stock").value),
        stockMinimo: parseInt(document.getElementById("prod-stockminimo").value)
    };

    const url = id ? `${API}/${id}` : API;
    const metodo = id ? "PUT" : "POST";

    await fetch(url, {
        method: metodo,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(producto)
    });

    modalProducto.hide();
    cargarTodo();
}

async function eliminar(id) {
    if (!confirm("¿Eliminar este producto?")) return;
    await fetch(`${API}/${id}`, { method: "DELETE" });
    cargarTodo();
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
                <strong>${p.nombre}</strong> — quedan ${p.stock} (mínimo ${p.stockMinimo})
            </div>`).join("");
    }
}

// --- ALERTAS ---
async function renderAlertas() {
    const resp = await fetch(`${API}/stock-bajo`);
    const bajos = await resp.json();
    const tbody = document.getElementById("tabla-alertas");
    if (bajos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-success">Sin alertas.</td></tr>';
        return;
    }
    tbody.innerHTML = bajos.map(p => `
        <tr class="fila-stock-bajo">
            <td>${p.nombre}</td>
            <td>${p.categoria}</td>
            <td class="text-end">${p.stock}</td>
            <td class="text-end">${p.stockMinimo}</td>
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
