package game.engine.cards;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class EnergyStealCard extends Card implements CanisterModifier{
	private int energy;
	
	public EnergyStealCard(String name, String description, int rarity, int energy){
		super(name, description, rarity, true);
		this.energy = energy;
	}

	public int getEnergy() {
		return energy;
	}
	
	@Override
	public void performAction(Monster player, Monster opponent){
		int stolen = opponent.getEnergy();
		if((opponent.isShielded() == false) && (this.energy < stolen)){
			modifyCanisterEnergy(player, this.energy);
			modifyCanisterEnergy(opponent, (this.energy*-1));
		}
		else if((opponent.isShielded() == false) && (this.energy >= stolen)){
			modifyCanisterEnergy(player, stolen);
			modifyCanisterEnergy(opponent, (stolen*-1));
		}
		else{
			modifyCanisterEnergy(opponent, (stolen*-1));
		}
	}
	
	
	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue){
		monster.alterEnergy(canisterValue);
		
	}

}
