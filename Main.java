//INFO: ejemplo de Streams, una herramienta MUY potente para componer programas

//PodemosAprender: voy a usar Streams, un tipo de datos que me da elementos de a uno
// es la forma tradicional de hacer independiente el resto de mi programa 
// de si estoy leyendo de la red, un archivo enoooorme, un string en memoria
// y me ofrece metodos para transformar elementos uno a uno, uno a varios,
// filtrar, etc. con solo definir una funcion para eso, sin preocuparme por como se
// recorren. Ademas los streams suelen ser 'lazy', no hacen la operacion hasta que no
// pedis un elemento.
// SEE: https://www.oracle.com/technetwork/es/articles/java/procesamiento-streams-java-se-8-2763402-esa.html

// En este caso lo separo en muchas funciones para que puedas probar por separado, PERO
//  por comodidad muchas veces escribo todo en un solo metodo

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;
import java.util.regex.*;
//TODO: detallar que clase importe de cada paquete, ahora uso * para hacer mas rapido

public class Main {
	//TODO: separar en clases para reusar y componer, dejo todo en Main para que se lea junto


	//------------------------------------------------------------------------------
	//U: una funcion que transforma UNA palabra, a PigLatin, o Jeringozo, o por un ID, etc.
	public static String palabraAPigLatin(String s) {
		return s+"ay"; //TODO: implementar
	}

	public static String palabraAJeringozo(String s) {
		return s+"opo"; //TODO: implementar
	}

	public static String palabraAVesre(String s) {
		return new StringBuilder(s).reverse().toString(); //TODO: implementar
	}
	//PodemosAprender: podria tener varias distintas

	//------------------------------------------------------------------------------
	//S: esta parte es generica, independiente de como transforme cada palabra

	//U: separar string en palabra y separador usando regex
	public static Stream<MatchResult> wordAndSeparator(Scanner scn) {
		return scn.findAll("([a-zA-Z]+)([^a-zA-Z+]*)");
		//A: captura dos grupos, el primero de uno o mas caracteres entre a y z o A y Z, en segundo de cero o mas que NO esten en esos rangos
		//PodemosAprender: las Regular Expressions son una herramienta MUY potente, son equivalentes a los automatas finitos, una familia enooorme de programas, y entran en unos pocos caracteres!
		//SEE: https://en.wikipedia.org/wiki/Regular_expression
	}

	//U: una clase para guardar los que capture en un solo stream
	//TODO: deberia estar en una clase aparte
	public static class WordOrSep {
		boolean isWord= true; //U: verdadero es palabra, falso es separador
		String str= ""; //U: los caracteres de este elemento
		WordOrSep(boolean isWord, String str) { this.isWord= isWord; this.str= str; }
		
		public String toString() { //U: asi puedo hacer ej "hola "+wos , le aplica este toString implicito y pega el string capturado
			return (isWord ? "(w)" : "(s)") + "'" + str + "'"; 
		} 
	}

	//U: Separo el input en palabras y separadores, devuelvo un stream uniforme de WordOrSep para recordar que es que
	//PodemosAprender: podria no usar WordOrSep y simplemente devolver un stream de MatchResult total viene la palabra en el grupo(1) y el separador en el 2; tambien podria devolver un stream de String y cuando mapeo fijarme si la palabra es par o impar, va en el ejemplo PigLatin-OneLiner
	public static Stream<WordOrSep> wordOrSeparator(Scanner scn) {
		return wordAndSeparator(scn).flatMap( 
			m -> Stream.of( 
				new WordOrSep(true, m.group(1)), 
				new WordOrSep(false, m.group(2))
			)
		);
	}

	//U: agregar peek con un cartel a un stream, cuanto tomas un elemento imprime cartel y elemento antes
	public static <T> Stream<T> debug(String cartel, Stream<T> s) {
		//A: notar que el metodo es generico sobre el tipo T, de modo que el typechecker infiere el tipo del resultado a partir del tipo del parametro s
		return s.peek(e -> System.out.println(cartel + e));
	}
	

	public static Stream<WordOrSep> mapPalabras(Stream<WordOrSep> wos, Function<String,String> transformarUnaPalabra) {
		return wos.map(e -> (e.isWord ? new WordOrSep(true, transformarUnaPalabra.apply(e.str)) : e) );
		//SEE: https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html#apply-T-
	}

	public static String  ejemploDetallado(String original) {
		Scanner scn= new Scanner(original);
		//TODO: tambien podria construir el scanner con un nombre de archivo
		//SEE: https://docs.oracle.com/javase/7/docs/api/java/util/Scanner.html#Scanner(java.io.File)

		var wos= wordOrSeparator(scn);
		//DBG: wos= debug("Separe ", wos);

		var palabrasTransformadas= wos;

		//PodemosAprender: puedo aplicar las transformaciones que quiera, en el orden que quiera!
		// proba comentar o descomentar algunas transformaciones

		//palabrasTransformadas= mapPalabras(palabrasTransformadas, s -> palabraAVesre(s));
		palabrasTransformadas= mapPalabras(palabrasTransformadas, s -> palabraAPigLatin(s));
		//palabrasTransformadas= mapPalabras(palabrasTransformadas, s -> palabraAJeringozo(s));

		//A: hasta aca no se transformo ni separo NADA, solo conecte cosas!

		//PodemosAprender: puedo a) devolver el stream para que otra parte decida,
		// o, descomenta la que quieras ...
		// palabrasTransformadas.forEach(e -> System.out.println(e) ); // b) procesar cada elemento
		String r= palabrasTransformadas.reduce("", (acc,e) -> (acc + e.str + " | "), (a,b) -> a+b  ); System.out.println(r); // c) recolectarlos en arrays, strings, etc.
		return r;
		//SEE: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html
	}

	//PodemosAprender: todo el poder para escribir las ideas rapido y cortito (aunque hacerlo asi ofrece menos oportunidades de reuso, hay que buscar un balance!
	public static String ejemploOneLiner(String original) {
		return 
			new Scanner(original)
			.findAll("([a-zA-Z]+)([^a-zA-Z+]*)")
			.flatMap( m -> Stream.of( 
				palabraAPigLatin( m.group(1) ), 
				m.group(2)
			))
			.reduce("", (acc,e) -> (acc + e + " | "), (a,b) -> a+b  ); 
	}

	public static void main(String[] args) {
		String ej1= "Hi, this is a test! Not the best, but a test :)";
		System.out.println( ejemploDetallado(ej1) );
		System.out.println( ejemploOneLiner(ej1) );
	}
}
