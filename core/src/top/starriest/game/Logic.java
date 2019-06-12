package top.starriest.game;

import com.badlogic.gdx.Gdx;

import java.util.*;

public class Logic {
    final static int Left = -1;
    final static int Right = 1;
    final static int Up = -4;
    final static int Down = 4;
    Board board;
    //已经走过地图类型（去重复用）
    HashSet<String> history = new HashSet<String>();
    //每一步的所有走法（走到终点回溯上一步用，如果只求步数则可以不要）
    List<List<Node>> allNodes = new ArrayList<List<Node>>();
    //下一步各种走法节点
    List<Node> nextList;
    int index;
    boolean dirty = true;
    Stack<Node> ans;

    public Logic(Board board) {
        this.board = board;
    }

    public static void main(String[] args) {
        (new Logic(null)).Next();
//        Logic l = new Logic(new Board());
//        new Board();
//        System.out.println(1);
    }

    void Move(int dir, String map, char ch, boolean first) {
        StringBuilder work = new StringBuilder(map.replace(ch, ' '));

        for (int i = 0; i < 20; i++) {
            if (map.charAt(i) == ch) {
                int pos = i + dir;
                int x = i % 4;

                if (dir == Left && x == 0 ||
                        dir == Right && x == 3 ||
                        pos < 0 || pos >= 20) return;

                if (work.charAt(pos) != ' ') return;
                work.setCharAt(pos, ch);
            }
        }
        String _work = work.toString();
        //重复检查
        if (IsDuplicate(_work)) return;
        //加入下一步,记录父节点
        nextList.add(new Node(_work, index, ch, dir));
//        if (first) {
//            //试着走第二步，但不能退回
//            if (dir != Right) Move(Left, _work, ch, false);
//            if (dir != Left) Move(Right, _work, ch, false);
//            if (dir != Down) Move(Up, _work, ch, false);
//            if (dir != Up) Move(Down, _work, ch, false);
//        }
    }

    boolean IsDuplicate(String map) {
        StringBuilder layout = new StringBuilder(
                //相似的形状统一成一种，去重复
                map.replace('3', '1').replace('4', '1').replace('6', '1').replace('7', '0').replace('8', '0').replace('9', '0')
        );

        if (!history.add(layout.toString())) return true;

        //左右镜像（大约节约1/2时间），去重复
        StringBuilder reverse = new StringBuilder(layout.toString());
        for (int k = 0; k < 20; k++) {
            int x = 3 - (k % 4);
            int y = k / 4;
            reverse.setCharAt(y * 4 + x, layout.charAt(k));
        }
        return history.contains(reverse.toString());
    }

    void Move(int index) {
        ans = new Stack<Node>();
        ArrayList<String> outList = new ArrayList<String>();
        int parent = index;
        Node last = null;
        for (int level = allNodes.size() - 1; level >= 0; level--) {
            last = allNodes.get(level).get(parent);
            parent = last.parent;
            ans.push(last);
            if(level == 1)
            {
                break;
            }
        }
        exec();
    }

    void exec()
    {
        Node next = ans.pop();
        Gdx.app.log("next run: ", Character.toString(next.ch) + " " + Integer.toString(next.dir) );
        Gdx.app.log("Next: \n", "");
        for (int y = 0; y < 5; y++)
        {
            System.out.println(next.map.substring(y * 4,y * 4 + 4));
        }
        char ch = next.ch;
        int dir = next.dir;
        Chess to_move = null;
        if(ch == '0') to_move = board.chesses.get("zu2");
        if(ch == '1') to_move = board.chesses.get("zhang");
        if(ch == '2') to_move = board.chesses.get("cao");
        if(ch == '3') to_move = board.chesses.get("zhao");
        if(ch == '4') to_move = board.chesses.get("ma");
        if(ch == '5') to_move = board.chesses.get("guan");
        if(ch == '6') to_move = board.chesses.get("huang");
        if(ch == '7') to_move = board.chesses.get("zu3");
        if(ch == '8') to_move = board.chesses.get("zu4");
        if(ch == '9') to_move = board.chesses.get("zu1");
        board.selected = to_move;
        if(dir == Left)
            board.move(-1, 0);
        if(dir == Up)
            board.move(0, 1);
        if(dir == Right)
            board.move(1, 0);
        if(dir == Down)
            board.move(0, -1);
        dirty = false;
    }

    public void Scan() {
        String initMap = encode();
        history = new HashSet<String>();
        allNodes = new ArrayList<List<Node>>();
        List<Node> curList = new ArrayList<Node>();
        curList.add(new Node(initMap, 0, '0', 0));

        long begin = (new Date()).getTime();
        //迭代直到无路可走
        while (curList.size() > 0) {
            //记录每一步
            allNodes.add(curList);

            nextList = new ArrayList<Node>();
            for (index = 0; index < curList.size(); index++) {
                String map = curList.get(index).map;
                //到达终点的判断
                if (map.charAt(4 * 4 + 1) == '2' && map.charAt(4 * 4 + 2) == '2') {
                    System.out.println("time:" + ((new Date()).getTime() - begin));
                    dirty = false;
                    Move(index);
                    return;
                }
                //穷举各种可能性,去重复，加入到下一步的节点
                for (char ch = '0'; ch <= '9'; ch++) {
                    Move(Left, map, ch, true);
                    Move(Right, map, ch, true);
                    Move(Up, map, ch, true);
                    Move(Down, map, ch, true);
                }
            }
            //迭代
            curList = nextList;
        }
        Gdx.app.log("无解", "5555");
    }

    public void Next() {
        if(dirty)
            Scan();
        else
            exec();
    }

    String encode() {
        List<Character> map = new ArrayList<Character>();
        for (int i = 0; i < 20; i++) {
            map.add(' ');
        }

        for(Chess chess: board.chesses_list)
        {
            setChessCode(chess, map);
        }
        StringBuilder sb = new StringBuilder();
        for (Character ch : map) {
            sb.append(ch);
        }
        return sb.toString();
    }

    int setChessCode(Chess chess, List<Character> map) {
        String name = chess.getName();
        int x = Math.round(chess.getX() / Conf.chessWidth);
        int y = Math.round(chess.getY() / Conf.chessHeight);
        int loc = (4 - y) * 4 + x;
        int code = 0;
        List<Integer> locs = new ArrayList<Integer>();
        locs.add(loc);
        if (name.equals("zu1"))
            code = 9;
        if (name.equals("zu2"))
            code = 0;
        if (name.equals("zu3")) code = 7;
        if (name.equals("zu4")) code = 8;
        if (name.equals("zhang"))
        {
            code = 1;
            locs.add(loc-4);
        }
        if (name.equals("guan"))
        {
            locs.add(loc+1);
            code = 5;
        }
        if (name.equals("cao")) {
            locs.add(loc+1);
            locs.add(loc-4);
            locs.add(loc-3);
            code = 2;
        }
        if (name.equals("ma")) {
            code = 4;
            locs.add(loc-4);
        }
        if (name.equals("huang")) {
            code = 6;
            locs.add(loc-4);
        }
        if (name.equals("zhao")) {
            code = 3;
            locs.add(loc-4);
        }
        for(Integer index: locs){
            map.set(index, Integer.toString(code).charAt(0));
        }
        return 0;
    }

    class Node {
        public String map;
        public int parent;
        public char ch;
        public int dir;

        public Node(String map, int parent, char ch, int dir) {
            this.map = map;
            this.parent = parent;
            this.ch = ch;
            this.dir = dir;
        }
    }
}
