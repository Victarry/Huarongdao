package top.starriest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class Conf{

    static final int chessHeight = 50;
    static final int chessWidth = 50;
    static final int worldWidth = 200;
    static final int worldHeight = 350;

    static final int button_step_x = 50;
    static final int button_step_y = 300;
}


// TODO 添加成功判定
public class Game extends ApplicationAdapter {
    private Skin skin;
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
    HashMap<String, Chess> chesses;
    List<Chess> chesses_list;
    public Chess selected;
    Label button_steps;
    TextButton hint;
    Skin skin;
    Logic logic;
    Board() {
        super(new StretchViewport(Conf.worldWidth, Conf.worldHeight));
        chesses = new HashMap<String, Chess>();
        logic = new Logic(this);
        skin = new Skin(Gdx.files.internal("shadow-walker-ui.json")) {
            //Override json loader to process FreeType fonts from skin JSON
            @Override
            protected Json getJsonLoader(final FileHandle skinFile) {
                Json json = super.getJsonLoader(skinFile);
                final Skin skin = this;
                json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
                    @Override
                    public FreeTypeFontGenerator read(Json json,
                                                      JsonValue jsonData, Class type) {
                        String path = json.readValue("font", String.class, jsonData);
                        jsonData.remove("font");

                        FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.valueOf(json.readValue("hinting",
                                String.class, "AutoMedium", jsonData));
                        jsonData.remove("hinting");

                        Texture.TextureFilter minFilter = Texture.TextureFilter.valueOf(
                                json.readValue("minFilter", String.class, "Nearest", jsonData));
                        jsonData.remove("minFilter");

                        Texture.TextureFilter magFilter = Texture.TextureFilter.valueOf(
                                json.readValue("magFilter", String.class, "Nearest", jsonData));
                        jsonData.remove("magFilter");

                        FreeTypeFontGenerator.FreeTypeFontParameter parameter = json.readValue(FreeTypeFontGenerator.FreeTypeFontParameter.class, jsonData);
                        parameter.hinting = hinting;
                        parameter.minFilter = minFilter;
                        parameter.magFilter = magFilter;
                        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.parent().child(path));
                        BitmapFont font = generator.generateFont(parameter);
                        skin.add(jsonData.name, font);
                        if (parameter.incremental) {
                            generator.dispose();
                            return null;
                        } else {
                            return generator;
                        }
                    }
                });

                return json;
            }
        };

        final Table root = new Table();
        root.setTouchable(Touchable.childrenOnly);
        root.setFillParent(true);
        this.addActor(root);

        Stack stack = new Stack();
        root.add(stack);

        Image image = new Image(new Texture(Gdx.files.internal("bg.jpg")));
        image.setScaling(Scaling.stretch);
        image.setTouchable(Touchable.disabled);
        stack.add(image);

        Table table = new Table();
        stack.add(table);


        Chess zu1 = new Chess("zu1",0, 0, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu2 = new Chess("zu2",3, 0, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu3 = new Chess("zu3",1, 1, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess zu4 = new Chess("zu4",2, 1, 1, 1, new Texture(Gdx.files.internal("zu.png")));
        Chess ma = new Chess("ma",0, 1, 1, 2, new Texture(Gdx.files.internal("ma.png")));
        Chess huang = new Chess("huang",3, 1, 1, 2, new Texture(Gdx.files.internal("huang.png")));
        Chess guan = new Chess("guan",1, 2, 2, 1, new Texture(Gdx.files.internal("guan.png")));
        Chess zhang = new Chess("zhang",0, 3, 1, 2, new Texture(Gdx.files.internal("zhang.png")));
        Chess zhao = new Chess("zhao", 3, 3, 1, 2, new Texture(Gdx.files.internal("zhao.png")));
        Chess cao = new Chess("cao",1, 3, 2, 2, new Texture(Gdx.files.internal("cao.png")));
        chesses_list = new ArrayList<Chess>(Arrays.asList(zu1, zu2, zu3, zu4,
                ma, huang, guan, zhang, zhao, cao));
        for (Chess chess : chesses_list) {
            chesses.put(chess.getName(), chess);

            table.addActor(chess);
        }

        button_steps = new Label("Step count 0", skin);
        button_steps.setTouchable(Touchable.disabled);
        button_steps.setPosition(50, Conf.button_step_y);
//        button_steps.setWidth(50);
        this.addActor(button_steps);

        hint = new TextButton("Hint", skin);
        hint.setTransform(true);
        hint.setScale(0.5f, 0.5f);
        hint.setPosition(40, 250);
        hint.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                logic.Next();
            }

        });
        this.addActor(hint);
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
        for(Chess chess : chesses.values())
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
            logic.dirty = true;
            step_count += 1;
            button_steps.setText("Step count "+Integer.toString(step_count));
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
        Actor hited = hit(start_x, start_y, true);
        if(hited instanceof Chess)
            selected = (Chess) hited;
        else{
            selected = null;
        }
        return super.touchDown(screenX, screenY, pointer, button);
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
        return super.touchUp(screenX, screenY, pointer, button);
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
