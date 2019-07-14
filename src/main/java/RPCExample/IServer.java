package RPCExample;

import java.io.IOException;

public interface IServer {
    public void regsiter(Class serviceInterface, Class impl);
    public void start() throws IOException;
    public void stop();
    public boolean isRunning();
}
