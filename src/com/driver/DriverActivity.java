package com.driver;

import java.io.IOException;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 *
 *	@author John Linford
 *	CS286 Project - Driver game for android
 *	Main driver class
 *
 */

public class DriverActivity extends SimpleBaseGameActivity
{

	public static final int CAMERA_WIDTH = 360;
	public static final int CAMERA_HEIGHT = 240;
	protected static final int MENU_RESET = 0;
	protected static final int MENU_QUIT = MENU_RESET + 1;
	protected static final int MENU_OK = MENU_QUIT + 1;
	protected static final int MENU_NEXT_LEVEL = MENU_OK + 1;


	public static int level = 1;
	public static boolean hasBoss = false;	// if boss is going on or not

	// for main menu and game over and next level
	private MenuScene menuScene;
	private MenuScene nextMenu;
	private MenuScene gameOverMenu;
	private BitmapTextureAtlas menuTexture;
	private ITextureRegion menuResetTextureRegion;
	private ITextureRegion menuQuitTextureRegion;
	private	ITextureRegion menuOkayTextureRegion;

	// texture atlas's for texture and background, and parralax for background
	private BitmapTextureAtlas bitmapTextureAtlas;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private ITextureRegion mParallaxLayer1;
	private ITextureRegion mParallaxLayer2;

	// textures
	private TiledTextureRegion playerTextureRegion;
	public TiledTextureRegion enemyTextureRegion;	// blue car
	public TiledTextureRegion enemy2TextureRegion;	// orange car
	public TiledTextureRegion enemy3TextureRegion;	// van
	public TiledTextureRegion enemy4TextureRegion; 	// truck
	public TiledTextureRegion bossTextureRegion;	// police car animated
	public TiledTextureRegion power1TextureRegion;	// kill all enemies
	public TiledTextureRegion power2TextureRegion;	// heal
	public TiledTextureRegion power3TextureRegion;
	public TiledTextureRegion explosionTextureRegion; // explosion, 12 134x134 images (1602 x 134)
	private Player player;

	// world, level 1, and level 2
	private PhysicsWorld physicsWorld;
	private Scene scene;
	private Scene scene2;

	// music and sound effects
	private Music level1Music;
	private Music level2Music;
	private Music crashSound1;
	private Music crashSound2;
	private Music crashSound3;
	private Music sirenSound;

	// for HUD
	private Font smallFont;
	private Font largeFont;
	private Text healthText;
	private Text scoreText;
	private int score = 0;

	private float xVelocity;
	private float yVelocity;

	private Camera camera;
	private Controls controls;
	public Spawner spawner;


	//---------------------------------------//
	// Methods for/from SuperClass/Interfaces
	//--------------------------------------//
	@Override
	public EngineOptions onCreateEngineOptions() 
	{
		//		Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();

		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getAudioOptions().setNeedsMusic(true);

		return engineOptions;
	}

	@Override
	public void onCreateResources() 
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// set up player/enemy textures
		bitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 36, 72 * 17, TextureOptions.BILINEAR);
		playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "player.png", 0, 0, 1, 1); // 36x32
		enemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "car1.png", 0, 72, 1, 1); // 36x72
		enemy2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "car2.png", 0, 144, 1, 1);
		enemy3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "van1.png", 0, 216, 1, 1);
		enemy4TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "truck1.png", 0, 288, 1, 1); // 36 x 138
		power1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "kill.png", 0, 426, 1, 1); // 36x36
		power2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "heal.png", 0, 462, 1, 1);	// 36 x 36
		power3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "shield.png", 0, 498, 1, 1);	// 36x36
		bossTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "police.png", 0, 534, 1, 2);		// 36 x 146
		explosionTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "explosion.png", 0, 680, 1, 12);	// 36 x something
		bitmapTextureAtlas.load();

		mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 353, 235 * 2);
		mParallaxLayer1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "road3.png", 0, 0);
		mParallaxLayer2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "road2.png", 0, 235);
		mAutoParallaxBackgroundTexture.load();

		// for main menu
		menuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		menuResetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTexture, this, "menu_reset.png", 0, 0);
		menuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTexture, this, "menu_quit.png", 0, 50);
		menuOkayTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTexture, this, "menu_ok.png", 0, 100);
		menuTexture.load();

		// load music
		MusicFactory.setAssetBasePath("sfx/");
		try 
		{
			// music
			level1Music = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "level1.mp3");
			level2Music = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "level2.mp3");
			level1Music.setLooping(true);
			level2Music.setLooping(true);
			level1Music.setVolume(.25f);
			level2Music.setVolume(.25f);

			// crash sound effect
			crashSound1 = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "explosion1.mp3");
			crashSound1.setVolume(.6f);
			crashSound2 = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "explosion2.mp3");
			crashSound2.setVolume(.6f);
			crashSound3 = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "explosion3.mp3");
			crashSound3.setVolume(.6f);

			// siren for boss
			sirenSound = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "siren.mp3");
			sirenSound.setVolume(.25f);
		} 
		catch (final IOException e) 
		{
			Debug.e(e);
		}

		// set up font
		smallFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), 16);
		largeFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), 48);
		smallFont.load();
		largeFont.load();
	}

	@Override
	public Scene onCreateScene() 
	{		
		mEngine.registerUpdateHandler(new FPSLogger());

		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_MOON), false);

		// create menus
		createMenuScene();
		createGameOverMenu();
		createNextMenu();

		// game scene
		scene = new Scene();		
		controls = new Controls(this);
		spawner = new Spawner(this, scene);

		// set touch listeners to scene and menu
		scene.setOnSceneTouchListener(controls);
		scene.setOnAreaTouchListener(controls);
		menuScene.setOnMenuItemClickListener(controls);
		gameOverMenu.setOnMenuItemClickListener(controls);
		nextMenu.setOnMenuItemClickListener(controls);

		// create boundaries / walls
		final VertexBufferObjectManager vertexBufferObjectManager = getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		ground.setColor(.9f, .9f, .9f, 1f);
		roof.setColor(.9f, .9f, .9f, 1f);
		right.setColor(0, 0, 0, 0);
		left.setColor(0, 0, 0, 0);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		// add walls to scene
		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(left);
		scene.attachChild(right);

		// create background
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		Sprite backSprite = new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayer1.getHeight(), this.mParallaxLayer1, vertexBufferObjectManager);
		//backSprite.setRotation((float) (-3.14 / 2));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(85.0f, backSprite));
		scene.setBackground(autoParallaxBackground);

		// register physicsWorld and spawner as update handlers to be called every update
		scene.registerUpdateHandler(this.physicsWorld);
		scene.registerUpdateHandler(spawner);

		// add player
		player = new Player(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, playerTextureRegion, getVertexBufferObjectManager(), this);

		// start music
		level1Music.play();

		// place HUD on screen
		healthText = new Text(0, 0, this.smallFont, "Health: " + player.getHealth(), new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scoreText = new Text(0, 20, this.smallFont, "Score: 0000", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scene.attachChild(healthText);
		scene.attachChild(scoreText);

		return this.scene;
	}

	// show the menu scene if the menu button is pressed
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) 
	{
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
		{
			if(scene.hasChildScene()) 
			{
				// Remove the menu and reset it.
				menuScene.back();
			} 
			else 
			{
				// Attach the menu.
				if (level == 1)
					scene.setChildScene(menuScene, false, true, true);
				else if (level == 3)
					scene2.setChildScene(menuScene, false, true, true);
			}
			return true;
		} 
		else
		{
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public void onResumeGame() 
	{
		super.onResumeGame();
		enableAccelerationSensor(controls);
		
		if (level == 1)
			level1Music.play();
		else
			level2Music.play();
	}

	@Override
	public void onPauseGame() 
	{
		super.onPauseGame();
		disableAccelerationSensor();
		
		// stop music
		level1Music.pause();
		level2Music.pause();
	}


	//----------------//
	// Class Methods
	//---------------//
	protected void createMenuScene() 
	{
		menuScene = new MenuScene(camera);

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, menuResetTextureRegion, getVertexBufferObjectManager());
		resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, menuQuitTextureRegion, getVertexBufferObjectManager());
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);
	}

	// menu for game over
	protected void createGameOverMenu()
	{
		gameOverMenu = new MenuScene(camera);

		final Text gameOverText = new Text(50, 10, largeFont, "GAME OVER!", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		gameOverMenu.attachChild(gameOverText);

		final SpriteMenuItem okayMenuItem = new SpriteMenuItem(MENU_OK, menuOkayTextureRegion, getVertexBufferObjectManager());
		okayMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		gameOverMenu.addMenuItem(okayMenuItem);

		gameOverMenu.buildAnimations();
		gameOverMenu.setBackgroundEnabled(false);
	}

	// menu for next level
	protected void createNextMenu()
	{
		nextMenu = new MenuScene(camera);

		final Text nextText = new Text(10, 10, largeFont, "Level Complete!", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		nextMenu.attachChild(nextText);

		final SpriteMenuItem nextMenuItem = new SpriteMenuItem(MENU_NEXT_LEVEL, menuOkayTextureRegion, getVertexBufferObjectManager());
		nextMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		nextMenu.addMenuItem(nextMenuItem);

		nextMenu.buildAnimations();
		nextMenu.setBackgroundEnabled(false);
	}

	// display game over menu
	protected void gameOver()
	{
		// Attach the menu
		if (level == 3)
			scene2.setChildScene(gameOverMenu, false, true, true);
		else
			scene.setChildScene(gameOverMenu, false, true, true);
	}

	// display next level
	protected void levelComplete()
	{
		scene.setChildScene(nextMenu, false, true, true);
	}

	// load level 2
	protected void loadNextLevel()
	{
		// update attributes
		level = 3;
		hasBoss = false;
		score = 0;
		getPlayer().HEALTH = 150;

		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		// game scene
		scene2 = new Scene();
		spawner = new Spawner(this, scene2);

		// set touch listeners to scene and menu
		scene2.setOnSceneTouchListener(controls);
		scene2.setOnAreaTouchListener(controls);

		// create boundaries / walls
		final VertexBufferObjectManager vertexBufferObjectManager = getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		ground.setColor(.9f, .9f, .9f, 1f);
		roof.setColor(.9f, .9f, .9f, 1f);
		right.setColor(0, 0, 0, 0);
		left.setColor(0, 0, 0, 0);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		// add walls to scene
		scene2.detachChildren();
		scene2.attachChild(ground);
		scene2.attachChild(roof);
		scene2.attachChild(left);
		scene2.attachChild(right);

		// create background
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		Sprite backSprite = new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayer2.getHeight(), this.mParallaxLayer2, vertexBufferObjectManager);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(100.0f, backSprite));
		scene2.setBackground(autoParallaxBackground);

		// register physicsWorld and spawner as update handlers to be called every update
		scene2.registerUpdateHandler(this.physicsWorld);
		scene2.registerUpdateHandler(spawner);

		// add player
		player = new Player(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, playerTextureRegion, getVertexBufferObjectManager(), this);

		// start music
		setMusic(2);

		// place HUD on screen
		healthText = new Text(0, 0, this.smallFont, "Health: " + player.getHealth(), new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scoreText = new Text(0, 20, this.smallFont, "Score: 0000", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scene2.attachChild(healthText);
		scene2.attachChild(scoreText);

		// remove menu and reset it to new level
		getScene().reset();
		getScene().clearChildScene();
		nextMenu.reset();
		nextMenu.clearChildScene();
		nextMenu.closeMenuScene();

		this.getEngine().setScene(scene2);
	}

	// reset to beggining
	public void reset()
	{
		// update attributes
		level = 1;
		hasBoss = false;
		score = 0;
		getPlayer().HEALTH = 100;

		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		// game scene
		scene = new Scene();
		spawner = new Spawner(this, scene);

		// set touch listeners to scene and menu
		scene.setOnSceneTouchListener(controls);
		scene.setOnAreaTouchListener(controls);

		// create boundaries / walls
		final VertexBufferObjectManager vertexBufferObjectManager = getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		ground.setColor(.9f, .9f, .9f, 1f);
		roof.setColor(.9f, .9f, .9f, 1f);
		right.setColor(0, 0, 0, 0);
		left.setColor(0, 0, 0, 0);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		// add walls to scene
		scene.detachChildren();
		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(left);
		scene.attachChild(right);

		// create background
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		Sprite backSprite = new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayer1.getHeight(), this.mParallaxLayer1, vertexBufferObjectManager);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(85.0f, backSprite));
		scene.setBackground(autoParallaxBackground);

		// register physicsWorld and spawner as update handlers to be called every update
		scene.registerUpdateHandler(this.physicsWorld);
		scene.registerUpdateHandler(spawner);

		// add player
		player = new Player(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, playerTextureRegion, getVertexBufferObjectManager(), this);

		// start music
		setMusic(1);

		// place HUD on screen
		healthText = new Text(0, 0, this.smallFont, "Health: " + player.getHealth(), new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scoreText = new Text(0, 20, this.smallFont, "Score: 0000", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
		scene.attachChild(healthText);
		scene.attachChild(scoreText);

		// remove menu and reset it to new level
		getScene().reset();
		getScene().clearChildScene();
		gameOverMenu.reset();
		gameOverMenu.clearChildScene();
		gameOverMenu.closeMenuScene();

		this.getEngine().setScene(scene);
	}


	//---------------------//
	// Getters and Setters //
	//---------------------//
	public void setMusic(int l)
	{
		// start level 1 music, pause level 2
		if (l == 1)
		{
			level1Music.play();
			level2Music.pause();
		}
		// start l2, pause l1
		else if (l == 2)
		{
			level1Music.pause();
			level2Music.play();
		}
	}
	public void setCrashSound()
	{
		// play a random explosion sound
		Random rand = new Random();
		int crashNum = rand.nextInt(3);

		if (crashNum == 0)
			crashSound1.play();
		else if (crashNum == 1)
			crashSound2.play();
		else if (crashNum == 2)
			crashSound3.play();
	}
	public void setSiren()
	{
		// play siren sound (4sec)
		sirenSound.play();
	}

	public void setHealth(int h)
	{
		// update health on HUD
		healthText.setText("Health: " + h);
	}
	public void incScore(int s)
	{
		// update score on HUD
		score += s;
		scoreText.setText("Score: " + score);
	}

	public void setVelocity(float x, float y)
	{
		this.xVelocity = x;
		this.yVelocity = y;
	}
	public float getXGravity()
	{
		return this.xVelocity;
	}
	public float getYGravity()
	{
		return this.yVelocity;
	}

	public PhysicsWorld getPhysicsWorld()
	{
		return physicsWorld;
	}
	public Scene getScene()
	{
		if (level == 1)
			return scene;
		else 
			return scene2;
	}
	public MenuScene getMenuScene()
	{
		return menuScene;
	}

	public Player getPlayer()
	{
		return player;
	}
	public int getScore()
	{
		return score;
	}
}
