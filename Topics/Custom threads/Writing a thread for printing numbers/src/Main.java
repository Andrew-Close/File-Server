class NumbersThread extends Thread {
    private int from;
    private int to;

    public NumbersThread(int from, int to) {
        // implement the constructor
        super();
        this.from = from;
        this.to = to;
    }

    // you should override some method here
    @Override
    public void run() {
        for (int i = this.from; i <= this.to; i++) {
            System.out.println(i);
        }
    }                                                   
}