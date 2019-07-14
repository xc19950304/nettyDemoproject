package RPCExample.ServiceImpl;

import RPCExample.IService.IServiceTest1;

public class ServiceImplTest1 implements IServiceTest1 {
    @Override
    public String sayHello(String str) {
        return "hello，"+str;
    }
}
