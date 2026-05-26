package game.engine.cells;
import game.engine.monsters.Monster;
public class MonsterCell extends Cell{
	private Monster cellMonster;
	
	public MonsterCell(String name, Monster cellMonster){
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}
	
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster){
		super.onLand(landingMonster, opponentMonster);
		if(landingMonster.getRole() == cellMonster.getRole()){
			landingMonster.executePowerupEffect(opponentMonster);
		}
		else{
			int landingEnergy = landingMonster.getEnergy();
			int cellEnergy = cellMonster.getEnergy();
			if(landingEnergy > cellEnergy){
				cellMonster.setEnergy(cellMonster.energyModifier(landingEnergy));
				int penalty = cellEnergy - landingEnergy;
				landingMonster.alterEnergy(penalty);
			}
		
		}
	}
	
}
