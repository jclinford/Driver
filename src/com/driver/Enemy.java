package com.driver;

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
	private int HEALTH = 10;
	//private static final float VELOCITY = 3;
	private static final int DAMAGE = 10;

	private DriverActivity activity;
	private Body body;

	public Enemy(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,
			final DriverActivity a, final int vx, final int vy)
	{
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		activity = a;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0f, 0f, true);
		body = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), this, BodyType.KinematicBody, objectFixtureDef);
		
		// rotate body to face north
		body.setTransform(body.getWorldCenter(), (float)(3.14 / 2));                                        
		this.setRotation((float)(3.14 / 2));

		activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		
		setUserData(body);

		body.setLinearVelocity(vx, vy);
	}

	@Override
	public void onManagedUpdate(final float pSecondsElapsed) 
	{
		// check for collisions with player
		if (this.collidesWith(activity.getPlayer()) && !activity.getPlayer().getImmune())
		{
			Log.i("Driver", "Player-Enemy collision");
			activity.getPlayer().dealDamage(DAMAGE);
			HEALTH = 0;
			
			// add explosion animation, in same direction as car was going
			final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f, true);
			AnimatedSprite explosion = new AnimatedSprite(this.getX(), this.getY(), activity.explosionTextureRegion, activity.getVertexBufferObjectManager());
			Body b = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), explosion, BodyType.KinematicBody, objectFixtureDef);
			explosion.setUserData(b);
			explosion.animate(30);
			activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(explosion, b, true, true));
			b.setLinearVelocity(8, 0);
			activity.getScene().attachChild(explosion);
			// add another explosion to make it look more real
			AnimatedSprite explosion2 = new AnimatedSprite(this.getX() + 15, this.getY(), activity.explosionTextureRegion, activity.getVertexBufferObjectManager());
			Body b2 = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), explosion2, BodyType.KinematicBody, objectFixtureDef);
			explosion2.setUserData(b2);
			explosion2.animate(40);
			activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(explosion2, b2, true, true));
			b2.setLinearVelocity(8, 0);
			activity.getScene().attachChild(explosion2);
		}
		
		// check for collisions with boss
		if (activity.level == 1 && activity.spawner.boss != null && this.collidesWith(activity.spawner.boss))
		{
			Log.i("Driver", "Boss - Enemy collision");
			HEALTH = 0;
			
			// add explosion animation, in same direction as car was going
			final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f, true);
			AnimatedSprite explosion = new AnimatedSprite(this.getX(), this.getY(), activity.explosionTextureRegion, activity.getVertexBufferObjectManager());
			Body b = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), explosion, BodyType.KinematicBody, objectFixtureDef);
			explosion.setUserData(b);
			explosion.animate(30);
			activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(explosion, b, true, true));
			b.setLinearVelocity(8, 0);
			activity.getScene().attachChild(explosion);
			// add another explosion to make it look more real
			AnimatedSprite explosion2 = new AnimatedSprite(this.getX() + 15, this.getY(), activity.explosionTextureRegion, activity.getVertexBufferObjectManager());
			Body b2 = PhysicsFactory.createBoxBody(activity.getPhysicsWorld(), explosion2, BodyType.KinematicBody, objectFixtureDef);
			explosion2.setUserData(b2);
			explosion2.animate(40);
			activity.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(explosion2, b2, true, true));
			b2.setLinearVelocity(8, 0);
			activity.getScene().attachChild(explosion2);
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
