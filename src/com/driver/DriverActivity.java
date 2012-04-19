package com.driver;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 *
 *	@author John Linford
 *	CS286 Project - Shooter game for android
 *	Main driver class
 *
 */

public class DriverActivity extends SimpleBaseGameActivity
{

	public static final int CAMERA_WIDTH = 360;
	public static final int CAMERA_HEIGHT = 240;

	private BitmapTextureAtlas bitmapTextureAtlas;

	private TiledTextureRegion playerTextureRegion;
	public TiledTextureRegion enemyTextureRegion;	// blue car
	public TiledTextureRegion enemy2TextureRegion;	// orange car
	public TiledTextureRegion enemy3TextureRegion;	// van
	public TiledTextureRegion enemy4TextureRegion; 	// truck
	private Player player;

	private PhysicsWorld physicsWorld;
	private Scene scene;

	private float xVelocity;
	private float yVelocity;

	private Controls controls;
	private Spawner spawner;


	//---------------------------------------//
	// Methods for/from SuperClass/Interfaces
	//--------------------------------------//
	@Override
	public EngineOptions onCreateEngineOptions() 
	{
		//		Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();

		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() 
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// set up player/enemy textures
		bitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 36, 72 * 6, TextureOptions.BILINEAR);
		playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "player.png", 0, 0, 1, 1); // 36x32
		enemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "car1.png", 0, 72, 1, 1); // 36x72
		enemy2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "car2.png", 0, 144, 1, 1);
		enemy3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "van1.png", 0, 216, 1, 1);
		enemy4TextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(bitmapTextureAtlas, this, "truck1.png", 0, 288, 1, 1);
		
		bitmapTextureAtlas.load();
		
		// set up font
		//mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
		//mFont.load();
	}

	@Override
	public Scene onCreateScene() 
	{		
		mEngine.registerUpdateHandler(new FPSLogger());

		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_MOON), false);

		scene = new Scene();		
		controls = new Controls(this);
		spawner = new Spawner(this, scene);
		
		// set background and touch listeners to scene
		scene.setBackground(new Background(0, 0, 0));
		scene.setOnSceneTouchListener(controls);
		scene.setOnAreaTouchListener(controls);

		// create boundaries / walls
		final VertexBufferObjectManager vertexBufferObjectManager = getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		ground.setColor(0, 0, 0, 0);
		roof.setColor(0, 0, 0, 0);
		right.setColor(0, 0, 0, 0);
		left.setColor(0, 0, 0, 0);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0f, .5f, 0.5f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		// add walls to scene
		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(left);
		scene.attachChild(right);

		// register physicsWorld and spawner as update handlers to be called every update
		scene.registerUpdateHandler(this.physicsWorld);
		scene.registerUpdateHandler(spawner);

		player = new Player(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, playerTextureRegion, getVertexBufferObjectManager(), this);
		//Enemy enemy = new Enemy(CAMERA_WIDTH/2 - 40, 40, enemyTextureRegion, getVertexBufferObjectManager(), this);
		//Enemy enemy2 = new Enemy(1, 1, enemyTextureRegion, getVertexBufferObjectManager(), this);
		return this.scene;
	}

	@Override
	public void onResumeGame() 
	{
		super.onResumeGame();
		enableAccelerationSensor(controls);
	}

	@Override
	public void onPauseGame() 
	{
		super.onPauseGame();
		disableAccelerationSensor();
	}


	//----------------//
	// Class Methods
	//---------------//
	public void addFace(final float pX, final float pY) 
	{
		//this.gameObjectCount++;

		final AnimatedSprite face;
		final Body body;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f);

		face = new AnimatedSprite(pX, pY, this.playerTextureRegion, this.getVertexBufferObjectManager());
		body = PhysicsFactory.createBoxBody(this.physicsWorld, face, BodyType.DynamicBody, objectFixtureDef);

		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

		//face.animate(new long[]{200,200}, 0, 1, true);
		face.setUserData(body);
		this.scene.registerTouchArea(face);
		this.scene.attachChild(face);
	}

	public void jumpFace(final AnimatedSprite face) 
	{
		final Body faceBody = (Body)face.getUserData();

		final Vector2 velocity = Vector2Pool.obtain(this.xVelocity * -50, this.yVelocity * -50);
		faceBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
	}


	//---------------------//
	// Getters and Setters //
	//---------------------//
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
		return scene;
	}

	public Player getPlayer()
	{
		return player;
	}
}
