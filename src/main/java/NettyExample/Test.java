package NettyExample;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args)  {
        /*List<Integer> l1 = new ArrayList<Integer>();
        List<String> l2 = new ArrayList<String>();
        Class l1Class = l1.getClass();
        Class l2Class = l1.getClass();
        System.out.println(l1Class);*/
        List<String>[] l = new ArrayList[10];
        Object o = l;
        Object[] oa = (Object [])o;
        List<Integer> l1 = new ArrayList<Integer>();
        l1.add(new Integer(2));
        oa[0] = l1;
        String s = l[0].get(0);


    }
}
