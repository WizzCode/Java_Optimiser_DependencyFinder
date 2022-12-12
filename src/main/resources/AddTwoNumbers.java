class AddTwoNumbers {

    public static void main(String[] args) {

        int first = 10;
        int second = 20;

        // add two numbers
        int sum =0;
        sum = add(first, second);
        System.out.println(first + " + " + second + " = "  + sum);
    }

    public static int add(int first, int second){
        int sum = first + second;
        return sum;
    }
}