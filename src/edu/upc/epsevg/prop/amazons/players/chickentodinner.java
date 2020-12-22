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
    private int maxDepth = 2;
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
        int alpha = minint;     
        int beta = maxint; 
        int best = minint;
        int bestAmazon = 0;
        Point bestMove = null;
        
        for (int i = 0; i<s.getNumberOfAmazonsForEachColor(); i++){
           java.awt.Point pActual = s.getAmazon(s.getCurrentPlayer(), i);
           java.util.ArrayList<java.awt.Point> actualAmazon = s.getAmazonMoves(pActual, false);
           for (int j = 0; j<actualAmazon.size(); j++){
                GameStatus aux = new GameStatus(s);
                aux.moveAmazon(pActual, actualAmazon.get(j));
                aux.placeArrow(shootArrow(aux));
                int ab = AlphaBeta(aux, alpha, beta, maxDepth);      
                if (ab>best){                  
                    bestMove=actualAmazon.get(j);                     
                    best = ab;                 
                }                       
            alpha = Math.max(alpha, best);
               
           }
           
        }
        if(bestMove == null){
            System.out.println("bestMove == NULL");
        }
        return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove, shootArrow(s), (int)numNodosExp, maxDepth, SearchType.RANDOM);
    }
    
    private int heuristica(GameStatus s){
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        double posx,posy;
        CellType jugadoractual=s.getCurrentPlayer();
        for (int i = 0; i<numeroamazones; i++){
            amazonActual=s.getAmazon(jugadoractual,i);
        
     
        }
       return getHeuristica(s,CellType.PLAYER1)-getHeuristica(s,CellType.PLAYER2);//REVISAR!!!!!!!!PLAYER!!!!!    
    }
    private int getHeuristica(GameStatus s,CellType p){
        return 0;
    }
    
    
    
 private java.awt.Point shootArrow(GameStatus s){
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        int tauler=s.getSize();
        int amazonIndex;
        double posx,posy;
        int buida=0;
        int bestAmazon=0;
        int minBuida=maxint;
        CellType jugadoractual=s.getCurrentPlayer();
        for (int i = 0; i<numeroamazones; i++){
            amazonActual=s.getAmazon(jugadoractual,i);
            posx=amazonActual.getX();
            posy=amazonActual.getY();
            amazonIndex=i;
            for (double x = posx-1; x<posx+1; x++){
                 for (double y = posy-1; y<posy+1; y++){ 
                    if(isInBounds((int)x,(int)y)){//Revisar si x o y pot ser iguala tauler
                        if(s.getPos((int)x,(int)y)==CellType.EMPTY){
                           ++buida;
                       }
                    }
                }
            } 
            //comparacion em quedo amb la més petita
            if(buida<minBuida){
                minBuida=buida;
                bestAmazon=amazonIndex;
            }
            buida=0;// Revisar
        }
        amazonActual=s.getAmazon(jugadoractual,bestAmazon);
        posx=amazonActual.getX();
        posy=amazonActual.getY();
        boolean trobada=true;
        for (double x = posx-1; x<posx+1; x++){
            for (double y = posy-1; y<posy+1; y++){ 
                if((x>=0 || x<tauler) && (y>=0 || y<tauler) ){//Revisar si x o y pot ser iguala tauler
                    if(s.getPos((int)x,(int)y)==CellType.EMPTY || trobada){
                        trobada=false;
                        apuntada=new Point((int)x,(int)y);
                    }
                }
            }
        }
        return apuntada;
    }
    
    
    
    
    
    private int AlphaBeta(GameStatus s, int alpha, int beta, int depth){
    numNodosExp++;          
    int best;               
    if (depth <= 0 || s.isGameOver() || s.getEmptyCellsCount() == 0){ //añadir mejor condicion de salida 
        if(s.isGameOver()){
            if(player == s.GetWinner()) return maxint;
            else return minint;
        }
        return heuristica(s);                               
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
            aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i));
            aux.placeArrow(shootArrow(aux));
            best = Math.max(best, AlphaBeta(aux, alpha, beta, depth-1));  
            alpha = Math.max(alpha, best);  
            if (alpha >= beta) break;       
        }
        return best;                            
    }
    else{      
        best = maxint;  
        for (int i = 0; i<priorityAmazon.size(); i++){    
            GameStatus aux = new GameStatus(s);
            aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i));
            aux.placeArrow(shootArrow(aux));
            best = Math.min(best, AlphaBeta(aux, alpha, beta, depth-1));  
            beta = Math.min(beta, best);    
            if (beta <= alpha) break;     
        }
        return best;                            
    } 
  } 
}

