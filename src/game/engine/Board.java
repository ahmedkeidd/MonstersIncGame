package game.engine;

import game.engine.cells.*;
import game.engine.monsters.*;
import game.engine.cards.*;
import game.engine.exceptions.InvalidMoveException;

import java.util.ArrayList;
import java.util.Collections;


public class Board {
	
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters;
	private static ArrayList<Card> originalCards; 
	public static ArrayList<Card> cards;
	
	private static game.engine.cards.Card lastDrawnCard;
	
	public Board(ArrayList<Card> readCards){
	
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		cards = new ArrayList<Card>();
		originalCards = readCards;
		setCardsByRarity();
		reloadCards();
	}
	
	private int[] indexToRowCol(int index) {
		int n = Constants.BOARD_COLS;   
		int row = index / n;
		int col = index % n;
		if (row % 2 == 1) 
		    col = (n - 1) - col;  
		return new int[]{row, col};
	}

	private Cell getCell(int index) {
	    int[] pos = indexToRowCol(index);
	    int x = pos[0];
	    int y = pos[1];
	    return boardCells[x][y];
	}
	
	private void setCell(int index, Cell cell){
		int[] RowCol = indexToRowCol(index);
		int x = RowCol[0];
		int y = RowCol[1]; 
		boardCells[x][y] = cell;
	}
	
	public void initializeBoard(ArrayList<Cell> specialCells){
		int[] contaminationIndices = Constants.SOCK_CELL_INDICES;
		int[] conveyorIndices = Constants.CONVEYOR_CELL_INDICES;
		int[] monsterIndices = Constants.MONSTER_CELL_INDICES;
		int[] cardIndices = Constants.CARD_CELL_INDICES;
	    for (int index = 0; index < 100; index++) { 
		        setCell(index, new Cell("Rest Cell"));
	    } 
	    int boardIndex = 1;
	    for (int z = 0; z < specialCells.size(); z++) {
	        if (specialCells.get(z) instanceof DoorCell) {
	            if (boardIndex < 100) {
	                setCell(boardIndex, specialCells.get(z));
	                boardIndex += 2;
	            }
	        }
	    }
		for (int z=0, i=0 ; i<conveyorIndices.length && z<specialCells.size(); z++){
			if(specialCells.get(z) instanceof ConveyorBelt){
				setCell(conveyorIndices[i], specialCells.get(z));
				i++;
			}
		}
		for (int z=0, i=0; i<contaminationIndices.length && z<specialCells.size();z++){
			if(specialCells.get(z) instanceof ContaminationSock){
				setCell(contaminationIndices[i], specialCells.get(z));
				i++;
			}
		}
		for (int i = 0; i < monsterIndices.length && i < stationedMonsters.size(); i++) {
	        Monster m = stationedMonsters.get(i);
	        int index = monsterIndices[i];
	        setCell(index, new MonsterCell(m.getName(), m));
	        m.setPosition(index);
	    }
	    for (int i = 0; i < cardIndices.length; i++) {
	        setCell(cardIndices[i], new CardCell("Card Cell"));
	    }
	}
	
	private void setCardsByRarity(){
		ArrayList<Card> updatedCards = new ArrayList<>();
		for(int i = 0; i< originalCards.size(); i++){
			Card current = originalCards.get(i);
			for(int y = 0; y < current.getRarity() ; y++ ){
				updatedCards.add(current);	
			}
		}
		Board.originalCards = updatedCards;
	}
	
	public static void reloadCards(){
		Board.cards = new ArrayList<>(Board.originalCards);
		Collections.shuffle(Board.cards);
	}
	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
	    Role oldRole = currentMonster.getRole();
	    int oldPosition = currentMonster.getPosition();
	    
	    currentMonster.move(roll);

	    getCell(currentMonster.getPosition()).onLand(currentMonster, opponentMonster);

	    if (currentMonster.getPosition() == opponentMonster.getPosition()) {
	        currentMonster.setPosition(oldPosition);
	        throw new InvalidMoveException("Cannot land on opponent!");
	    }
	
	    if (currentMonster.isConfused() && currentMonster.getRole() == oldRole) {
	        currentMonster.decrementConfusion();
	        opponentMonster.decrementConfusion();
	    }
	    
	    updateMonsterPositions(currentMonster, opponentMonster);
	}
	
	private void updateMonsterPositions(Monster player, Monster opponent) {
	    for (int i = 0; i < Constants.BOARD_SIZE; i++) {
	        getCell(i).setMonster(null);
	    }
	    getCell(player.getPosition()).setMonster(player);
	    getCell(opponent.getPosition()).setMonster(opponent);
	}
	
	public static Card drawCard(){
		if (Board.cards.size() == 0)
			reloadCards();
		return Board.cards.remove(0);		
	}

	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}

	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getCards() {
		return cards;
	}

	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}

	public Cell[][] getBoardCells() {
		return boardCells;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	
	public static void setLastDrawnCard(game.engine.cards.Card card) {
	    lastDrawnCard = card;
	}

	public static game.engine.cards.Card getLastDrawnCard() {
	    return lastDrawnCard;
	}
	
	
}
