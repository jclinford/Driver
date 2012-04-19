package com.driver;

import java.util.Random;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.Scene;


import android.util.Log;

/**
 * 
 * @author John Linford
 * 
 *	Used to spawn enemies and items
 */
public class Spawner implements IUpdateHandler
{
	private static final int ENEMEY_SPAWN_TIME = 10;
	private static final int OFFSET = 80;
	
	private static double totalGameTime = 0;
	private static double previousSpawnTime = 0;
	private Random spawnGen = new Random();
	private DriverActivity activity;
	private Scene scene;
	
	public Spawner(DriverActivity a, Scene s)
	{
		scene = s;
		activity = a;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) 
	{
		totalGameTime += pSecondsElapsed;
		
		// only spawn enemy every 3 seconds
		if (totalGameTime - previousSpawnTime > ENEMEY_SPAWN_TIME)
		{
			previousSpawnTime = totalGameTime;
			
			//addBlue();
			//addOrange();
			//addVan();
			//addTruck();
		}
		
		// remove spawned enemies if they go off screen or die
		for (int i = 0; i < scene.getChildCount(); i++)
		{
			
			if (scene.getChild(i).getX() > activity.CAMERA_WIDTH)
			{
				activity.getScene().detachChild(scene.getChild(i));
				Log.i("Shooter", "Detaching enemy: " + scene.getChildCount());
			}
			// TODO remove dead people
		}
		
	}

	@Override
	public void reset() 
	{
		// TODO Auto-generated method stub
		
	}
	
	// adding blue cars
	public void addBlue()
	{
		// get a random y to spawn the person at, add 40 to compensate for boundaries
		int randY = spawnGen.nextInt(activity.CAMERA_HEIGHT - OFFSET);
		randY += OFFSET;
		
		Log.i("Shooter", "UPdating Spawner");
		Enemy enemy = new Enemy(0, randY, activity.enemyTextureRegion, activity.getVertexBufferObjectManager(), activity, 1, 0);
		//this.scene.registerTouchArea(enemy);
		this.scene.attachChild(enemy);
	}
	
	// adding orange car
	public void addOrange()
	{
		int randY = spawnGen.nextInt(activity.CAMERA_HEIGHT - OFFSET);
		randY += OFFSET;
		
		Log.i("Shooter", "Adding orange");
		Enemy enemy = new Enemy(0, randY, activity.enemy2TextureRegion, activity.getVertexBufferObjectManager(), activity, 2, 0);
		this.scene.attachChild(enemy);
	}
	
	// adding van
	public void addVan()
	{
		int randY = spawnGen.nextInt(activity.CAMERA_HEIGHT - OFFSET);
		randY += OFFSET;
		
		Log.i("Shooter", "Adding orange");
		Enemy enemy = new Enemy(0, randY, activity.enemy3TextureRegion, activity.getVertexBufferObjectManager(), activity, 3, 0);
		this.scene.attachChild(enemy);
	}
	
	// adding truck
	public void addTruck()
	{
		int randY = spawnGen.nextInt(activity.CAMERA_HEIGHT - OFFSET);
		randY += OFFSET;
		
		Log.i("Shooter", "Adding orange");
		Enemy enemy = new Enemy(0, randY, activity.enemy4TextureRegion, activity.getVertexBufferObjectManager(), activity, 4, 0);
		this.scene.attachChild(enemy);
	}

}
