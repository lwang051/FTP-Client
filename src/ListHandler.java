import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class ListHandler {
	
	public void list(Socket socket) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line = null;
		while((line = in.readLine()) != null) {
			System.out.println(line);
		}
		in.close(); // Actually this will close socket as well !!!
	}
	
	public boolean exist(Socket socket, String file_path) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		boolean exist = false;
		String line = null;
		while((line = in.readLine()) != null) {
			if(line.equals(file_path)) {
				exist = true;
				break;
			}
		}
//		in.close();  // This will close socket as well !!!
		return exist;
	}
	
}
