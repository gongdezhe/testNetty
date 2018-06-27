import com.tulun.Client;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class ClientMain {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 6666;
        try {
            new Client(host, port).start();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
