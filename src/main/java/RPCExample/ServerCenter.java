package RPCExample;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerCenter implements IServer {


    private static ExecutorService executorService = Executors.newFixedThreadPool
            (Runtime.getRuntime().availableProcessors());
    private static final HashMap<String,Class> serviceMap = new HashMap<String,Class>();

    private static int port;
    private static  boolean isRunning;

    ServerCenter(int port)
    {
        this.port = port;
        isRunning = true;

    }

    @Override
    public void regsiter(Class serviceInterface, Class impl) {
        serviceMap.put(serviceInterface.getName(),impl);

    }

    @Override
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        isRunning = true;
        while(true)
        {
            //监听到该请求，则创建线程池进行数据传输
            Socket socket = serverSocket.accept();
            executorService.execute(new ServiceTask(socket));
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    public  class ServiceTask implements Runnable {
        Socket clientSocket = null;

        public ServiceTask(Socket client) {
            this.clientSocket = client;
        }
        @Override
        public void run() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = clientSocket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                String className = objectInputStream.readUTF();
                String methodName = objectInputStream.readUTF();

                Class<?>[] methodArgTypes = (Class<?>[]) objectInputStream.readObject();
                Object[] arguments = (Object[]) objectInputStream.readObject();

                Class serviceClass = serviceMap.get(className);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(className + " not found");
                }

                Method method = serviceClass.getMethod(methodName,methodArgTypes);
                Constructor constructor = serviceClass.getConstructor();
                Object result = method.invoke(constructor.newInstance(), arguments);

                outputStream = clientSocket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(result);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
