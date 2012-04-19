package com.driver;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;


/**
 * 
 * @author John Linford
 * 
 *	Player class that the user controls
 *	to move or shoot
 *
 */
public class Player extends AnimatedSprite implements GameObject
{
	private static int HEALTH = 100;
	private static final float VELOCITY = 1;
	private static final int DAMAGE = 10;
	
	private DriverActivity activity;
	//private final PhysicsHandler physicsHandler;
	private final Body body;


	public Player(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,
			final DriverActivity a) 
	{
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		activity = a;
		//physicsHandler = new PhysicsHandler(this);
		//registerUpdateHandler(physicsHandler);

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, .5f, .5f);
		body = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), this, BodyType.DynamicBody, objectFixtureDef);
		
		// rotate the body so its facing north
		body.setTransform(body.getWorldCenter(), (float)(3.14 / 2 * 3));                                        
		this.setRotation((float)(3.14 / 2 * 3));

		activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(this, body, true, true));

		//animate(new long[]{200,200}, 0, 1, true);
		setUserData(body);
		activity.getScene().registerTouchArea(this);
		activity.getScene().attachChild(this);
	}

	@Override
	public void onManagedUpdate(final float pSecondsElapsed) 
	{
		//		if(this.mX < 0) 
		//		{
		//			this.physicsHandler.setVelocityX(DriverActivity.VELOCITY);
		//		} else if(this.mX + this.getWidth() > DriverActivity.CAMERA_WIDTH) 
		//		{
		//			this.physicsHandler.setVelocityX(-DriverActivity.VELOCITY);
		//		}
		//
		//		if(this.mY < 0) 
		//		{
		//			this.physicsHandler.setVelocityY(DriverActivity.VELOCITY);
		//		} else if(this.mY + this.getHeight() > DriverActivity.CAMERA_HEIGHT) 
		//		{
		//			this.physicsHandler.setVelocityY(-DriverActivity.VELOCITY);
		//		}

		super.onManagedUpdate(pSecondsElapsed);
	}
	
	
	//---------------------//
	// Getters and Setters //
	//---------------------//
	public void dealDamage(int damage)
	{
		HEALTH -= damage;
		// TODO check for 0 health, give game over
	}
	public int getDamage()
	{
		return DAMAGE;
	}
	public int getHealth() 
	{
		return HEALTH;
	}
	
	public Body getBody() 
	{
		return body;
	}

}
