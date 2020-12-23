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
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author nilbm y David
 */
public class chickentodinner_ItDepth implements IPlayer, IAuto {
    private String name;
    private GameStatus s;
    private int depth = 0;
    private long numNodosExp = 0;
    private int maxint = Integer.MAX_VALUE; //valor maximo que puede asignarse a un entero
    private int minint = Integer.MIN_VALUE; //valor minimo que puede asignarse a un entero
    private CellType player;
    private CellType enemy;
    private long Zobrist;
    private java.util.HashMap<java.lang.Long, Integer> hmap = new HashMap<java.lang.Long, Integer>();
    long[][][] table= new long[10][10][3];
    private boolean timeOut = false;
    
    public chickentodinner_ItDepth(String name) {
        this.name = name;
    }
    
    
    @Override
    public void timeout() {
        timeOut = true;
    }
    
    @Override
    public String getName() {
        return "Random(" + name + ")";
    }
    
    private boolean isInBounds(int x, int y) {
        return (x >= 0 && x < s.getSize())
                && (y >= 0 && y < s.getSize());
    }
    
    private void initZobrist(){
        Random rd = new Random();
        for (int i=0; i<s.getSize(); i++){
            for (int j=0; j<s.getSize(); j++){
                for (int z=0; z<3; z++){
                    table[i][j][z] = rd.nextLong();
                }
            }
        }
    }
    
    public long getHashKey(GameStatus s){
        long hashKey = 0;
        for (int i=0; i<s.getSize(); i++){
            for (int j=0; j<s.getSize(); j++){
                if(s.getPos(i, j) == CellType.PLAYER1){
                    hashKey ^= table[i][j][0];
                } else if(s.getPos(i, j) == CellType.PLAYER2){
                    hashKey ^= table[i][j][1];
                } else if(s.getPos(i, j) == CellType.ARROW){
                    hashKey ^= table[i][j][2];
                }
            }
       }
        return hashKey;
    }
    
    public Move move(GameStatus s) {
        this.s = s;
        initZobrist();
        timeOut = false;
        player = s.getCurrentPlayer();
        if(player == CellType.PLAYER1) enemy = CellType.PLAYER2;
        else enemy = CellType.PLAYER1;
        numNodosExp = 0;
        depth = 0;
        int alpha = minint;     
        int beta = maxint; 
        int best = minint;
        int bestAmazon = 0;
        Point bestMove = null;
        
        while(!timeOut){
            depth+=1;
            for (int i = 0; i<s.getNumberOfAmazonsForEachColor(); i++){
                java.awt.Point pActual = s.getAmazon(s.getCurrentPlayer(), i);
                java.util.ArrayList<java.awt.Point> actualAmazon = s.getAmazonMoves(pActual, false);
                for (int j = 0; j<actualAmazon.size(); j++){
                    GameStatus aux = new GameStatus(s);
                    aux.moveAmazon(pActual, actualAmazon.get(j));
                    aux.placeArrow(shootArrow(aux));
                    int ab = AlphaBeta(aux, alpha, beta, depth);      
                    if (ab>best){                  
                        bestMove=actualAmazon.get(j);  
                        bestAmazon = i;
                        best = ab;                 
                    }                       
                    alpha = Math.max(alpha, best);
                }         
            }
    
        }
        if(bestMove == null){
            return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), s.getAmazon(s.getCurrentPlayer(), bestAmazon), shootArrow(s), (int)numNodosExp, depth, SearchType.RANDOM);  
        }
        GameStatus aux = new GameStatus(s);
        aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove);
        return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove, shootArrow(aux), (int)numNodosExp, depth, SearchType.RANDOM);
    }
    
    private int heuristica(GameStatus s){
        return getHeuristica(s,player)-getHeuristica(s,enemy);
    }
    
    private int getHeuristica(GameStatus s,CellType p){
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        ArrayList llistapos = new ArrayList(); //Llista de possibles posicions
        int valor=0;
        int valortotal=0;
        CellType jugadoractual=p;
        if(s.isGameOver()){
            
            if(s.GetWinner()==jugadoractual){
                valortotal=maxint;
            }else{
                valortotal=minint;
            }
        }else{
            for (int i = 0; i<numeroamazones; i++){
                amazonActual=s.getAmazon(jugadoractual,i);
                llistapos=s.getAmazonMoves(amazonActual,false);
                valor=llistapos.size();
                valortotal+=valor;      
            } 
        }
        
        return valortotal;
    }
    
    
    
 private java.awt.Point shootArrow(GameStatus s){
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        int tauler=s.getSize();
        int amazonIndex = 0;
        double posx,posy;
        int buida=0;
        int bestAmazon=0;
        int minBuida=maxint;
        CellType jugadoractual=enemy;
        for (int i = 0; i<numeroamazones; i++){
            amazonActual=s.getAmazon(jugadoractual,i);
            posx=amazonActual.getX();
            posy=amazonActual.getY();
            amazonIndex=i;
            for (double x = posx-1; x<=posx+1; x++){
                 for (double y = posy-1; y<=posy+1; y++){ 
                    if(isInBounds((int)x,(int)y)){//Revisar si x o y pot ser iguala tauler
                        if(s.getPos((int)x,(int)y)==CellType.EMPTY){
                           ++buida;
                       }
                    }
                }
            } 
            //comparacion em quedo amb la més petita
            if(buida<minBuida && buida > 0){
                minBuida=buida;
                bestAmazon=amazonIndex;
            }
            buida=0;
        }
        amazonActual=s.getAmazon(jugadoractual,bestAmazon);
        posx=amazonActual.getX();
        posy=amazonActual.getY();
        boolean trobada=false;
        for (double x = posx-1; x<=posx+1 && !trobada; x++){
            for (double y = posy-1; y<=posy+1 && !trobada; y++){
                if(isInBounds((int)x,(int)y)){
                    if(s.getPos((int)x,(int)y)==CellType.EMPTY){
                        trobada=true;

                        apuntada=new Point((int)x,(int)y);
                        return apuntada;
                    }
                }
            }
        }
        if(apuntada == null || !isInBounds((int)apuntada.getX(), (int)apuntada.getY()) || s.getPos((int)apuntada.getX(), (int)apuntada.getY())==CellType.EMPTY){
           int n = s.getEmptyCellsCount();
            Random rand = new Random();
            int p = rand.nextInt(n) + 1;//de 1 a n
            for (int i = 0; i < s.getSize(); i++) {
                for (int j = 0; j < s.getSize(); j++) {
                    if (s.getPos(i, j) == CellType.EMPTY) {
                        p--;
                        if (p == 0) {
                            return new Point(i, j);
                        }
                    }
                }
            }
        } 
        return apuntada;
    }
    
    
    
    
    
    private int AlphaBeta(GameStatus s, int alpha, int beta, int depth){
    if(timeOut){
        return minint;
    }
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

