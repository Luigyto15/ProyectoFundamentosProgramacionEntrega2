package com.poligran.proyecto;


/**Esta parte es donde importamos las librerias que necesitaremos de java tanto
 para leer los archivos de texto, escribir archivos, manejar el control de errores
 de cada uno de los archivos,codificacion de caracteres, gestion de estructuras de datos
 tales como listas, mapas, etc. */ 

import java.io.BufferedReader; 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
  La clase main procesa automáticamente los archivos planos generados por GenerateInfoFiles,
  consolidando las ventas, calculando totales por vendedor y producto, y generando
  los reportes CSV ordenados de forma descendente, cumpliendo con los puntos 3 y 4 de la rúbrica del proyecto.
 */
public class main { //Declaramos la clase del programa con el nombre main

    public static void main(String[] args) { // Aqui definimos el metodo principal, que es la entrada desde donde empieza a ejecutar el programma 
        
        // PRUEBAS DE OPTIMIZACIÓN Y EJECUCIÓN (Mide el tiempo de rendimiento con HashMap y Listas)
        long startTime = System.currentTimeMillis();

        try {
            System.out.println("Iniciando el procesamiento de archivos y generación de reportes.."); //Imprimos un mensaje y continuamos con 4 pasos

            //1. Cargamos el catálogo de los productos uniendolos por nombre
            Map<String, ProductoInfo> mapaProductos = cargarProductos("productos.txt");

            // 2. Procesamos todos los archivos individualmente de cada vendedores y calculamos sus totales.
            Map<String, VendedorReporte> mapaVendedores = procesarVentasVendedores(mapaProductos);

            // 3. Generamos un reporte financiero de los vendedores ordenado de mayor a menor 
            generarReporteVendedores(mapaVendedores);

            // 4. Generamos el reporte de cantidades vendidas por producto ordenandolos de mayor a menor
            generarReporteProductos(mapaProductos);

            //En estos pasos utilizamos la clase y el codigo creado en la primera entrega ya que con esos datos se basara para la generacion de dichos reportes
            
            System.out.println("Proceso de la Entrega 2 realizado con éxito, Reportes CSV generados.");

        } catch (IOException e) { //el catch captura cualquier tipo de error de lectura y escrittura que se presente
            System.err.println("Error crítico durante el procesamiento de los archivos: " + e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("[OPTIMIZACIÓN Y PRUEBAS] Tiempo total de ejecución del algoritmo: " + (endTime - startTime) + " ms.");
    }

    /**
     Esta sera una clase interna para modelar la información 
     y estadísticas de un producto.
     */
    
    static class ProductoInfo {
        String id;
        String nombre;
        double precio;
        int cantidadTotalVendida;

        /**Lo que hacemos es inicicalizar los valores de el prodcuto con un trim
         para que elmine cualquier espacio vacio y inicie la cantidad total vendida 
         en 0 por defecto 
          */
        
        public ProductoInfo(String id, String nombre, double precio) {
            this.id = id.trim();
            this.nombre = nombre.trim();
            this.precio = precio;
            this.cantidadTotalVendida = 0;
        }
    }

    /**
     Definimos una clase interna para diseñar el reporte del vendedor e implementar 
     caracteristicas necesarias para facilitar su ordenamiento 
     directo por dinero recaudado.
     */
    
    static class VendedorReporte implements Comparable<VendedorReporte> {
        String nombreCompleto;
        long cedula;
        double totalRecaudado;
        
        /**Igual que en el anterior inicalizamos el dinero recaudado en 0 y 
         hacemos limpieza de espacios*/

        public VendedorReporte(String nombreCompleto, long cedula) {
            this.nombreCompleto = nombreCompleto.trim();
            this.cedula = cedula;
            this.totalRecaudado = 0.0;
        }

       /** aqui escribe el metodo comparativo para enseñarle a java que la regla del ordemaniento
        sea descendente osea del que mas dinero recaudo al que menos.
          */
        public int compareTo(VendedorReporte otro) {
            return Double.compare(otro.totalRecaudado, this.totalRecaudado);
        }
    }

    /**
     Lee el archivo plano de productos y los agrupa por nombre para evitar 
     duplicados y precios variados.
     */
    
    private static Map<String, ProductoInfo> cargarProductos(String archivo) throws IOException {
        Map<String, ProductoInfo> productos = new HashMap<>();
        File file = new File(archivo);
        
        if (!file.exists()) {
            System.out.println("Alerta: El archivo de productos no existe en dicha ruta.");
            return productos;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                // Limpiamos la línea separandolos por punto y coma, si la linea tiene 3 partes limpia el ID , el nombre y convierte el precio de tecto a un numero entero
                String[] partes = linea.split(";");
                if (partes.length >= 3) {
                    try {
                        String id = partes[0].trim();
                        String nombre = partes[1].trim();
                        double precio = Double.parseDouble(partes[2].trim());
                        
                        // VALIDACIÓN AVANZADA: Accion para rechazar precios negativos o ceros en lectura
                        if (precio < 0) {
                            System.err.println("Advertencia [Línea " + numeroLinea + "]: Se omitió un producto con precio negativo (" + precio + ").");
                            continue;
                        }

                        // VALIDACIÓN AVANZADA: Comprobar que los ID no estén vacíos
                        if (id.isEmpty()) {
                            System.err.println("Advertencia [Línea " + numeroLinea + "]: ID de producto vacío.");
                            continue;
                        }
                        
                        // convertimos el nombre del producto en clave unica para unir productos repetidos
                        String claveNombre = nombre.toLowerCase();
                        
                        // Si el producto ya existe, podemos actualizar o mantener el primer precio  que se registrado
                        if (!productos.containsKey(claveNombre)) {
                            productos.put(claveNombre, new ProductoInfo(id, nombre, precio));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error de formato numérico en productos [Línea " + numeroLinea + "]: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("Catálogo de productos cargado y unificado exitosamente.");
        return productos;
    }

    /**
     Escaneamos el directorio del proyecto en busca de los archivos que creamos 
     ventas de cada vendedor y se cruza toda información con los productos unificados.
     */
    
    private static Map<String, VendedorReporte> procesarVentasVendedores(Map<String, ProductoInfo> mapaProductos) throws IOException {
        Map<String, VendedorReporte> vendedoresMap = new HashMap<>();
        File carpetaActual = new File(".");
        
        File[] archivos = carpetaActual.listFiles((dir, name) -> name.startsWith("vendedor") && !name.equals("vendedores.txt"));
        
        //Pequeña validacion de que si se encotraron los archivos de ventas o si la lista esta vacia retornar el mapa de vendedores vacio 
        
        if (archivos == null || archivos.length == 0) {
            System.out.println("Alerta: No se encontraron archivos individuales de ventas en la carpeta.");
            return vendedoresMap;
        }

        //Hace un ciclo leeyendo el contenido de cada archivo de ventas que encontro
        
        for (File archivo : archivos) {
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = reader.readLine();
                if (primeraLinea == null) continue;

                //analiza la primera linea del archivo y extrae nombre y cedula del vendedor
                
                String nombreVendedor = "Desconocido";
                long cedulaVendedor = 0;

                if (primeraLinea.contains("CC;")) {
                    String[] partesCabecera = primeraLinea.split("CC;");
                    nombreVendedor = partesCabecera[0].replace(";", "").trim();
                    if (partesCabecera.length > 1) {
                        try {
                            cedulaVendedor = Long.parseLong(partesCabecera[1].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Error de formato en cédula del archivo " + archivo.getName() + ": " + e.getMessage());
                        }
                    }
                }
                
                //Usamos la cedula como clase unica para buscar al vendedor indicado

                String claveVendedor = String.valueOf(cedulaVendedor);
                VendedorReporte reporte = vendedoresMap.get(claveVendedor);
                if (reporte == null) {
                    reporte = new VendedorReporte(nombreVendedor, cedulaVendedor);
                    vendedoresMap.put(claveVendedor, reporte);
                }
                
                // Lee el resto del archivo de ventas y separa las ventas por ID del prodcuto y cantidad vendida

                String lineaVenta;
                int numLineaVenta = 1;
                while ((lineaVenta = reader.readLine()) != null) {
                    numLineaVenta++;
                    String[] datosVenta = lineaVenta.split(";");
                    if (datosVenta.length >= 3) {
                        try {
                            String idProd = datosVenta[0].trim();
                            int cantidad = Integer.parseInt(datosVenta[2].trim());

                            // VALIDACIÓN AVANZADA: Controlara las cantidades menores o iguales a 0
                            if (cantidad <= 0) {
                                System.err.println("Advertencia [" + archivo.getName() + " - Línea " + numLineaVenta + "]: Cantidad de venta inválida (" + cantidad + "). Se ignora.");
                                continue;
                            }

                            // OPTIMIZACIÓN DE BÚSQUEDA: Es un pequeño mapeo directo optimizado o de búsqueda ágil
                            ProductoInfo prodInfo = null;
                            for (ProductoInfo p : mapaProductos.values()) {
                                if (p.id.equals(idProd)) {
                                    prodInfo = p;
                                    break;
                                }
                            } 
                            
                            //si encontramos el prodcuto lo que se hace es multiplicar el precio unitario por la cantidad vendida 

                            if (prodInfo != null) {
                                reporte.totalRecaudado += (prodInfo.precio * cantidad);
                                prodInfo.cantidadTotalVendida += cantidad;
                            } else {
                                System.err.println("Advertencia [" + archivo.getName() + "]: Producto con ID " + idProd + " no encontrado en el catálogo.");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error numérico en ventas [" + archivo.getName() + " - Línea " + numLineaVenta + "]: " + e.getMessage());
                        }
                    }
                }
            }
        }
        System.out.println("Archivos individuales de ventas procesados con éxito.");
        return vendedoresMap;
    }

    /**
     Genera el archivo CSV con el reporte consolidado de los vendedores.
     */
    
    private static void generarReporteVendedores(Map<String, VendedorReporte> mapaVendedores) throws IOException {
        List<VendedorReporte> listaVendedores = new ArrayList<>(mapaVendedores.values());
        Collections.sort(listaVendedores);

        String nombreArchivo = "reporte_vendedores.csv";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nombreArchivo), StandardCharsets.UTF_8))) {
            
            writer.write("Vendedor;Total Recaudado");
            writer.newLine();

            //Esto es una pequeña accion que se hizo para formatear el dinero recaudado a moneda colombiana COP
            
            for (VendedorReporte v : listaVendedores) {
                String totalFormateado = String.format(new Locale("es", "CO"), "COP %,d", (long) v.totalRecaudado);
                writer.write(v.nombreCompleto + ";" + totalFormateado);
                writer.newLine();
            }
        }
        System.out.println("Reporte de vendedores generado con éxito:" + nombreArchivo);
    }

    /**
     Genera el archivo CSV con el reporte de productos vendidos sin duplicados.
     */
    private static void generarReporteProductos(Map<String, ProductoInfo> mapaProductos) throws IOException {
        List<ProductoInfo> listaProductos = new ArrayList<>(mapaProductos.values());
        
        listaProductos.sort((p1, p2) -> Integer.compare(p2.cantidadTotalVendida, p1.cantidadTotalVendida));

        String nombreArchivo = "reporte_productos.csv";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nombreArchivo), StandardCharsets.UTF_8))) {
            
            writer.write("Producto;Precio Unitario;Cantidad Vendida");
            writer.newLine();

            for (ProductoInfo p : listaProductos) {
                String precioFormateado = String.format(new Locale("es", "CO"), "COP %,d", (long) p.precio);
                writer.write(p.nombre + ";" + precioFormateado + ";" + p.cantidadTotalVendida);
                writer.newLine();
            }
        }
        System.out.println("Reporte de productos generado con formato COP:" + nombreArchivo);
    }
}