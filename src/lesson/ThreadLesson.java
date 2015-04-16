package lesson;

public class ThreadLesson {

    public static void main(String[] args) {
        ThreadPrinter t1 = new ThreadPrinter("egg");
        ThreadPrinter t2 = new ThreadPrinter("hen");
        t1.start();
        t2.start();
    }

    protected static class ThreadPrinter extends Thread {
        private String text;

        public ThreadPrinter(String text) {
            this.text = text;
        }

        @Override
        public void run(){
            for (int i = 0; i < 10; i++) {
                synchronized (System.out) {
                    try {
                        System.out.println(text);
                        System.out.notifyAll();
                        System.out.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
