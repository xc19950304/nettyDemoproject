package RPCExample;

import RPCExample.IService.IServiceTest1;
import RPCExample.ServiceImpl.ServiceImplTest1;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RPCTest {
    public static void main(String arg[])
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerCenter serverCenter = new ServerCenter(8088);
                serverCenter.regsiter(IServiceTest1.class, ServiceImplTest1.class);
                try {
                    serverCenter.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                IServiceTest1 service = RPCClient.getRemoteService(IServiceTest1.class, new InetSocketAddress("localhost", 8088));
                System.out.println(service.sayHello("lh"));
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                IServiceTest1 service = (IServiceTest1) RPCClient.getRemoteService(IServiceTest1.class, new InetSocketAddress("localhost", 8088));
                System.out.println(service.sayHello("llx"));
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                IServiceTest1 service = (IServiceTest1) RPCClient.getRemoteService(IServiceTest1.class, new InetSocketAddress("localhost", 8088));
                System.out.println(service.sayHello("xc"));
            }
        }).start();


    }
}
