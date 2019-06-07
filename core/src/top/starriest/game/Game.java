package top.starriest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.badlogic.gdx.graphics.Color.*;

public class Game extends ApplicationAdapter implements InputProcessor{

	Animation<TextureRegion> animation;
	Board board;
	private int chessWidth = 50;
    private int chessHeight = 50;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private float start_x;
    private float start_y;
    private float end_x;
    private float end_y;
    @Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		board = new Board();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 200, 250);
        Gdx.input.setInputProcessor(this);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        for(Chess chess: board.chesses){
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(chess.c);
            shapeRenderer.rect(chess.x*chessWidth, chess.y*chessWidth,
                    chess.width*chessWidth, chess.height * chessHeight);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(BLACK);
            shapeRenderer.rect(chess.x*chessWidth, chess.y*chessWidth,
                    chess.width*chessWidth, chess.height * chessHeight);
            shapeRenderer.end();
        }

    }

	@Override
	public void dispose () {
        shapeRenderer.dispose();
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0)
            return false;
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        start_x = touchPos.x;
        start_y = touchPos.y;
        board.selectChess((int) (start_x/chessWidth), (int)(start_y/chessHeight));
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        end_x = touchPos.x;
        end_y = touchPos.y;
        float x = end_x - start_x;
        float y = end_y - start_y;
        if(Math.abs(x) > Math.abs(y))
        {
            if(x > 0)
            {
                board.move(1, 0);
            }
            else
                board.move(-1, 0);
        }
        else
        {
            if(y > 0)
            {
                board.move(0, 1);
            }
            else
                board.move(0, -1);
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}

class Board{
    List<Chess> chesses;
    Chess selected;
    Board() {
        Chess zu1 = new Chess(0, 0, 1, 1, YELLOW);
        Chess zu2 = new Chess(3, 0, 1, 1, YELLOW);
        Chess zu3 = new Chess(1, 1, 1, 1, YELLOW);
        Chess zu4 = new Chess(2, 1, 1, 1, YELLOW);
        Chess ma = new Chess(0,  1, 1, 2, GREEN);
        Chess huang = new Chess(3,  1, 1, 2, GREEN);
        Chess guan = new Chess(1, 2, 2, 1, RED);
        Chess zhang = new Chess(0, 3, 1, 2, BLUE);
        Chess zhao = new Chess(3, 3, 1, 2, BLUE);
        Chess cao = new Chess(1, 3, 2, 2, PURPLE);
        chesses = new ArrayList<Chess>(Arrays.asList(zu1, zu2, zu3, zu4,
                ma, huang, guan, zhang, zhao, cao));
    }
    void selectChess(int x, int y)
    {
        for(Chess chess: chesses)
        {
            if(chess.contain(x, y))
            {
                selected = chess;
                return;
            }
        }
    }

    void move(int x, int y)
    {
        selected.x += x;
        selected.y += y;
        Gdx.app.log("move direction", Integer.toString(x) + ':' + Integer.toString(y));

        if(!selected.inBoard())
        {
            selected.x -= x;
            selected.y -= y;
            Gdx.app.log("Can't move", "out of board");
            return;
        }
        for(Chess chess : chesses)
        {
            if(!chess.equals(selected))
            {
                if(chess.collision(selected))
                {
                    selected.x -= x;
                    selected.y -= y;
                    return;
                }
            }
        }
    }
}

class Chess{
    int x;
    int y;
    int width;
    int height;
    Color c;
    Chess(int x, int y, int width, int height, Color c) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.c = c;
    }
    boolean contain(int xx, int yy)
    {
        return xx >= x && xx < x + width && yy >= y && yy < y + height;
    }
    boolean collision(Chess other)
    {
        Rectangle r1 = new Rectangle(this.x, this.y, this.width, this.height);
        Rectangle r2 = new Rectangle(other.x, other.y, other.width, other.height);

        return Intersector.overlaps(r1, r2);
    }

    boolean inBoard() {
        if(x < 0 || x + width > 4){
            return false;
        }
        if(y < 0 || y + height > 5)
        {
            return false;
        }
        return true;
    }
}
