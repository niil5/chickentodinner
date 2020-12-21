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
    private long numNodosExp = 0;
    private int maxint = Integer.MAX_VALUE; //valor maximo que puede asignarse a un entero
    private int minint = Integer.MIN_VALUE; //valor minimo que puede asignarse a un entero
    private CellType player;
    
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
        player = s.getCurrentPlayer();
        numNodosExp = 0;
        return null;
    }
    
    private int heuristica(){
        return 0;
    }
    
    
    
    private java.awt.Point shootArrow(GameStatus s){
        java.awt.Point apuntada = null;
        
        return apuntada;
    }
    
    
    
    
    
    private int AlphaBeta(GameStatus s, int alpha, int beta){
    numNodosExp++;          
    int best;               
    if (depth <= 0 || s.isGameOver() || s.getEmptyCellsCount() == 0){ //añadir mejor condicion de salida 
        if(s.isGameOver()){
            if(player == s.GetWinner()) return maxint;
            else return minint;
        }
        return heuristica();                               
    }
    
    java.util.ArrayList<java.awt.Point> priorityAmazon = null; //miramos cual es la que menos movimientos posibles tiene, para priorizarla y que no la bloqueen.
    int amazonIndex = 0;
    for (int i = 0; i<s.getNumberOfAmazonsForEachColor(); i++){
        java.awt.Point pActual = s.getAmazon(s.getCurrentPlayer(), i);
        java.util.ArrayList<java.awt.Point> actualAmazon = s.getAmazonMoves(pActual, true);
        if(priorityAmazon == null){
            priorityAmazon = s.getAmazonMoves(pActual, true); //true para solo la ultima posicion de cada linea de movimiento, false para todas
            amazonIndex = i;
        } else if(priorityAmazon.size() < actualAmazon.size()){
            priorityAmazon = actualAmazon;
            amazonIndex = i;
        }
    }
    
    if (player == s.getCurrentPlayer()){  
        best = minint;  
        for (int i = 0; i<priorityAmazon.size(); i++){  
            GameStatus aux = new GameStatus(s);
            //aux.moveAmazon(aux.getAmazon(aux.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i), shootArrow(aux), 0, 0, SearchType.RANDOM);
            best = Math.max(best, AlphaBeta(aux, alpha, beta));  
            alpha = Math.max(alpha, best);  
            if (alpha >= beta) break;       
        }
        return best;                            
    }
    else{      
        best = maxint;  
        for (int i = 0; i<priorityAmazon.size(); i++){    
            GameStatus aux = new GameStatus(s);
            //aux.moveAmazon(aux.getAmazon(aux.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i), shootArrow(aux), 0, 0, SearchType.RANDOM);
            best = Math.min(best, AlphaBeta(aux, alpha, beta));  
            beta = Math.min(beta, best);    
            if (beta <= alpha) break;     
        }
        return best;                            
    } 
  } 
}
