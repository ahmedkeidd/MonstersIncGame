package game.engine.monsters;
import game.engine.Role;

public abstract class Monster implements Comparable<Monster>{
	private String name;
	private String description;
	private Role role;
	private Role originalRole;
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;
	
	
	public Monster(String name, String description, Role originalRole, int energy){
		this.originalRole = originalRole;
		this.role = originalRole;
		this.position = 0;
		this.confusionTurns = 0;
		this.frozen = false;
		this.shielded = false;
		this.name = name;
		this.description = description;
		this.energy = energy;
		
	}
	
	public int compareTo(Monster o){
		Integer monsterA = this.position;
		Integer monsterB = o.position;
		return monsterA.compareTo(monsterB);
		
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public void setEnergy(int energy) {
		if(energy >= 0){
			this.energy = energy;}
		else {
			this.energy =0;
			}
		
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		
		if(position <0)
			this.position = 0;
		else{
			this.position = position % 100;
		}

	}
	
	public boolean isFrozen() {
		return frozen;
	}
	
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	
	public boolean isShielded() {
		return shielded;
	}
	
	public void setShielded(boolean shielded) {
		this.shielded = shielded;
	}
	
	public int getConfusionTurns() {
		return confusionTurns;
	}
	
	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = confusionTurns;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Role getOriginalRole() {
		return originalRole;
	}
	
	public abstract void executePowerupEffect(Monster opponentMonster);
	
	public boolean isConfused(){
		if(this.confusionTurns != 0)
			return true;
		else
			return false;
	}
	
	public void move(int distance){
		this.setPosition(this.getPosition() + distance);
	}
	
	public final void alterEnergy(int energy){
		if(this.shielded == true && energy < 0)
			this.shielded = false;
		else 
			this.setEnergy(this.energy + energyModifier(energy));
	
	}
	
	public int energyModifier(int energy){
		return energy;
	}
	
	
	public void decrementConfusion(){
		if(this.confusionTurns > 0){
			this.confusionTurns -= 1;
			
			if(this.confusionTurns == 0)
				this.role = this.originalRole;	
		}
		else 
			return;
	}
	
	
}
