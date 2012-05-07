package com.driver;

import java.util.Random;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.scene.Scene;


import android.util.Log;

/**
 * 
 * @author John Linford
 * 
 *	Used to spawn enemies, items and boss
 */
public class Spawner implements IUpdateHandler
{
	private static final int BLUE_ENEMEY_SPAWN_TIME = 2;
	private static final int ORANGE_ENEMEY_SPAWN_TIME = 5;
	private static final int VAN_ENEMEY_SPAWN_TIME = 8;
//	private static final int TRUCK_ENEMEY_SPAWN_TIME = 25;
	private static final int POWERUP_SPAWN_TIME = 8;
	private static final int BOSS_SPAWN_TIME = 10;
	private static final int START_BOSS_SCORE = 300;
	private static final int LEVEL_COMPLETE_SCORE = 500;

	// the y positions of lanes, used for spawning
	private static final int LANE1 = 165;
	private static final int LANE2 = 115;
	private static final int LANE3 = 58;
	private static final int LANE4 = 10;

	private static double velocity = 1;
	private static double totalGameTime = 0;
	private static double previousBlueSpawnTime = 0;
	private static double previousOrangeSpawnTime = 0;
	private static double previousVanSpawnTime = 0;
//	private static double previousTruckSpawnTime = 0;
	private static double previousPowerSpawnTime = 0;
	private static double previousBossSpawnTime = 0;
	private Random rand = new Random();
	private DriverActivity activity;
	private Scene scene;
	
	public Boss boss = null;

	public Spawner(DriverActivity a, Scene s)
	{
		scene = s;
		activity = a;
		
		// scale velocity depending on level
		if (activity.level == 1)
			velocity = 1;
		else 
			velocity = 1.5;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) 
	{
		totalGameTime += pSecondsElapsed;

		// only spawn blue enemy every 3 seconds.. etc
		if (totalGameTime - previousBlueSpawnTime > BLUE_ENEMEY_SPAWN_TIME)
		{
			previousBlueSpawnTime = totalGameTime;
			addBlue();
		}
		if (totalGameTime - previousOrangeSpawnTime > ORANGE_ENEMEY_SPAWN_TIME)
		{
			previousOrangeSpawnTime = totalGameTime;
			addOrange();
		}
		if (totalGameTime - previousVanSpawnTime > VAN_ENEMEY_SPAWN_TIME)
		{
			previousVanSpawnTime = totalGameTime;
			addVan();
		}
		if (totalGameTime - previousPowerSpawnTime > POWERUP_SPAWN_TIME)
		{
			previousPowerSpawnTime = totalGameTime;
			addPowerUp();
		}
		if (totalGameTime - previousBossSpawnTime > BOSS_SPAWN_TIME && activity.hasBoss)
		{
			// play the siren sound
			activity.setSiren();
			
			previousBossSpawnTime = totalGameTime;
			addBoss();
		}
		
		// check for game over
		if (activity.getPlayer().getHealth() <= 0)
			activity.gameOver();
		
		// at start boss score start spawning bosses
		if (activity.getScore() == START_BOSS_SCORE * activity.level)
		{
			activity.hasBoss = true;
		}
		
		// at levelcompletescore start the next level
		if (activity.getScore() == LEVEL_COMPLETE_SCORE && activity.level == 1)
		{
			activity.levelComplete();
		}

		// remove spawned enemies if they go off screen or die
		for (int i = 0; i < scene.getChildCount(); i++)
		{
			// check if off screen and remove
			if (scene.getChild(i).getX() > activity.CAMERA_WIDTH)
			{
				activity.getScene().detachChild(scene.getChild(i));
				Log.i("Driver", "Detaching enemy (bounds): " + scene.getChildCount());
				
				// if they go off screen, award player with points
				activity.incScore(10);
			}
			// check if dead and remove
			else
			{
				try
				{
					GameObject obj = (GameObject) scene.getChild(i);

					// if health is zero then we have ran into them
					if (obj.getHealth() == 0)
					{
						// remove entity and play explosion sound
						activity.getScene().detachChild((IEntity) obj);
						activity.setCrashSound();
						Log.i("Driver", "Detaching enemy (health): " + scene.getChildCount());
					}
					// if health is -1, then it just needs to be removed with no dmg dealth
					else if (obj.getHealth() < 0)
					{
						activity.getScene().detachChild((IEntity) obj);
					}
				}
				catch (Exception e){}
			}
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
		// spawn the car in a random lane
		int randY = rand.nextInt(4);
		if (randY == 0)
			randY = LANE1;
		else if (randY == 1)
			randY = LANE2;
		else if (randY == 2)
			randY = LANE3;
		else if (randY == 3)
			randY = LANE4;

		Log.i("Driver", "Adding blue");
		Enemy enemy = new Enemy(0, randY, activity.enemyTextureRegion, activity.getVertexBufferObjectManager(), activity, (int)(4 * velocity), 0);
		//this.scene.registerTouchArea(enemy);
		this.scene.attachChild(enemy);
	}

	// adding orange car
	public void addOrange()
	{
		int randY = rand.nextInt(4);
		if (randY == 0)
			randY = LANE1;
		else if (randY == 1)
			randY = LANE2;
		else if (randY == 2)
			randY = LANE3;
		else if (randY == 3)
			randY = LANE4;

		Log.i("Driver", "Adding orange");
		Enemy enemy = new Enemy(0, randY, activity.enemy2TextureRegion, activity.getVertexBufferObjectManager(), activity, (int)(5 * velocity), 0);
		this.scene.attachChild(enemy);
	}

	// adding van
	public void addVan()
	{
		int randY = rand.nextInt(4);
		if (randY == 0)
			randY = LANE1;
		else if (randY == 1)
			randY = LANE2;
		else if (randY == 2)
			randY = LANE3;
		else if (randY == 3)
			randY = LANE4;

		Log.i("Driver", "Adding van");
		Enemy enemy = new Enemy(0, randY, activity.enemy3TextureRegion, activity.getVertexBufferObjectManager(), activity, (int)(6 * velocity), 0);
		this.scene.attachChild(enemy);
	}

	// add kill powerup
	public void addPowerUp()
	{
		PowerUp power = null;

		// remove immunity if it is active before spawning another
		if (activity.getPlayer().getImmune())
			activity.getPlayer().setImmune(false);

		int randY = rand.nextInt(4);
		if (randY == 0)
			randY = LANE1;
		else if (randY == 1)
			randY = LANE2;
		else if (randY == 2)
			randY = LANE3;
		else if (randY == 3)
			randY = LANE4;

		// get a random type of power up to add
		int randType = rand.nextInt(3);

		Log.i("Driver", "Adding PowerUp");

		if (randType == 0)
			power = new PowerUp(0, randY, activity.power1TextureRegion, activity.getVertexBufferObjectManager(), activity, 2, 0, randType);
		else if (randType == 1)
			power = new PowerUp(0, randY, activity.power2TextureRegion, activity.getVertexBufferObjectManager(), activity, 2, 0, randType);
		else if (randType == 2)
			power = new PowerUp(0, randY, activity.power3TextureRegion, activity.getVertexBufferObjectManager(), activity, 2, 0, randType);

		this.scene.attachChild(power);
	}
	
	// add a boss police car, starts at bottom and goes upwards
	public void addBoss()
	{		
		int randY = rand.nextInt(4);
		if (randY == 0)
			randY = LANE1;
		else if (randY == 1)
			randY = LANE2;
		else if (randY == 2)
			randY = LANE3;
		else if (randY == 3)
			randY = LANE4;

		Log.i("Driver", "Adding police");
		boss = new Boss(activity.CAMERA_HEIGHT + 100, randY, activity.bossTextureRegion, activity.getVertexBufferObjectManager(), activity, (int)(-4 * velocity), 0);
		boss.animate(40);
		this.scene.attachChild(boss);
	}

}
