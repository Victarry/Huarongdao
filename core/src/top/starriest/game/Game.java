package top.starriest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Conf{

    static final int chessHeight = 50;
    static final int chessWidth = 50;
    static final int worldWidth = 200;
    static final int worldHeight = 350;

    static final int button_step_x = 50;
    static final int button_step_y = 300;
}


// TODO 添加步数记录
// TODO 添加时间记录
// TODO 添加成功判定
public class Game extends ApplicationAdapter {

    private Board board;

    @Override
    public void create() {
        board = new Board();
        Gdx.input.setInputProcessor(board);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        board.act(delta);
        board.getViewport().apply();
        board.draw();
    }

    public void dispose() {

    }
}

class Board extends Stage {
    float start_x;
    float start_y;
    float end_x;
    float end_y;
    int step_count = 0;
    List<Chess> chesses;
    public Chess selected;
    Button button_steps;
    Board() {
        super(new StretchViewport(Conf.worldWidth, Conf.worldHeight));

        Chess zu1 = new Chess("zu",0, 0, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu2 = new Chess("zu",3, 0, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu3 = new Chess("zu",1, 1, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu4 = new Chess("zu",2, 1, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess ma = new Chess("ma",0, 1, 1, 2, new Texture(Gdx.files.internal("ma.png")));
        Chess huang = new Chess("huang",3, 1, 1, 2, new Texture(Gdx.files.internal("huang.png")));
        Chess guan = new Chess("guan",1, 2, 2, 1, new Texture(Gdx.files.internal("guan.png")));
        Chess zhang = new Chess("zhang",0, 3, 1, 2, new Texture(Gdx.files.internal("zhang.png")));
        Chess zhao = new Chess("zhao", 3, 3, 1, 2, new Texture(Gdx.files.internal("zhao.png")));
        Chess cao = new Chess("cao",1, 3, 2, 2, new Texture(Gdx.files.internal("cao.png")));
        chesses = new ArrayList<Chess>(Arrays.asList(zu1, zu2, zu3, zu4,
                ma, huang, guan, zhang, zhao, cao));
        for (Chess chess : chesses) {
            this.addActor(chess);
        }
//        button_steps = new Button();
//        button_steps.setPosition(Conf.button_step_x, Conf.button_step_y);
//        this.addActor(button_steps);

    }

    void move(int x, int y) {
        if (selected == null)
            return;
        float origin_x = selected.getX();
        float origin_y = selected.getY();
        float new_x = selected.getX() + x*Conf.chessWidth;
        float new_y = selected.getY() + y*Conf.chessHeight;
        selected.setX(new_x);
        selected.setY(new_y);

        // 是否合理
        boolean ok = true;

        // 判断是否在棋盘内
        if (!inBoard(selected)) {
            ok = false;
        }
        // 判断是否可以移动
        for(Chess chess : chesses)
        {
            if(chess != selected && chess.collision(selected))
            {
                ok = false;
                break;
            }
        }
        // 移动成功
        selected.setX(origin_x);
        selected.setY(origin_y);
        if(ok)
        {
            step_count += 1;
            selected.addAction(Actions.moveTo(new_x, new_y, 0.3f));
        }
    }

    boolean inBoard(Chess ch) {
        if (ch.getX() < 0 || ch.getX() + ch.getWidth() > 4*Conf.chessWidth) {
            return false;
        }
        if (ch.getY() < 0 || ch.getY() + ch.getHeight() > 5*Conf.chessHeight) {
            return false;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0)
            return false;
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        getCamera().unproject(touchPos);
        start_x = touchPos.x;
        start_y = touchPos.y;
        selected = (Chess) hit(start_x, start_y, true);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        getCamera().unproject(touchPos);
        end_x = touchPos.x;
        end_y = touchPos.y;
        float x = end_x - start_x;
        float y = end_y - start_y;
        if (Math.abs(x) > Math.abs(y)) {
            if (x > 0) {
                move(1, 0);
            } else
                move(-1, 0);
        } else {
            if (y > 0) {
                move(0, 1);
            } else
                move(0, -1);
        }
        return true;
    }
}

class Chess extends Actor{

    Texture texture;
    Chess(String name, int x, int y, int width, int height, Texture t) {
        setName(name);
        setX(x*Conf.chessWidth);
        setY(y*Conf.chessHeight);
        setWidth(width*Conf.chessWidth);
        setHeight(height*Conf.chessHeight);
        this.texture = t;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    boolean collision(Chess other)
    {
        Rectangle r1 = new Rectangle(getX(), getY(), getWidth(), getHeight());
        Rectangle r2 = new Rectangle(other.getX(), other.getY(), other.getWidth(), other.getHeight());

        return Intersector.overlaps(r1, r2);
    }
}
