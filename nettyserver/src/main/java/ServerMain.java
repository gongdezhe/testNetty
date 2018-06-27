import com.tulun.Server;

/**
 * Created by gongdezhe on 2018/6/27.
 */
public class ServerMain {
    public static void main(String[] args) {
        /**
         * https://my.oschina.net/waylau/blog/380957
         */
        int port = 6666;
        try {
            new Server().bind(port);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
