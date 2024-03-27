import java.util.ArrayList;


public class Directory {

	private ArrayList<FileObj> directory;
	private int wb; //Write Buffer
	private int rb; //Read Buffer
	

	public Directory() {
		this.directory = new ArrayList<FileObj>();
		rb = 0; //////
		wb = 0; /////
	}

	public int getSize() {
		if (directory.isEmpty()) {
			return 0;
		}
        return directory.size();
	}

	public String getList() {
		StringBuilder temp = new StringBuilder();
		for (FileObj file : directory) {
			temp.append(file.toString()).append(" -- ");
		}
		if (!temp.isEmpty())
			return temp.toString();
		else
			return "La directory è vuota";
	}

	public String createFile(String name) {
		boolean check = false;
		for (FileObj file : directory) {
            if (file.getName().equals(name)) {
                check = true;
                break;
            }
		}
		if (!check) {
			FileObj file = new FileObj(name);
			directory.add(file);
			return "File creato";
		} else
			return "File esistente";
	}

	public boolean fileExist(String name){
		for (FileObj file : directory) {
			if (file.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public synchronized void deleteFile(String name) throws InterruptedException {
		FileObj fileProv = null;
		for (FileObj file : directory) {
			if (file.getName().equals(name)) {
				while (file.getWriteBuffer() && file.getReadBuffer() > 0) {
					wait();
				}
				fileProv = file;
			}
		}
		directory.remove(fileProv);
		notifyAll();
	}

	public synchronized FileObj getFileToRead(String name) throws InterruptedException {
		FileObj fileReading = null;
		for (FileObj file : directory) {
			if (file.getName().equals(name)) {
				while (file.getWriteBuffer()) {
					wait();
				}
				fileReading = file;
				notifyAll();
			}
		}
		return fileReading;
	}


	public synchronized FileObj getFileToEdit(String name) throws InterruptedException {
		FileObj fileWriting = null;
		for (FileObj file : directory) {
			if (file.getName().equals(name)) {
				while (file.getWriteBuffer() || file.getReadBuffer() > 0) {
					wait();
				}
				fileWriting = file;
				notifyAll();
			}
		}
		return fileWriting;
	}



	public synchronized void wakeReadBuffer() {
		this.rb = rb + 1;
		notifyAll();
	}
	public synchronized void wakeWriteBuffer() {
		this.wb = wb + 1;
		notifyAll();
	}

	public synchronized void deleteReadBuffer() {
		this.rb = rb - 1;
		notifyAll();
	}

	public synchronized void deleteWriteBuffer() {
		this.wb = wb - 1;
		notifyAll();
	}

	public int getReadSession() {
		int readers = 0;
		for (FileObj file : directory) {
			readers += file.getReadBuffer();
		}
		return readers;
	}

	public int getWriteSession() {
		int writers = 0;
		for (FileObj file : directory) {
			if (file.getWriteBuffer())
				writers++;
		}
		return writers;
	}

	public void clearSessions() {
		for (FileObj file : directory) {
			if (file.getWriteBuffer() || file.getReadBuffer() != 0) {
				file.finishWriteSession();
				file.setReadBuffer(0);
			}
		}
	}
}

