package daliy;


import java.util.ArrayList;
import java.util.List;

public class TT {


    static boolean[] state;
    static List<List<Integer>> res = new ArrayList<>();

    //false表示不能选
    public static List<List<Integer>> combinations(int n, int k) {
        state = new boolean[n + 1];
        for (int i = 1; i <= n; i++) {
            state[i] = false;
        }
        for (int i = 1; i <= n; i++) {
            if (state[i] != true) {
                List<Integer> list = new ArrayList<>();
                state[i] = true;
                list.add(i);
                fun(n, list, k - 1);
            }
        }
        return res;
    }

    private static void fun(int n, List list, int k) {
        if (k == 0) {
            res.add(list);
            return;
        }
        for (int i = 1; i <= n; i++) {
            if (state[i] != true) {
                state[i] = true;
                list.add(i);
                fun(n, list, k - 1);
                state[i] = false;
            }
        }
    }

    public static void main(String[] args) {
        List<List<Integer>> lists = combinations(4, 2);
        System.out.print("[");
        for (List<Integer> list : lists) {
            System.out.print("[");
            for (Integer integer : list) {
                System.out.print(integer + ",");
            }
            System.out.print("]");
        }
        System.out.print("]");
    }
}
