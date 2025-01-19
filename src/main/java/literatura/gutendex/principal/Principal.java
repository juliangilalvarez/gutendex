package literatura.gutendex.principal;

import literatura.gutendex.modelo.*;
import literatura.gutendex.repositorio.IAutorRepository;
import literatura.gutendex.repositorio.ILibroRepository;
import literatura.gutendex.servicio.ConsumoAPI;
import literatura.gutendex.servicio.ConvierteDatos;

import java.util.*;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private final IAutorRepository autorRepositorio;
    private final ILibroRepository libroRepositorio;

    public Principal(IAutorRepository autorRepositorio, ILibroRepository libroRepositorio) {
        this.autorRepositorio = autorRepositorio;
        this.libroRepositorio = libroRepositorio;
    }


    public void muestraElMenu() {

        var opcion = -1;
        var menuInicio = """
                \n Elija la opción a través de su número:
                 1- Buscar libro por titulo.
                 2- Buscar libros registrados en la base de datos.
                 3- Listar autores registrados
                 4- Listar autores vivos en un determinado año
                 5- Listar libros por idioma
                 0- Salir   
                """;

        while (opcion != 0) {
            System.out.println("\n------ Bienvenido a Literalura -----");
            System.out.println(menuInicio);

            try {
                opcion = teclado.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Solo acepta numeros del menu");

            } finally {
                teclado.nextLine();
            }

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresPorAño();
                    break;
                case 5:
                    listarLibrosPorIdiomas();
                    break;
                case 0:

                    System.exit(0);
                    break;
                default:
                    System.out.println("Opcion ingresada no valida, por favor vuelva a intentar.");
                    break;
            }

        }
    }


    private Datos getDatosLibros() {
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatosAPI(URL_BASE + tituloLibro.replace(" ", "+"));
        Datos datosLibros = conversor.obtenerDatos(json, Datos.class);
        return datosLibros;
    }

    private Libro crearLibro(DatosLibro datosLibros, Autor autor) {
        if (autor != null) {
            return new Libro(datosLibros, autor);
        } else {
            System.out.println("El autor es null, no se puede crear el libro");
            return null;
        }
    }
    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        Datos datos = getDatosLibros();
        if (!datos.resultados().isEmpty()) {
            DatosLibro datosLibros = datos.resultados().get(0);
            DatosAutor datosAutores = datosLibros.autor().get(0);

            Libro libroEncontrado = libroRepositorio.findByTitulo(datosLibros.titulo());
            if (libroEncontrado != null) {
                System.out.println("Este libro ya se encuentra en la base de datos");
                System.out.println(libroEncontrado.toString());
            } else {
                Autor autorEncontrado = autorRepositorio.findByNombreIgnoreCase(datosAutores.nombreAutor());
                if (autorEncontrado != null) {
                    Libro nuevoLibro = crearLibro(datosLibros, autorEncontrado);
                    libroRepositorio.save(nuevoLibro);
                    System.out.println("LIBRO AGREGADO\n" + nuevoLibro);
                    System.out.println("**************************\n");
                } else {
                    Autor nuevoAutor = new Autor(datosAutores);
                    nuevoAutor = autorRepositorio.save(nuevoAutor);
                    Libro nuevoLibro = crearLibro(datosLibros, nuevoAutor);
                    libroRepositorio.save(nuevoLibro);
                    System.out.println("**** LIBRO ******\n" + nuevoLibro + "\n");
                }
            }
        } else {
            System.out.println("Libro No Encontrado");
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados");
            return;
        }
        System.out.println(" LOS LIBROS REGISTRADOS SON:\n");
        libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(libro -> {
                    System.out.println("***********   LIBRO  ************");
                    System.out.println(libro);
                });
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepositorio.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados");
            return;
        }
        System.out.println("----- LOS AUTORES REGISTRADOS SON: -----\n");
        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void listarAutoresPorAño() {
        System.out.println("Escribe el año en el que deseas buscar: ");
        var anio = teclado.nextInt();
        teclado.nextLine();
        if(anio < 0) {
            System.out.println("El año debe ser mayor a cero");
            return;
        }
        List<Autor> autoresPorAnio = autorRepositorio.findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThanEqual(anio, anio);
        if (autoresPorAnio.isEmpty()) {
            System.out.println("No se encontraron autores en ese año");
            return;
        }
        System.out.println("----- LOS AUTORES VIVOS REGISTRADOS EN EL AÑO "+ anio + " SON: -----\n");
        autoresPorAnio.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void listarLibrosPorIdiomas() {
        System.out.println("Escribe el idioma ; ");
        String menu = """
                es - Español
                en - Inglés
                fr - Francés
                pt - Portugués
                """;
        System.out.println(menu);
        var idioma = teclado.nextLine();

        Set<String> idiomasValidos = Set.of("es", "en", "fr", "pt");
        if (!idiomasValidos.contains(idioma)) {
            System.out.println("Idioma no válido, intenta de nuevo");
            return;
        }

        List<Libro> librosPorIdioma = libroRepositorio.findByIdiomaContaining(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No hay libros registrados en el idioma: " + idioma);
            return;
        }
        System.out.println(" LOS LIBROS REGISTRADOS EN EL IDIOMA SELECCIONADO SON:\n");
        librosPorIdioma.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);
    }


}
