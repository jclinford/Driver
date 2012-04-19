package com.driver;

import com.badlogic.gdx.physics.box2d.Body;


/**
 * @author John Linford
 * 
 *	A game object that all moving objects will implement
 *	such as the player, enemies, projectiles and powerups
 *
 */
public interface GameObject 
{
	
	// Update Loop
	public void onManagedUpdate(final float pSecondsElapsed);

	//---------------------//
	// Getters and Setters //
	//---------------------//
	public void dealDamage(int damage);
	public Body getBody();
	public int getHealth();
	public int getDamage();
}
