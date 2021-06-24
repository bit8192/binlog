package cn.bincker.web.blog;

public class Test {
    @org.junit.jupiter.api.Test
    public void test() throws InterruptedException {
        new Thread(()->{
            synchronized ("test"){
                try {
                    System.out.println("a");
                    Thread.sleep(10000);
                    System.out.println("b");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(100);
        synchronized ("test") {
            System.out.println("c");
        }
    }
}
