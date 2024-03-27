import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class FileObj {
	
	private String name;
	private boolean wb; //Write Buffer
	private int rb; //Read Buffer
	private String lastEdit;
	private ArrayList <String> text;

	public FileObj(String name) {
		this.name = name;
		this.text = new ArrayList<String>();
		this.lastEdit =DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(LocalDateTime.now());
		this.wb = false;
		this.rb = 0;
	}

	public synchronized void setName(String name) throws InterruptedException {
		while(rb >0 && wb) {
			wait();
		}
		this.name=name;
		this.updateLastEdit();
	}
	
	public String getName() {
		return name;
	}
	
	public void updateLastEdit() {
		this.lastEdit =DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(LocalDateTime.now());
	}
	
	// public String getLastChange() {return lastChange;}
	
	// public void setTest(ArrayList<String> test) {this.test = test;}
	
	public String getText() {
		StringBuilder textContent = new StringBuilder();
        for (String s : text) {
            textContent.append(s).append(" ");
        }
		return textContent.toString();
	}
	
	public String backspace() {
		if (text.isEmpty()) {
			return "Nessun contenuto trovato";
		} else {
			text.remove(text.size() - 1);
		}
		return "Ultima riga eliminata";
	}

	public void addNewLine(String newLine){
		text.add(newLine);
	}
	
	public String toString() {
		return "Nome: " + name + " Ultimo aggiornamento: "+ lastEdit +" Utenti in lettura: "+ rb;
	}

		public synchronized void addReadBuffer() {
			this.rb = this.rb +1;
			notifyAll();
		}
		
		public synchronized void removeReadBuffer() {
			this.rb = this.rb -1;
			notifyAll();
		}

		public void setReadBuffer(int rb) {
		    this.rb = rb;
	    }
		
		public int getReadBuffer() {
			return rb;
		}

		
		public boolean getWriteBuffer() {
			return wb;
		}
		
		public synchronized void startWriteSession() throws InterruptedException {
			while(wb || rb >0) {
				wait();
			}
			this.wb=true;
			notifyAll();
		}
		
		public synchronized void finishWriteSession() {
			this.updateLastEdit();
			this.wb=false;
			notifyAll();
		}
}
