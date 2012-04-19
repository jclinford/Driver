package com.driver;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;

import com.badlogic.gdx.math.Vector2;

/**
 * 
 * @author John Linford
 * 
 *	Handles all controls including
 *	accelerometer and touch 
 *
 */
public class Controls implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener
{

	private DriverActivity activity;
	
	public Controls(DriverActivity a)
	{
		activity = a;
	}
	
	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if(activity.getPhysicsWorld() != null)
		{
			if(pSceneTouchEvent.isActionDown()) 
			{
				activity.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) 
	{
		//TODO could just replace this with velocity applied to object to be quicker?
		// get new gravity
		final Vector2 velocity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		
		// update activity and physics world and recycle
		activity.setVelocity(velocity.x, velocity.y);
		activity.getPhysicsWorld().setGravity(velocity);
		//activity.getPlayer().getBody().setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);		
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, ITouchArea pTouchArea, float pTouchAreaLocalX, float pTouchAreaLocalY) 
	{
		if(pSceneTouchEvent.isActionDown()) 
		{
			final AnimatedSprite face = (AnimatedSprite) pTouchArea;
			activity.jumpFace(face);
			return true;
		}

		return false;
	}
}
