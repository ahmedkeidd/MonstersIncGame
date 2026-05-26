package game.engine.monsters;
import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster{
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role,int energy){
		super(name,description,role,energy);
		this.normalSpeedTurns=0;
	}
	
	@Override
	public int energyModifier(int energy){
		return energy + Constants.MULTITASKER_BONUS;
	}

	//Passive 
	@Override
	public void move(int distance){
		if(this.normalSpeedTurns > 0){
			this.normalSpeedTurns--;
			super.move(distance);
		}
		else
			super.move(distance/2);
	}
	
	@Override
	public void executePowerupEffect(Monster opponentMonster){
		this.normalSpeedTurns = 2;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}

}
