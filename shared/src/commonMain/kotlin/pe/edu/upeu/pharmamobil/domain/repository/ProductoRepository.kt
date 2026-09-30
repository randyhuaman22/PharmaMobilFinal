package pe.edu.upeu.pharmamobil.domain.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {

    /** Incorpora el producto al inventario y devuelve el producto ya identificado. */
    suspend fun registrar(producto: Producto): Producto

    /** Entrega el inventario completo en el orden en que fue registrado. */
    suspend fun listar(): List<Producto>

    /** Busca un producto por su identificador. */
    suspend fun obtenerPorId(id: Long): Producto

    /** Actualiza el producto indicado por su identificador. */
    suspend fun actualizar(producto: Producto): Producto

    /** Elimina el producto indicado por su identificador. */
    suspend fun eliminar(id: Long)
}
