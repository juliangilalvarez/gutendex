package literatura.gutendex;

import literatura.gutendex.principal.Principal;
import literatura.gutendex.repositorio.IAutorRepository;
import literatura.gutendex.repositorio.ILibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GutendexApplication implements CommandLineRunner {

	@Autowired
	private IAutorRepository autorRepositorio;
	@Autowired
	private ILibroRepository libroRepositorio;

	public static void main(String[] args) {
		SpringApplication.run(GutendexApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(autorRepositorio,libroRepositorio);
		principal.muestraElMenu();




	}
}
