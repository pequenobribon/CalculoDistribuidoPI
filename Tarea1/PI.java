//Realizó: Carlos Armando Rojas de la Rosa
//Motivó: Mary 
//Fecha: 5 de octubre del 2020
//Grupo: 4CV2
//Materia: Desarrollo de Sistemas Distribuidos

import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;
import java.nio.ByteBuffer;

class PI
{
  static Object lock = new Object();
  static double pi = 0;
  static final long TERMINOS = 10000000;
 // lee del DataInputStream todos los bytes requeridos

  static void read(DataInputStream f,byte[] b,int posicion,int longitud) throws Exception
  {
    while (longitud > 0)
    {
      int n = f.read(b,posicion,longitud);
      posicion += n;
      longitud -= n;
    }
  }

  static class Worker extends Thread{
    Socket conexion;
    Worker(Socket conexion)
    {
      this.conexion = conexion;
    }
    public void run()
    {
      // Algoritmo 1

	//Se crean los buffers de entrada y salida
	try{
	
		DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
	        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
	
		//Se crea la variable x de tipo double y se iguala a la suma que ha sido calculada por cliente
				
		double x = entrada.readDouble();

		//Se realiza la suma de pi + x haciendo uso del objeto lock para la sincronización
		synchronized( lock ){
			pi += x;
		}

		//Cerramos los streams de entrada y salida
		entrada.close();		
		salida.close();

		//Cerramos la conexión
		conexion.close();
		
		
	}

	catch(Exception e){
		System.err.println(e.getMessage());
	} 	
    }
  }
  public static void main(String[] args) throws Exception
  {
    if (args.length != 1)
    {
	//Se indica si se debe comportar como cliente o servidor
      System.err.println("Uso:");
      System.err.println("java PI <nodo>");
      System.exit(0);
    }
    int nodo = Integer.valueOf(args[0]);
    if (nodo == 0)
    {
      // Algoritmo 2
	System.out.println("Servidor");

	//Instanciamos un objeto de la clase ServerSocket
	ServerSocket servidor = new ServerSocket(50000);

	//Declaramos un vector w de la clase Worker con 3 elementos
	Worker arrayWorkers[] = new Worker[3];
	
	int i = 0;
	//Aquí se crean los clientes que van a realizar las sumas	
	for(i = 0; i < 3; i++){

		Socket conexion;
		conexion = servidor.accept();

		Worker w = new Worker(conexion);
		arrayWorkers[i] = w;

		arrayWorkers[i].start();		
	}
	
	double suma = 0;
	i = 0;
	//algoritmo para realizar las sumas de los términos de PI
	for(i = 0; i < TERMINOS;i++){
		suma = 4.0 / ( 8 * i + 1 ) + suma;
	}
	
	synchronized( lock ){
		pi = suma + pi;
	}

	i = 0;
	
	for(i = 0; i < 3; i++){
	
		arrayWorkers[i].join();
	}
	
	System.out.println("PI:" + pi);

    }
    else
    {
      // Algoritmo 3
	System.out.println("Cliente");
	Socket conexion = null;
	//Algoritmo para realizar reintento en caso de no concretar la conexión con el servidor
	for(;;)
		try{
			conexion = new Socket("localhost",50000);
			break;		
		}
		catch(Exception e){
			Thread.sleep(100);	
		}
	//Se declaran los buffers de entrada y salida
	DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
	DataInputStream entrada = new DataInputStream(conexion.getInputStream());
	
	double suma = 0;
	int i = 0;
	
	for(i = 0; i < TERMINOS; i++){
		suma = 4.0 / (8 * i + 2 * (nodo - 1) + 3) + suma;
	}
	
	suma = nodo % 2 == 0 ? suma: - suma;
	salida.writeDouble(suma);
	
	salida.close();
	entrada.close();
	conexion.close();
	
    }
  }
}
