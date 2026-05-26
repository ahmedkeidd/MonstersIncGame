package game.engine.monsters;

import java.util.ArrayList;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	public Schemer(String name, String description, Role role,int energy){
		super(name,description,role,energy);
	}
	
	@Override
	public int energyModifier(int energy){
		return energy + Constants.SCHEMER_STEAL;
	}
	
	private int stealEnergyFrom(Monster target){
		int energyStolen = 0;
		if(target.getEnergy() >= Constants.SCHEMER_STEAL){
			energyStolen = Constants.SCHEMER_STEAL*-1;
			target.setEnergy(target.getEnergy() + target.energyModifier(energyStolen));
			return energyStolen*-1;
		}
		else {
			energyStolen = target.getEnergy();
			target.setEnergy(target.energyModifier(0));
			return energyStolen;
		}
			
	}
	
	@Override
	public void executePowerupEffect(Monster opponentMonster){
		ArrayList<Monster> stationed = Board.getStationedMonsters();
		int totalBonus = stealEnergyFrom(opponentMonster);
		for (int i = 0; i < stationed.size(); i++) {
			Monster current = stationed.get(i);
			totalBonus += stealEnergyFrom(current); 
		}
		this.setEnergy(this.energyModifier(totalBonus) + this.getEnergy());			
	}

}
