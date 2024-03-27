import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ServerHandler implements Runnable{
	
	private ServerSocket socket;
    private Directory directory; 
    private Scanner scan;
    private File JsonFile;
    
    public ServerHandler(ServerSocket socket, Directory directory, Scanner scan, File JsonFile) {
    	this.socket = socket;
        this.directory = directory;
        this.scan = scan;
        this.JsonFile = JsonFile;
    } 
    
    
	@Override
	public void run() {

			while(true) {

				String request = scan.nextLine();

				if(request.equalsIgnoreCase(("quit"))) {
					
					Gson gson = new GsonBuilder().setPrettyPrinting().create(); /////
					try {
						directory.clearSessions();
						FileWriter fileJ = new FileWriter(JsonFile);
						gson.toJson(directory,fileJ);
						fileJ.flush();
						fileJ.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					System.out.println("Server chiuso");
					scan.close();
            		break;
					
            	} else if(request.equals("info")) {
					try {
						System.out.println("File gestiti: " + directory.getSize());
						System.out.println("Sessioni lettura: " + directory.getReadSession());
						System.out.println("Sessioni Scrittura: " + directory.getWriteSession());
					} catch (NullPointerException e){
						System.out.println("Directory non inizializzata ");
					}
            	} else {
                    System.out.println("Ripetere comando, accettati: quit, info ");
                }
			}
			
		
	}

}