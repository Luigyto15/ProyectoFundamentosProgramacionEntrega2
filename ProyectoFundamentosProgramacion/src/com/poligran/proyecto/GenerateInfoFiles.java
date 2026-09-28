package com.poligran.proyecto; //Aqui definimos el package al que pertenece la clase.

import java.io.BufferedWriter; //Estas dos son herramientas que nos sirven para
import java.io.FileWriter;     //escribir los textos que necesitamos y guardarlos en mi disco duro 

import java.io.IOException; // Esta clase o herramienta es muy importante ya que maneja los errores si algo falla a la hora que se escriban todos los archivos  

import java.util.Random; // Nos permite generar numeros aleatorios para los textos.

/** La clase GenerateInfoFiles que cree se encargara de darnos de manera pseudoaleatoria
 todos los archivos de entrada tales como productos, información de los vendedores
 y archivos de ventas individuales necesarios para el sistema general. **/
 
public class GenerateInfoFiles {

    // Aqui se hizo una variable de Random para que nos otorge numeros al azar tanto para precios, cantidades y selecionar nombres.
	
    private static final Random random = new Random();

   
    //Se realizan unos arrays de texto para generar nombres, apellidos y productos coherentes a lo que solicitan
    
    private static final String[] NOMBRES = {
        " Mauricio ", " Ana ", " Andrés ", " Lorena ", " Alejandro ", " Tatiana ", " David ", " Valeria ", " Daniel ", " Sofía "
    };
    private static final String[] APELLIDOS = {
        " Leal ", " Tabares ", " Campos ", " Medina ", " Peña ", " Urrego ", " Sánchez ", " Ramírez ", " Piramanrrique ", " Gaviria "
    };
    
    private static final String[] NOMBRES_PRODUCTOS = {
        " Camiseta ", " Pantalón ", " Zapatos ", " Correa ", " Gorra ", " Maleta ", " Chaqueta ", " Guantes ", " Pantaloneta ", " Reloj "
    };

    /**
     Este es el inicio de todo, este es el main donde se crean todos los archivos
     de texto que se necesitan para las siguinetes entregas del proyecto, como 
     esto es un metodo en el cual el usuario no interactua con la consola,
     ya que lo unico que el genera son los archivos de manera aletoria para usarlos.
     Lo unico que añadi fue un mensaje indicando que va hacer el main. 
     */
    
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando la generación de archivos ...");

            // Declaramos una variable para la generacion de 10 productos 
            int totalProductos = 10;
            createProductsFile(totalProductos);

            // Declaramos variable que hace un archivo con la información general de los vendedores
            int totalVendedores = 5;
            createSalesManInfoFile(totalVendedores);

            // Aqui lo que hace es generar archivos de ventas individuales para cada uno de los vendedores
            createSalesMenFile(15,  " Mauricio Leal ", 1000241507L);
            createSalesMenFile(12,  " Ana Tabares ", 1000352708L);
            createSalesMenFile(20, " Tatiana Urrego ", 1021508654L);
            createSalesMenFile(8, " Daniel Piramanrique ", 1000606241L);
            createSalesMenFile(14, " Sofia Gaviria ", 1025654895L);

            // Mando que imprima un mensaje si todo salio bien al crear los archivos
            System.out.println("Proceso Realizado con éxito.Todos los archivos han sido generados.");

            // Aqui el bloque catch almacena cualquier error de escrcitura de los archivos y nos imprime un mensaje de error en la consola
        } catch (IOException e) {
            System.err.println("Error durante la creacion de archivos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    /**
      Este es la parte del main que crea un archivo con la información 
      aleatoria de los productos disponibles.
      Con este formato: ID Producto; Nombre Producto; Precio Por Unidad Producto.
     */
    
    
    public static void createProductsFile(int productsCount) throws IOException {
        String fileName = "productos.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        	
        	//Aqui asignamos un ciclo for para que se repita constantemente y creamos dos <strings para la creacion de ID o serial al procuto y el nombre del producto a usar.
            for (int i = 1; i <= productsCount; i++) {
                String idProd = "PR" + String.format("%03d", i);
                String nombreProd = NOMBRES_PRODUCTOS[random.nextInt(NOMBRES_PRODUCTOS.length)] ;
                double precioUnitario = 10000 + (random.nextInt(90) * 1000); // Escoje un precio al azar entre 10,000 y 100,000
                
                // Pedimos que escriba la linea de texto con una separacion de dos guiones para que no quede tan junto.
                
                writer.write(idProd + " ; " + nombreProd + " ; " + precioUnitario);
                writer.newLine(); //Esto es un salto de linea para seguir con el siguiente prodcuto.
            }
        }
        System.out.println(" Archivo de productos creado: " + fileName);
    }

    
    /**
      Por otro lado en el main aqui se encaraga de crear un archivo con la información 
      de una cantidad puntual de vendedores.
      El formato es: Tipo Documento; Número Documento; Nombres Vendedor; Apellidos Vendedor
     */
    
    
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        String fileName = "vendedores.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
        	
        	//El ciclo for creamos strings para que asigne un tipo de documento, numero de cedula y combina nombre y apellido al azar.
            for (int i = 1; i <= salesmanCount; i++) {
                String tipoDoc = "CC";
                long numDoc = 1000000000L + (long) (random.nextDouble() * 9000000000L);
                String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
                
                
                writer.write(tipoDoc + ";" + numDoc + ";" + nombre + ";" + apellido);
                writer.newLine();
            }
        }
        System.out.println(" Archivo primordial de vendedores creado: " + fileName);
    }

    
    /**
       Por ultimo el main crea un archivo de ventas para un vendedor específico.
       con el formato:
       Tipo Documento Vendedor; Número Documento Vendedor: en primera línea
       ID Producto; Cantidad Vendida: en las líneas siguientes.
     */
    
    
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        String fileName = "vendedor" + id + "--" + name + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Primera línea: Datos del vendedor
            writer.write( name + "CC;" + id);
            writer.newLine();

            // Líneas siguientes: Ventas de productos ID y Cantidad
            for (int i = 0; i < randomSalesCount; i++) {
                int productoNum = 1 + random.nextInt(10); // Asume productos del PR01 al PR010
                String idProducto = "PR" + String.format("%03d", productoNum);
                int cantidad = 1 + random.nextInt(5); // Cantidad entre 1 y 5

                writer.write(idProducto + "; Vendio ;" + cantidad);
                writer.newLine();
            }
        }
        System.out.println(" Archivo de ventas creado para " + name + ": " + fileName);
        
        /**Al final en la consola nos muestra todos los mensajes que decalramos
         y si se crearon los archivos de texto, que se guardaran en el disco duro
         del PC y se almacenaran en la carpeta del ProyectoFundamentosProgramacion **/
    }
}

