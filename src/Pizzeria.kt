import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

//-----------------------ANOTACIONES--------------------------------


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)

annotation class Promocionable(
    val descuento: Int = 5,
    val descripcion: String = "Promoción especial"
)

//-----------------------CLASES--------------------------------

class Ingrediente(val nombre: String, var stock: Int)

enum class Categoria { PIZZA, BEBIDA }

open class Producto(
    val nombre: String,
    val precio: Int,
    val ingredientes: List<Ingrediente>,
    var estado: String,
    val categoria: Categoria = Categoria.PIZZA
) {
    // Sobrecarga del operador ==: mismo producto si coinciden nombre y categoría
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto) return false
        return nombre == other.nombre && categoria == other.categoria
    }

    // Override HashCode
    override fun hashCode(): Int {
        return 31 * nombre.hashCode() + categoria.hashCode()
    }

    fun mostrarAtributos() {
        println("Nombre: $nombre")
        println("Precio: $precio")
        println("Ingredientes: ${ingredientes.map { it.nombre }}")
        println("Estado: $estado")
        println("Categoría: $categoria")
    }
}

// Pizzas elegibles para promoción (llevan la anotación)
@Promocionable(descuento = 5, descripcion = "Promo Napolitana: 5% de descuento")
class PizzaNapolitana(ingredientes: List<Ingrediente>) :
    Producto("Pizza Napolitana", 10000, ingredientes, "No Preparada")

@Promocionable(descuento = 10, descripcion = "Promo Cuatro Quesos: 10% de descuento")
class PizzaCuatroQuesos(ingredientes: List<Ingrediente>) :
    Producto("Pizza Cuatro Quesos", 12000, ingredientes, "No Preparada")

@Promocionable(descuento = 8, descripcion = "Promo Hawaiana: 8% de descuento")
class PizzaHawaiana(ingredientes: List<Ingrediente>) :
    Producto("Pizza Hawaiana", 8500, ingredientes, "No Preparada")

// Otras pizzas después se crean como Producto normal: No son promocionables.

class Cliente(val usuario: String, val email: String, val telefono: String) {

    // Desestructuración
    operator fun component1() = usuario
    operator fun component2() = email
    operator fun component3() = telefono

    // Override Equals
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return usuario == other.usuario && email == other.email
    }

    // Override HashCode
    override fun hashCode(): Int {
        return 31 * usuario.hashCode() + email.hashCode()
    }
}

class Pedido(val cliente: Cliente, val productos: MutableList<Producto>) {

    fun cantidadProductos(): Int = productos.size

    fun total(): Int = calculoTotal(productos)

    // Sobrecarga del operador +: combina dos pedidos en uno nuevo.
    operator fun plus(otro: Pedido): Pedido {
        val productosCombinados = (this.productos + otro.productos).toMutableList()
        return Pedido(this.cliente, productosCombinados)
    }
}

//--------------------MÉTODOS-----------------------

fun verificarFecha(): Boolean {
    val fechaInicio = LocalDate.of(2026, 11, 1)
    val fechaFin = LocalDate.of(2026, 11, 30)
    val rangoDeFechas = fechaInicio..fechaFin

    val hoy = LocalDate.now()

    return hoy in rangoDeFechas
}


// Devuelve la anotación @Promocionable del producto (o null si no la tiene)
fun obtenerPromocion(producto: Producto): Promocionable? {
    return producto::class.findAnnotation<Promocionable>()
}

fun esPromocionable(producto: Producto): Boolean = obtenerPromocion(producto) != null

fun calculoTotal(productos: List<Producto>): Int {
    var total = 0

    for (producto in productos) {
        val promo = obtenerPromocion(producto)

        if (promo != null) {
            // Producto elegible: se aplica su descuento promocional
            val descuento = producto.precio * promo.descuento / 100
            total += producto.precio - descuento
            println("Promoción aplicada a ${producto.nombre}: ${promo.descripcion} (-$$descuento)")
        } else {
            total += producto.precio
        }
    }

    //Descuento del 10% por pedir 3 o más pizzas
    if (productos.size >= 3) {
        total = total - total / 10
        println("Se aplicó un descuento del 10% por la compra de 3 o más pizzas.")
    }

    //Descuento especial del 15% por el mes de noviembre
    if (verificarFecha()) {
        total = total - total * 15 / 100
        println("Se aplicó un descuento del 15% por evento especial de Noviembre.")
    }

    return total
}

// Lista, usando reflection, qué productos del menú son promocionables
fun listarPromocionables(productos: List<Producto>) {
    println("Productos con promoción disponible:")
    val promocionables = productos.filter { esPromocionable(it) }

    if (promocionables.isEmpty()) {
        println("  (ninguno)")
        return
    }

    for (producto in promocionables) {
        val promo = obtenerPromocion(producto)!!
        println("  - ${producto.nombre}: ${promo.descuento}% -> ${promo.descripcion}")
    }
}

fun demostrarOperadores(pedidoActual: Pedido, menu: List<Producto>) {

    if (menu.size < 5) {
        println("El menú necesita al menos 5 productos para esta demostración.")
        return
    }
    else{

        println("")
        println("--- PRUEBA 1: Operador == en Producto (nombre y categoría) ---")

        val pizzaDelMenu = menu[0]
        val mismaPizza = Producto("Pizza Napolitana", 9999, emptyList(), "No Preparada", Categoria.PIZZA)
        val mismoNombreOtraCategoria = Producto("Pizza Napolitana", 10000, emptyList(), "No Preparada", Categoria.BEBIDA)

        println("Se probará con dos productos:")
        println("Producto 1:")
        mismaPizza.mostrarAtributos()
        println("----------")
        println("Producto 2:")
        mismoNombreOtraCategoria.mostrarAtributos()
        println("----------")

        println("")
        println("Resultado:")
        println("Mismo nombre y categoría (aunque cambie el precio): ${pizzaDelMenu == mismaPizza}")
        println("Mismo nombre pero distinta categoría: ${pizzaDelMenu == mismoNombreOtraCategoria}")

        //////////////////////

        println("")

        println("--- PRUEBA 2: Operador + en Pedido ---")

        val pedidoExtra = Pedido(pedidoActual.cliente, mutableListOf(menu[3], menu[4]))
        val combinado = pedidoActual + pedidoExtra

        println("Se probará con dos productos:")
        println("Producto 1:")
        menu[3].mostrarAtributos()
        println("----------")
        println("Producto 2:")
        menu[4].mostrarAtributos()
        println("----------")

        println("")
        println("Resultado:")
        println("Productos en su pedido actual: ${pedidoActual.cantidadProductos()}")
        println("Productos en el pedido extra: ${pedidoExtra.cantidadProductos()}")
        println("Productos en el pedido combinado: ${combinado.cantidadProductos()}")
        println("Total del pedido combinado: $${combinado.total()}")

    }

}


fun mostrarDatosCliente(cliente: Cliente) {

    val (usuario, email, telefono) = cliente

    println("Nombre: $usuario")
    println("E-mail: $email")
    println("Teléfono: $telefono")
}

//------------------MAIN-------------------------

fun main() {

    //SETEO DE VARIABLES Y STOCK

    //Ingredientes
    val ingredienteMozzarella = Ingrediente("Mozzarella", 20)
    val ingredienteTomate = Ingrediente("Tomate", 50)
    val ingredienteSalsaTomate = Ingrediente("Salsa de Tomate", 100)
    val ingredienteAceituna = Ingrediente("Aceituna", 40)
    val ingredienteJamon = Ingrediente("Jamon", 30)
    val ingredienteGorgonzola = Ingrediente("Gorgonzola", 30)
    val ingredienteParmesano = Ingrediente("Parmesano", 45)
    val ingredienteFontina = Ingrediente("Fontina", 20)
    val ingredientePina = Ingrediente("Piña", 30)
    val ingredienteSeta = Ingrediente("Seta Portobello", 50)
    val ingredienteAlbahaca = Ingrediente("Albahaca", 200)
    val ingredienteAceite = Ingrediente("Aceite de Oliva", 1000)

    //Pizzas
    val ingredientesPizza1 = listOf(ingredienteMozzarella, ingredienteTomate, ingredienteAceituna, ingredienteJamon)
    val pizza1 = PizzaNapolitana(ingredientesPizza1)

    val ingredientesPizza2 = listOf(ingredienteMozzarella, ingredienteGorgonzola, ingredienteParmesano, ingredienteFontina)
    val pizza2 = PizzaCuatroQuesos(ingredientesPizza2)

    val ingredientesPizza3 = listOf(ingredienteMozzarella, ingredienteTomate, ingredienteJamon, ingredientePina)
    val pizza3 = PizzaHawaiana(ingredientesPizza3)

    val ingredientesPizza4 = listOf(ingredienteMozzarella, ingredienteSalsaTomate, ingredienteSeta)
    val pizza4 = Producto("Pizza Funghi", 9000, ingredientesPizza4, "No Preparada")

    val ingredientesPizza5 = listOf(ingredienteMozzarella, ingredienteTomate, ingredienteAlbahaca, ingredienteAceite)
    val pizza5 = Producto("Pizza Margarita", 8000, ingredientesPizza5, "No Preparada")

    val listaDePizzas: List<Producto> = listOf(pizza1, pizza2, pizza3, pizza4, pizza5)
    val listaDePizzasPedidas = mutableListOf<Producto>()

    //Regex para formatos:
    val regexSoloLetras = Regex("^[a-zA-Z]+$")
    val regexEmail = Regex("^[a-zA-Z0-9.\\-]+@[a-zA-Z]+\\.[a-zA-Z]+$")
    val regexTelefono = Regex("^\\+569\\d{8}$")

    //Variables con datos de usuario:
    var usuario: String
    var email: String
    var telefono: String

    println("Bienvenido.")

    do {
        println("Por favor ingrese su nombre (solo letras, no símbolos):")
        usuario = readln().trim()

        val usuarioValido = regexSoloLetras.matches(usuario)

        if (!usuarioValido) {
            println("Nombre inválido. Use solo letras (sin espacios, números ni símbolos).")
        }

    } while (!usuarioValido)

    do {
        println("Por favor ingrese su e-mail:")
        email = readln().trim()

        val emailValido = regexEmail.matches(email)

        if (!emailValido) {
            println("E-mail inválido. El texto ingresado no coincide con el formato estándar.")
        }
    } while (!emailValido)

    do {
        println("Por favor ingrese su número de teléfono (formato chileno: +569xxxxxxxx):")
        telefono = readln().trim()

        val telefonoValido = regexTelefono.matches(telefono)

        if (!telefonoValido) {
            println("Teléfono inválido: debe comenzar con +569 y continuar con 8 dígitos")
        }
    } while (!telefonoValido)

    println("Bienvenido, $usuario.")

    val cliente = Cliente(usuario, email, telefono)
    val pedido = Pedido(cliente, listaDePizzasPedidas)

    println("")

    //Variables de input y numéricas:
    var inputGlobal: Int
    var inputPizza: Int
    var finPrograma = false
    var finSeleccionarPizza: Boolean

    val rangoDePedidos = 1..100

    do {
        println("")
        println("Digite 1: Agregar una pizza al pedido. 2: Ver promociones. 3. Demostrar operadores (+ y ==). 4: Finalizar el programa. ")

        inputGlobal = try {
            readln().trim().toInt()
        } catch (e: NumberFormatException) {
            println("Error: debes ingresar un número entero válido.")
            0
        }

        if (inputGlobal == 2) {

            println("")
            println("Contamos con un descuento especial del 15% durante todo Noviembre!")
            println("También contamos con un descuento del 10% en el total de la compra si lleva 3 o más productos.")
            listarPromocionables(listaDePizzas)

        }

        else if(inputGlobal == 3){
            demostrarOperadores(pedido, listaDePizzas)
        }

        else if (inputGlobal == 4) {
            finPrograma = true

            println("")
            println("Gracias por comprar en nuestra tienda.")

            println("Estos son sus datos:")
            mostrarDatosCliente(cliente)
            println("")

            if (pedido.productos.isNotEmpty()) {

                println("")
                println("A continuación: Se muestra el detalle de su compra:")
                println("")

                for ((indice, pizza) in pedido.productos.withIndex()) {
                    println("Pizza número: " + (indice + 1))
                    println("Nombre: " + pizza.nombre)
                    println("Precio: " + pizza.precio)
                    println("Estado: " + pizza.estado)
                    println("Promocionable: " + if (esPromocionable(pizza)) "Sí" else "No")
                    println("-----")
                }

                println("Total a pagar: $${pedido.total()}")
                println("Gracias por su visita.")
            } else {
                println("Ningún pedido registrado.")
                println("Vuelva pronto.")
            }
        }

        else if (inputGlobal == 1) {

            println("")
            println("Lista de pizzas disponibles en nuestro local:")
            println("")

            for ((indice, pizza) in listaDePizzas.withIndex()) {
                println("Pizza número: " + (indice + 1))
                println("Nombre: " + pizza.nombre)
                println("Precio: " + pizza.precio)
                println("Ingredientes: " + pizza.ingredientes.map { it.nombre })
                obtenerPromocion(pizza)?.let { println("¡PROMO! ${it.descripcion}") }
                println("-------------------")
            }

            finSeleccionarPizza = false

            do {
                println("")
                println("Digite un número del 1 al " + listaDePizzas.size + " para agregar su pizza.")

                inputPizza = try {
                    readln().trim().toInt()
                } catch (e: NumberFormatException) {
                    println("Error: debes ingresar un número entero válido.")
                    0
                }

                if (inputPizza in 1..listaDePizzas.size) {

                    val pizzaElegida = listaDePizzas[inputPizza - 1]

                    println("Verificando stock...")
                    // Todos los ingredientes deben tener stock disponible (> 0)
                    if (pizzaElegida.ingredientes.all { it.stock > 0 }) {

                        pizzaElegida.ingredientes.forEach { it.stock-- }

                        println("Stock de ingredientes ok.")
                        println("Verificando que el tamaño del carrito de compras no sea excesivamente grande...")
                        if (listaDePizzasPedidas.size in rangoDePedidos || listaDePizzasPedidas.isEmpty()) {
                            println("Pizza agregada exitosamente: " + pizzaElegida.nombre)


                            obtenerPromocion(pizzaElegida)?.let {
                                println("Esta pizza califica para: ${it.descripcion}")
                            }

                            listaDePizzasPedidas.add(pizzaElegida)
                            pizzaElegida.estado = "En Preparación"
                        } else {
                            println("No se ha podido agregar el pedido, porque su lista de pedidos es demasiado grande (+100).")
                        }

                    } else {
                        println("La pizza escogida no se encuentra disponible por falta de stock.")
                    }

                    finSeleccionarPizza = true

                } else {
                    println("Número ingresado inválido. Fuera de rango.")
                }

            } while (!finSeleccionarPizza)

        } else {
            println("Input inválido.")
        }

    } while (!finPrograma)

}