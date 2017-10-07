import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class FileHandler {
	
	public boolean get(Socket socket, String file_name) throws IOException {
		BufferedReader in_data = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		File file = new File(file_name);
		if(file.exists()) {
			System.out.println("File already exists on the client!");
			in_data.close();
			socket.close();
			return false;
		}
		FileOutputStream file_out = new FileOutputStream(file);
		String line = null;
		while((line = in_data.readLine()) != null) {
			file_out.write(line.getBytes());
		}
		file_out.close();
		in_data.close();
		socket.close();
		return true;
	}
	
	public boolean existOnClient(String file_path) {
		return new File(file_path).exists();
	}
	
	public int put(Socket socket, String file_path) throws IOException {
		DataOutputStream out_data = new DataOutputStream(socket.getOutputStream());
		File file = new File(file_path);
		FileInputStream file_input = new FileInputStream(file);
		int int_data;
		while((int_data = file_input.read()) != -1) {
			out_data.writeByte(int_data);
		}
		file_input.close();
		out_data.close();
		return (int)(file.length());
	}

}
