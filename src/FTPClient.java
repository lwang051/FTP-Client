import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPClient {
	
	private BufferedReader br;
	private Socket socket_control;
	private BufferedReader control_input;  // DataInputStream.readLine() is deprecated
	private DataOutputStream control_output;
	private ServerSocket socket_data;
	private int socket_data_port;
	private String port_command;
	
	// build data connection in active mode
	private void buildDataConnection() throws IOException {
		socket_data = new ServerSocket(++socket_data_port);
		String hex_str = Integer.toHexString(socket_data_port);
		int first = Integer.parseInt(hex_str.substring(0, 2), 16);
		int second = Integer.parseInt(hex_str.substring(2, hex_str.length()), 16);
		String ip = InetAddress.getLocalHost().toString().trim().split("/")[1];
		port_command = "PORT " + ip.replaceAll("\\.", ",") + "," + first + "," + second + "\r\n";
		control_output.writeBytes(port_command);
		control_input.readLine();
	}
	
	// close data connection
	private void closeDataConnection() throws IOException {
		socket_data.close();
	}
	
	// quit FTP client
	private void quit() throws IOException {
		control_output.writeBytes("QUIT\r\n");
		System.out.println(control_input.readLine());
		br.close();
		control_input.close();
		control_output.close();
		socket_control.close();
		socket_data.close();
	}
	
	// list item(s) in current directory
	private void list() throws IOException {
		buildDataConnection();
		control_output.writeBytes("LIST\r\n");
		System.out.println(control_input.readLine());
		Socket socket = socket_data.accept();
		new ListHandler().list(socket);
		System.out.println(control_input.readLine());
		socket.close();
		closeDataConnection();
	}
	
	// get file from the server, and store it in local storage
	private void get(String file_path) throws IOException {
		// check if the file exists on the server
		buildDataConnection();
		control_output.writeBytes("NLST " + file_path + "\r\n");
		control_input.readLine(); // clear replies we don't need
		control_input.readLine();
		Socket socket = socket_data.accept();
		if(!new ListHandler().exist(socket, file_path)) {
			System.out.println("File doesn't exist on the server!");
			socket.close();
			closeDataConnection();
			return;
		}
		socket.close();
		closeDataConnection();
		// get file
		buildDataConnection();
		control_output.writeBytes("SIZE " + file_path + "\r\n");
		int file_size = Integer.valueOf(control_input.readLine().trim().split("\\s")[1]);
		control_output.writeBytes("RETR " + file_path + "\r\n");
		String response = control_input.readLine();
		System.out.println(response);
		if(!response.trim().split("\\s")[0].equalsIgnoreCase("150")) {
			return;
		}
		String[] arr = file_path.trim().split("\\\\");
		int bytes = 0;
		if(new FileHandler().get(socket_data.accept(), arr[arr.length - 1])) {
			bytes = file_size;
		}
		String trasfered = " (" + bytes + " bytes transfered in total)";
		System.out.println(control_input.readLine() + trasfered);
		closeDataConnection();
	}
	
	// transfer a local file to the server, and store it in the current directory on the server
	private void put(String file_path) throws IOException {
		// check if the file exists on the client
		if(!new File(file_path).exists()) {
			System.out.println("File doesn't exist on the client!");
			return;
		}
		// check if already exists on the server
		buildDataConnection();
		control_output.writeBytes("NLST " + file_path + "\r\n");
		control_input.readLine(); // clear replies we don't need
		control_input.readLine();
		Socket socket = socket_data.accept();
		if(new ListHandler().exist(socket, file_path)) {
			System.out.println("File already exists on the server! Please delete it first.");
			socket.close();
			closeDataConnection();
			return;
		}
		socket.close();
		closeDataConnection();
		// transfer file
		buildDataConnection();
		control_output.writeBytes("STOR " + file_path + "\r\n");
		System.out.println(control_input.readLine());
		socket = socket_data.accept();
		int send_size = new FileHandler().put(socket, file_path);
		System.out.println(control_input.readLine() + " (" + send_size + " bytes transfered in total)");
		socket.close();
		closeDataConnection();
	}
	
	// delete a file on the server
	private void delete(String file_path) throws IOException {
		buildDataConnection();
		control_output.writeBytes("DELE " + file_path + "\r\n");
		System.out.println(control_input.readLine());
		closeDataConnection();
	}
	
	// show "myftp>" command line and wait for user's command
	private void waitingForCommand() throws IOException {
		System.out.print("myftp>");
		String command = null;
		while((command = br.readLine()) != null) {
			if(command.equalsIgnoreCase("quit")) {
				quit();
				break;
			}else if(command.equalsIgnoreCase("ls")) {
				list();
			}else if(command.trim().split("\\s+")[0].equalsIgnoreCase("get")) {
				get(command.trim().split("\\s+")[1]);
			}else if(command.trim().split("\\s+")[0].equalsIgnoreCase("put")) {
				put(command.trim().split("\\s+")[1]);
			}else if(command.trim().split("\\s+")[0].equalsIgnoreCase("delete")){
				delete(command.trim().split("\\s+")[1]);
			}else {
				System.out.println("Command not defined!");
			}
			System.out.print("myftp>");
		}
	}
	
	public FTPClient() {
		br = new BufferedReader(new InputStreamReader(System.in));
	}
	
	// build control connection
	public void buildControlConnection() throws IOException{
		// build control connection
		System.out.println("Use command: \"myftp server_name\" to connect to the server");
		String command;
		command = br.readLine();
		String[] strs = command.trim().split("\\s+");
		if(!strs[0].equalsIgnoreCase("myftp")) {
			System.out.println("Command misused!");      // TODO, consider to be more friendly
			return;
		}
		socket_control = new Socket(strs[1], 21);
		socket_data_port = socket_control.getLocalPort() + 1;
		control_input = new BufferedReader(new InputStreamReader(socket_control.getInputStream()));
		control_output = new DataOutputStream(socket_control.getOutputStream());
		System.out.println(control_input.readLine());
		
	}
	
	// login
	public void login() throws IOException {
		// check username
		System.out.print("Username: ");
		control_output.writeBytes("USER " + br.readLine() + "\r\n");
		String response_for_user = control_input.readLine();
		System.out.println(response_for_user);
		if(!response_for_user.trim().split("\\s+")[0].equalsIgnoreCase("331")) {
			return;
		}
		// check password
		System.out.print("Password: ");
		control_output.writeBytes("PASS " + br.readLine() + "\r\n");   // TODO, hide password in command line
		String response_for_password = control_input.readLine();
		System.out.println(response_for_password);
		if(!response_for_password.trim().split("\\s+")[0].equalsIgnoreCase("230")) {
			return;
		}
		waitingForCommand();
	}
	
}
