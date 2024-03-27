import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {

	public static void main (String[] args) {
		ArrayList<Socket> clientConnected = new ArrayList<>();

		if (args.length < 2) {
			System.err.println("Inserisci java Server.java <path> <port>");
			return;
		}
		String pathFile = args[0];

		int port = Integer.parseInt(args[1]);

		try {
			Scanner userInput = new Scanner(System.in);
			ServerSocket serverSocket = new ServerSocket(port);
			Gson gson= new Gson();
			File JsonFile = createJsonFile(pathFile);
			JsonReader jsonReader = new JsonReader(new FileReader(JsonFile));
			Directory directory = gson.fromJson(jsonReader, Directory.class);


			Thread serverHandlerThread= new Thread(new ServerHandler(serverSocket, directory, userInput, JsonFile));
			serverHandlerThread.start();
			System.out.println("Server Attivo");

			while(true) {
				Socket socket = serverSocket.accept();
				clientConnected.add(socket);
				System.out.println("Connesso");
				Thread clientHandlerThread = new Thread( new ClientHandler(socket, directory, JsonFile));
				clientHandlerThread.start();
			}

		} catch (IOException e) {
			try {
				for(Socket varSocket: clientConnected) {
					varSocket.close();
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}

		}
	}

	private static File createJsonFile(String pathFile) {
		File JsonFile;
		Scanner inputUser = new Scanner(System.in);
		String fileName = null;

		while (true){
			System.out.println("Inserisci il nome del file json");
			fileName = inputUser.nextLine();
			if (!checkFileName(fileName)) {
				break;
			}
			System.out.println("Ritenta l'inserimento");
		}

		if(pathFile.equals(".")) 
			JsonFile = new File(fileName + ".txt");
		else 
			JsonFile = new File(pathFile,fileName + ".txt");

		if(!JsonFile.exists()) {
			
			System.out.println(fileName + " e' stato creato, perchè inesistente");
			try {
				JsonFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				FileWriter fw = new FileWriter(JsonFile,true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("{\r\n"
						+ "  \"directory\": []\r\n"
						+ "}");

				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} else {
			System.out.println(fileName +" e' stato trovato");
			return JsonFile;
		}

		return JsonFile;
	}

	public static boolean checkFileName(String fileName) {
		if(fileName.isEmpty()) {
			System.out.println("hai inserito uno spazio vuoto");
			return true;
		} else if ((Character.toString(fileName.charAt(0)).isEmpty())) {
			System.out.println("hai inserito uno spazio vuoto");
			return true;
		} else {
			Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\_^]");
			Matcher matcher = pattern.matcher(fileName);
			return matcher.find();
		}
	}
}