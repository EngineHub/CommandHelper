/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import org.bukkit.Color;

/**
 *
 * @author Layton
 */
public class BukkitMCColor implements MCColor {
	
	public static MCColor GetMCColor(Color c){
		BukkitMCColor cc = new BukkitMCColor();
		cc.red = c.getRed();
		cc.green = c.getGreen();
		cc.blue = c.getBlue();
		return cc;
	}
	
	public static Color GetColor(MCColor c){
		return Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	private BukkitMCColor(){
		//
	}
	
	private int red;
	private int green;
	private int blue;

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	
}
