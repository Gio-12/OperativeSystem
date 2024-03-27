import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ClientHandler implements Runnable{
	
	private Socket socket;
	private Directory directory;
	private boolean readSession;
    private boolean writeSession;
    private FileObj file;
    private File JsonFile;
	private String notifyMessage;


    public ClientHandler(Socket socket, Directory directory, File JsonFile) {
        this.socket = socket;
        this.directory = directory;
        this.JsonFile = JsonFile;
    } 

	@Override
	public void run() {		
		try {		
			Scanner fromClient = new Scanner(socket.getInputStream());
			PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true);
			readSession=false;
            writeSession=false;
			file = null;

			while(true) {

				String[] requestCommands;
				String request = fromClient.nextLine();
				requestCommands = request.split(" ",3);

				
				if (requestCommands[0].equalsIgnoreCase("list")) {
					notifyMessage = directory.getList();
					toClient.println(notifyMessage);
				}

				else if (requestCommands[0].equalsIgnoreCase("create")) {

					notifyMessage = directory.createFile(requestCommands[1]);
					toClient.println(notifyMessage);
				}
				//Read Command
				else if (requestCommands[0].equalsIgnoreCase("read")) {

					file = directory.getFileToRead(requestCommands[1]);
					if (file == null) {
						toClient.println("il file è inesistente");
					} else {
						file.addReadBuffer();
						readSession = true;

						directory.wakeReadBuffer();

						String testo = file.getText();
						if ( testo == null || testo.equalsIgnoreCase("")){
							toClient.println("il file non contiene testo");
						} else {
							toClient.println(testo);
							while (true) {
								request = fromClient.nextLine();
								if (request.equals(":close")) {
									readSession = false;

									directory.deleteReadBuffer();

									file.removeReadBuffer();
									break;
								} else {
									toClient.println("Comando non accettato. Accettati :close");/////////
									toClient.flush();
								}
							}
							toClient.println("Sessione lettura conclusa");
						}

					}
				}

				
				else if (requestCommands[0].equalsIgnoreCase("edit")) {
					file = directory.getFileToEdit(requestCommands[1]);
					if (file == null) {
						toClient.println("file inesistente");
					} else {
						file.startWriteSession();
						writeSession = true;

						directory.wakeWriteBuffer();

						String testo = file.getText();
						if (testo == null) {
							testo = "";
						}

						toClient.println("Testo:" + testo);

						while (true) {
							String command = fromClient.nextLine();
							if (command.equalsIgnoreCase(":backspace")) {
								toClient.println(file.backspace());
							}
							else if (command.equalsIgnoreCase(":close")) {
								file.finishWriteSession();

								directory.deleteWriteBuffer();

								writeSession = false;
								break;
							} else if(command.equalsIgnoreCase("")) {
							}
							else {
								file.addNewLine(command);
								toClient.println("Modifica accettata  -- Comandi disponibili :backspace :close");
							}
							toClient.flush();
						}
						toClient.println("Sessione scrittura conclusa");
					}
				}
				
				else if (requestCommands[0].equalsIgnoreCase("rename")) {
					String name = requestCommands[1];
					String newName = requestCommands[2];

					file = directory.getFileToEdit(name);
					boolean check = directory.fileExist(newName);

					if (file == null){
						toClient.println("File non trovato");
					} else if (!check){
						file.setName(newName);
						file.updateLastEdit();
						toClient.println("Nome del file cambiato");
					} else {
						toClient.println("Nome file esistente");
					}
				}
				
				else if (requestCommands[0].equals("delete")) {
					file = directory.getFileToEdit(requestCommands[1]);
					directory.deleteFile(file.getName());
					toClient.println("File Eliminato");
				}
				else if (requestCommands[0].equals("quit")){
					fromClient.close();
					break;
					}
				else {
					toClient.println("Comando non riconosciuto");
				}
			}
			
			socket.close();
			System.out.println("Client chiuso");
			
			} catch (InterruptedException e) {
	        	return;
	        } catch (IOException e) {
	            System.err.println("Error during I/O operation:");
	            e.printStackTrace();
	        } catch (NoSuchElementException e) {
	        	try {
					socket.close();
				} catch (IOException unexpectedShutdown) {
					unexpectedShutdown.printStackTrace();
				}

	        	if(readSession) {
					file.removeReadBuffer();
					readSession=false;

					directory.deleteReadBuffer();

	        	} else if(writeSession) {
					writeSession=false;
					file.finishWriteSession();

					directory.deleteWriteBuffer();
				}

	        	//salvataggio dati sul file 
	        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
				try {
					FileWriter fileJ = new FileWriter(JsonFile);
					gson.toJson(directory,fileJ);
					fileJ.flush();
	            	fileJ.close();
				} catch (IOException e1) {

					e.printStackTrace();
				}
	        					
	        	System.err.println("Chiusura inaspettata del client");
	        }
	        
	    }
}
		

