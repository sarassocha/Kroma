/**
 * Cliente JS KromaStore: Gestiona carrito interactivo, swatches de tonos,
 * modal de detalle de producto y notificaciones visuales.
 */
const KromaStore = (() => {
    const seleccionesTono = {};
    let modalTonoActual = null;

    function formatearCOP(valor) {
        return new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            maximumFractionDigits: 0
        }).format(Number(valor || 0));
    }

    function mostrarToast(texto) {
        const toast = document.getElementById('kromaToast');
        if (!toast) return;
        toast.textContent = texto;
        toast.classList.remove('hidden');
        setTimeout(() => toast.classList.add('hidden'), 2800);
    }

    function seleccionarTonoTarjeta(productoId, nombreTono, hex, btnEl) {
        seleccionesTono[productoId] = { nombreTono, hex };
        const card = btnEl.closest('.product-card');
        if (!card) return;
        card.querySelectorAll('.swatch-dot').forEach((d) => d.classList.remove('active'));
        btnEl.classList.add('active');
        const label = card.querySelector('.tono-activo-label');
        if (label) label.textContent = nombreTono;
    }

    async function agregarAlCarrito(productoId, tonoFijo = null, hexFijo = null) {
        const sel = seleccionesTono[productoId] || {};
        const tonoSeleccionado = tonoFijo || sel.nombreTono || null;
        const hexTono = hexFijo || sel.hex || null;

        const resp = await fetch('/api/carrito/agregar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productoId, tonoSeleccionado, hexTono, cantidad: 1 })
        });
        const data = await resp.json();
        if (!data.ok) {
            mostrarToast('⚠️ ' + data.mensaje);
            return;
        }
        actualizarBadge(data.cantidadTotal);
        renderizarCarrito(data);
        mostrarToast('🛍️ Agregado a tu bolsa Kroma');
    }

    function actualizarBadge(cantidad) {
        const badge = document.getElementById('badgeCarrito');
        if (badge) badge.textContent = cantidad;
    }

    async function cargarCarrito() {
        const resp = await fetch('/api/carrito');
        const data = await resp.json();
        if (data.ok) {
            actualizarBadge(data.cantidadTotal);
            renderizarCarrito(data);
        }
    }

    async function cambiarCantidad(productoId, tonoSeleccionado, nuevaCantidad) {
        const resp = await fetch('/api/carrito/actualizar', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productoId, tonoSeleccionado, cantidad: nuevaCantidad })
        });
        const data = await resp.json();
        if (data.ok) {
            actualizarBadge(data.cantidadTotal);
            renderizarCarrito(data);
        } else {
            mostrarToast('⚠️ ' + data.mensaje);
        }
    }

    async function eliminarItem(productoId, tonoSeleccionado) {
        const resp = await fetch('/api/carrito/eliminar', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productoId, tonoSeleccionado })
        });
        const data = await resp.json();
        if (data.ok) {
            actualizarBadge(data.cantidadTotal);
            renderizarCarrito(data);
        }
    }

    function renderizarCarrito(data) {
        const lista = document.getElementById('carritoLista');
        const subEl = document.getElementById('carritoSubtotal');
        const msgEnvio = document.getElementById('carritoMensajeEnvio');
        if (!lista || !subEl) return;

        subEl.textContent = formatearCOP(data.subtotal);
        const faltante = Math.max(0, 120000 - Number(data.subtotal || 0));
        if (msgEnvio) {
            msgEnvio.textContent = faltante === 0 && data.subtotal > 0
                ? '🌱 ¡Tienes Envío Ecológico GRATIS a todo el país!'
                : `Agrega ${formatearCOP(faltante)} más para obtener envío biodegradable gratis.`;
        }

        if (!data.items || data.items.length === 0) {
            lista.innerHTML = `
                <div class="text-center py-12 space-y-3">
                    <div class="text-4xl">🛍️</div>
                    <p class="font-bold text-gray-700">Tu bolsa Kroma está vacía</p>
                    <p class="text-xs text-gray-500">Explora nuestros productos skin-first o prueba el Tone Matcher.</p>
                </div>`;
            return;
        }

        lista.innerHTML = data.items.map((item) => `
            <div class="flex gap-3 p-3 rounded-2xl border border-pink-100 bg-white">
                <img src="${item.imagenUrl}" alt="${item.nombreProducto}" class="w-16 h-16 rounded-xl object-cover bg-pink-50 shrink-0">
                <div class="flex-1 min-w-0">
                    <p class="font-bold text-xs text-kromaDark truncate">${item.nombreProducto}</p>
                    <div class="flex items-center gap-1.5 mt-1">
                        <span class="w-3 h-3 rounded-full border border-gray-300 inline-block" style="background:${item.hexTono}"></span>
                        <span class="text-[11px] text-gray-500">${item.tonoSeleccionado}</span>
                    </div>
                    <div class="flex items-center justify-between mt-2">
                        <div class="inline-flex items-center border border-pink-200 rounded-full text-xs">
                            <button type="button" onclick="KromaStore.cambiarCantidad('${item.productoId}', '${item.tonoSeleccionado.replace(/'/g, "\\'")}', ${item.cantidad - 1})" class="px-2.5 py-0.5 hover:text-kromaPink font-bold">-</button>
                            <span class="px-2 font-semibold">${item.cantidad}</span>
                            <button type="button" onclick="KromaStore.cambiarCantidad('${item.productoId}', '${item.tonoSeleccionado.replace(/'/g, "\\'")}', ${item.cantidad + 1})" class="px-2.5 py-0.5 hover:text-kromaPink font-bold">+</button>
                        </div>
                        <span class="font-extrabold text-xs text-kromaPink">${formatearCOP(item.precioUnitario * item.cantidad)}</span>
                    </div>
                </div>
                <button type="button" onclick="KromaStore.eliminarItem('${item.productoId}', '${item.tonoSeleccionado.replace(/'/g, "\\'")}')" class="text-gray-400 hover:text-red-500 text-xs self-start">✕</button>
            </div>
        `).join('');
    }

    function abrirCarrito() {
        cargarCarrito();
        document.getElementById('carritoOverlay')?.classList.remove('hidden');
    }

    function cerrarCarrito() {
        document.getElementById('carritoOverlay')?.classList.add('hidden');
    }

    async function abrirDetalle(productoId) {
        const resp = await fetch(`/api/productos/${productoId}`);
        const data = await resp.json();
        if (!data.ok) return;

        const p = data.producto;
        const primero = p.paletaTonos?.[0] || { nombreTono: 'Universal', hex: '#E8B4A0' };
        modalTonoActual = seleccionesTono[p.id] || primero;

        document.getElementById('modalProdImg').src = p.imagenUrl;
        document.getElementById('modalProdCat').textContent = `${p.categoria} • ${p.linea}`;
        document.getElementById('modalProdNombre').textContent = p.nombre;
        document.getElementById('modalProdPrecio').textContent = formatearCOP(p.precio);
        document.getElementById('modalProdDesc').textContent = p.descripcionCorta;
        document.getElementById('modalProdIngredientes').textContent = p.descripcionIngredientes;
        document.getElementById('modalProdTonoNombre').textContent = modalTonoActual.nombreTono;

        const swatchesContainer = document.getElementById('modalProdSwatches');
        swatchesContainer.innerHTML = (p.paletaTonos || []).map((t) => `
            <button type="button"
                onclick="KromaStore.seleccionarTonoModal('${t.nombreTono.replace(/'/g, "\\'")}', '${t.hex}')"
                class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs ${modalTonoActual.nombreTono === t.nombreTono ? 'border-kromaPink bg-pink-50 font-bold' : 'border-gray-200'}">
                <span class="w-3.5 h-3.5 rounded-full border border-gray-300" style="background:${t.hex}"></span>
                <span>${t.nombreTono}</span>
            </button>
        `).join('');

        document.getElementById('modalProdBtnAgregar').onclick = () => {
            agregarAlCarrito(p.id, modalTonoActual.nombreTono, modalTonoActual.hex);
            cerrarDetalle();
        };

        const modal = document.getElementById('modalProducto');
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function seleccionarTonoModal(nombreTono, hex) {
        modalTonoActual = { nombreTono, hex };
        document.getElementById('modalProdTonoNombre').textContent = nombreTono;
    }

    function cerrarDetalle() {
        const modal = document.getElementById('modalProducto');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    return {
        seleccionarTonoTarjeta,
        agregarAlCarrito,
        cambiarCantidad,
        eliminarItem,
        abrirCarrito,
        cerrarCarrito,
        abrirDetalle,
        seleccionarTonoModal,
        cerrarDetalle,
        mostrarToast,
        formatearCOP
    };
})();
