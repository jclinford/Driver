package com.driver;

import org.andengine.entity.IEntity;
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
 *	Power up class can create three different power ups
 *	depending on the type parameter 
 *	0 = kill all enemies
 *	1 = heal player
 *	2 = temp immune
 *
 */
public class PowerUp extends AnimatedSprite implements GameObject
{
	private int HEALTH = 1;
	private static final int DAMAGE = 10;

	private int type;

	private DriverActivity activity;
	private Body body;
	

	public PowerUp(final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager,
			final DriverActivity a, final int vx, final int vy, final int t)
	{
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		activity = a;
		type = t;

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
		if (this.collidesWith(activity.getPlayer()))
		{
			Log.i("Driver", "Power up collected");

			// kill all enemies
			if (type == 0)
			{
				for (int i = 0; i < activity.getScene().getChildCount(); i++)
				{
					try
					{
						GameObject obj = (GameObject) activity.getScene().getChild(i);

						// deal damage to all obj's to remove all
						if (obj != (IEntity) activity.getPlayer())
							obj.dealDamage(10);
					}
					catch (Exception e){}
				}
			}
			else if (type == 1)
			{
				// deal negative damage to heal player, then destroy the object
				activity.getPlayer().dealDamage(-10);
				this.dealDamage(HEALTH + 1);
			}
			else if (type == 2)
			{
				activity.getPlayer().setImmune(true);
				this.dealDamage(HEALTH + 1);
			}
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
