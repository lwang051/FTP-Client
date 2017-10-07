import java.io.IOException;



public class Driver {

	public static void main(String[] args) throws IOException {
		FTPClient client = new FTPClient();
		client.buildControlConnection();
		client.login();
	}

}
