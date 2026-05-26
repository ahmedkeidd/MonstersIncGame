package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import game.engine.Role;
import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.exceptions.InvalidCSVFormat;
import game.engine.monsters.*;

public class DataLoader {

    private static final String CARDS_FILE_NAME = "cards.csv";
    private static final String CELLS_FILE_NAME = "cells.csv";
    private static final String MONSTERS_FILE_NAME = "monsters.csv";

    @SuppressWarnings("resource")
	public static ArrayList<Card> readCards() throws IOException {

        ArrayList<Card> cards = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(CARDS_FILE_NAME));

        String line;

        while((line = br.readLine()) != null){

            String[] data = line.split(",");

            String cardType = data[0];
            String name = data[1];
            String description = data[2];
            int rarity = Integer.parseInt(data[3]);

            if(cardType.equals("SWAPPER"))
                cards.add(new SwapperCard(name, description, rarity));

            else if(cardType.equals("SHIELD"))
                cards.add(new ShieldCard(name, description, rarity));

            else if(cardType.equals("ENERGYSTEAL")){
            	int energy = Integer.parseInt(data[4]);
                cards.add(new EnergyStealCard(name, description, rarity, energy));}

            else if(cardType.equals("STARTOVER")){
            	boolean lucky = Boolean.parseBoolean(data[4]);
                cards.add(new StartOverCard(name, description, rarity, lucky));}

            else if(cardType.equals("CONFUSION")){
            	int duration = Integer.parseInt(data[4]);
            	cards.add(new ConfusionCard(name, description, rarity, duration));
            }
            else{
            	throw new InvalidCSVFormat("Unknown card type: " + cardType);
            }
                
        }

        br.close();
        return cards;
    }
    
    @SuppressWarnings("resource")
	public static ArrayList<Cell> readCells() throws IOException{

        ArrayList<Cell> cells = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(CELLS_FILE_NAME));
        
        String line;
        
        while((line = br.readLine()) != null){
        	String[] data = line.split(",");
        	String name = data[0];
        	int len = data.length;
        	if(len == 3){
        		Role role = Role.valueOf(data[1]);
        		int energy = Integer.parseInt(data[2]);
        		cells.add(new DoorCell(name, role, energy));
        	}
        	else if(len == 2){
        		int effect = Integer.parseInt(data[1]);
        		
        		if(effect > 0)
        			cells.add(new ConveyorBelt(name, effect));
        		else
        			cells.add(new ContaminationSock(name, effect));
        		}
        	else{
        		throw new InvalidCSVFormat(line);
        	}
        	}
    	br.close();
    	return cells;
        }
    @SuppressWarnings("resource")
	public static ArrayList<Monster> readMonsters() throws IOException{
    	 ArrayList<Monster> monsters = new ArrayList<>();
         BufferedReader br = new BufferedReader(new FileReader(MONSTERS_FILE_NAME));
         
         String line;
         
         while((line = br.readLine()) != null){
         	String[] data = line.split(",");
         	String monsterType = data[0];
         	String name = data[1];
         	String description = data[2];
         	Role role = Role.valueOf(data[3]);
         	int energy = Integer.parseInt(data[4]);
         	
         	if (monsterType.equals("DYNAMO"))
         		monsters.add(new Dynamo(name, description, role, energy));  
         	
         	else if (monsterType.equals("DASHER"))
         		monsters.add(new Dasher(name, description, role, energy)); 
         	
         	else if (monsterType.equals("SCHEMER"))
         		monsters.add(new Schemer(name, description, role, energy));
         	
         	else if (monsterType.equals("MULTITASKER"))
         		monsters.add(new MultiTasker(name, description, role, energy));
         	else
         		throw new InvalidCSVFormat("Unknown monster type: " + monsterType);
         	
         }
         br.close();
         return monsters;
    }
    
    
    
        
    }
    

   