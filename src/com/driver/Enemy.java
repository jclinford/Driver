package com.driver;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * 
 * @author John Linford
 * 
 *	Enemy class that will 'attack' the player
 *	and player can destroy them by hitting them (thus taking dmg)
 *	or by shooting them..
 *
 */
public class Enemy extends AnimatedSprite implements GameObject
{
	private static int HEALTH = 10;
	//private static final float VELOCITY = 3;
	private static final int DAMAGE = 10;

	private DriverActivity activity;
	private Body body;

	public Enemy(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,
			final DriverActivity a, final int vx, final int vy)
	{
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		activity = a;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, true);
		body = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), this, BodyType.KinematicBody, objectFixtureDef);
		
		// rotate body to face north
		body.setTransform(body.getWorldCenter(), (float)(3.14 / 2));                                        
		this.setRotation((float)(3.14 / 2));

		activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(this, body, true, true));

		//animate(new long[]{200,200}, 0, 1, true);
		setUserData(body);

		body.setLinearVelocity(vx, vy);
	}

	@Override
	public void onManagedUpdate(final float pSecondsElapsed) 
	{
		// check for collisions with player
		if (this.collidesWith(activity.getPlayer()))
		{
			Log.i("Shooter", "Player-Enemy collision");
			activity.getPlayer().dealDamage(DAMAGE);
			HEALTH = 0;
		}
	}
	
	
	//---------------------//
	// Getters and Setters //
	//---------------------//
	public void dealDamage(int damage)
	{
		HEALTH -= damage;
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
