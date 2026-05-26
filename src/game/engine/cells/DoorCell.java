package game.engine.cells;
import java.util.ArrayList;

import game.engine.Board;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;
public class DoorCell extends Cell implements CanisterModifier{
	
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy){
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public Role getRole() {
		return role;
	}

	public int getEnergy() {
		return energy;
	}
	
	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue){
	    if (monster.getRole().equals(this.getRole())) {
	        monster.alterEnergy(canisterValue); 
	    } else {
	        monster.alterEnergy(-1*canisterValue);
	    }
	}
	
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster){
	    super.onLand(landingMonster, opponentMonster);
	    if (this.activated) 
	        return;
	    else if(landingMonster.getRole().equals(this.role)){
	    	ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
	    	this.modifyCanisterEnergy(landingMonster, this.energy);
	    	for(int i = 0; i<stationedMonsters.size(); i++){
	    		Monster current = stationedMonsters.get(i);
	    		if(current.getRole().equals(landingMonster.getRole()))
	    			this.modifyCanisterEnergy(current, this.energy);
	    	}
	    	this.activated = true;
	    	return;
	    }
	    else if(!landingMonster.getRole().equals(this.role)){
	    	ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
	    	if(landingMonster.isShielded()){
	    		this.modifyCanisterEnergy(landingMonster, this.energy);
	    		return;
	    	}
	    	else{
	    		this.modifyCanisterEnergy(landingMonster, this.energy);
	    		for(int i = 0; i<stationedMonsters.size(); i++){
		    		Monster current = stationedMonsters.get(i);
		    		if(current.getRole().equals(landingMonster.getRole()))
		    			this.modifyCanisterEnergy(current, this.energy);
		    	}
	    		this.activated = true;
	    		return;
	    	}
	    }
	}
	   
}