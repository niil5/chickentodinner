/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import edu.upc.epsevg.prop.amazons.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author nilbm y David
 */
public class chickentodinner implements IPlayer, IAuto {
    private String name;
    private GameStatus s;
    private int depth = 2;
    private int numNodosExp = 0;
    private int maxint = Integer.MAX_VALUE; //valor maximo que puede asignarse a un entero
    private int minint = Integer.MIN_VALUE; //valor minimo que puede asignarse a un entero
    
    
    public chickentodinner(String name) {
        this.name = name;
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }
    
    @Override
    public String getName() {
        return "Random(" + name + ")";
    }
    
    private boolean isInBounds(int x, int y) {
        return (x >= 0 && x < s.getSize())
                && (y >= 0 && y < s.getSize());
    }
    
    public Move move(GameStatus s) {
        numNodosExp = 0;
        return null;
    }
    
    private int heuristica(){
        return 0;
    }
    
    private int AlphaBeta(GameStatus s){
    numNodosExp++;          
    int best;               
    if (depth <= 0 || s.isGameOver()){ //añadir mejor condicion de salida   
        return heuristica();                                
    }
   
    if (true){  
        best = minint;  
        for (int i = 0; i<10; i++){                   
        }
        return best;                            
    }
    else{      
        best = maxint;  
        for (int i = 0; i<10; i++){    
        }
        return best;                            
    } 
  } 
}
