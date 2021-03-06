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
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author nilbm y David
 */
public class chickentodinner_ItDepth implements IPlayer, IAuto {
    private boolean firtsTime = true;
    private String name;
    private GameStatus s;
    private long numNodosExp = 0;
    private int maxint = Integer.MAX_VALUE; //valor maximo que puede asignarse a un entero
    private int minint = Integer.MIN_VALUE; //valor minimo que puede asignarse a un entero
    private CellType player;
    private CellType enemy;
    private long key;
    private java.util.Hashtable<java.lang.Long, Integer> htable = new Hashtable<java.lang.Long, Integer>();
    long[][][] table= new long[10][10][3];
    private boolean timeOut = false;
    private int depth=0;
    
    
    
    /**
     * Crea un nuevo jugador con nombre = name.
     * @param name
     */
    public chickentodinner_ItDepth(String name) {
        this.name = name;
    }
    
    /**
     * Cambia la variable global timeOut a true si recibimos el evento timeout().
     */
    @Override
    public void timeout() {
        timeOut = true;
    }
    
    /**
     * Devuelve un String correspondiente al nombre de nuestro jugador + el nombre dado al crearlo.
     * @return String
     */
    @Override
    public String getName() {
        return "Chickentodinner(" + name + ")";
    }
    
    /**
     * Funcion para comprobar si un punto esta dentro del tablero, devuelve true si x e y estan dentro, false en caso contrario.
     * @param x coordenada x
     * @param y coordenada y
     * @return true si esta dentro del tablero (guardado en varialbe global s), false en caso contrario
     */
    private boolean isInBounds(int x, int y) {
        return (x >= 0 && x < s.getSize())
                && (y >= 0 && y < s.getSize());
    }
    /**
     * Generamos una tabla de s.getSize()*s.getSize()*3 para guardar longs aleatorios que utilizaremos en nuestra implementacion de Zobrist
     */
    private void initZobrist(){
        Random rd = new Random();
        //Por cada casilla de tablero, guardamos en nuestra tabla tres long aleatorios correspondientes a cada posible "pieza".
        for (int i=0; i<s.getSize(); i++){
            for (int j=0; j<s.getSize(); j++){
                for (int z=0; z<3; z++){
                    table[i][j][z] = rd.nextLong();
                }
            }
        }
    }
    /**
     * Generamos una Key a partir de un tablero y la tabla de valores aleatorios de initZobrist().
     * @param s GameStatus del cual generar la hashKey
     * @return un long correspondiente a la hashKey del tablero GameStatus s.
     */
    public long getHashKey(GameStatus s){
        long hashKey = 0;
        for (int i=0; i<s.getSize(); i++){ //miramos cada posicion del tablero
            for (int j=0; j<s.getSize(); j++){
                if(s.getPos(i, j) == CellType.PLAYER1){ //hacemos una operacion XOR sobre la key dependiendo de que este en esa posicion (si hay algo).
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
    
    /**
     * Funcion para escoger el mejor movimiento a partir de un GameStatus pasado por parametro.  
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @return Devuelve el siguiente movimiento que nuestro jugador quiere hacer.
     */
    public Move move(GameStatus s) {
        this.s = s;
        if(firtsTime){
            initZobrist();
            firtsTime=false;
            key = getHashKey(s);
        }
    
        timeOut = false;
        player = s.getCurrentPlayer();
        if(player == CellType.PLAYER1) enemy = CellType.PLAYER2;
        else enemy = CellType.PLAYER1;
        numNodosExp = 0;
        this.depth = 0;
        int alpha = minint;     
        int beta = maxint; 
        int best = minint;
        int bestAmazon = 0;
        Point bestMove = null;
        
        while(!timeOut){ //mientras no estemos en timeOut, mira nivel a nivel del arbol.
            this.depth+=1; //aumentamos el nivel en cada iterasion (valor inicial 0, asi que la primera iteracion sera depth = 1)
            for (int i = 0; i<s.getNumberOfAmazonsForEachColor(); i++){ //miramos para cada una de nuestras amazonas
                java.awt.Point pActual = s.getAmazon(s.getCurrentPlayer(), i);
                java.util.ArrayList<java.awt.Point> actualAmazon = s.getAmazonMoves(pActual, false); //cada uno de sus movimientos
                for (int j = 0; j<actualAmazon.size(); j++){
                    
                    GameStatus aux = new GameStatus(s);
                    aux.moveAmazon(pActual, actualAmazon.get(j));
                    java.awt.Point apuntada = shootArrow(aux);
                    aux.placeArrow(apuntada);

                    //Calculamos la key aplicando el movimiento realizado antes de llamar a la recursividad y despues deshacemos el movimiento en la key para volver al tablero inicial.
                    //###########################
                    int color;
                    if(s.getCurrentPlayer() == CellType.PLAYER1) color = 0;
                    else color = 1;
                    long oldPoss = table[(int)pActual.getX()][(int)pActual.getY()][color];
                    long newPoss = table[(int)actualAmazon.get(j).getX()][(int)actualAmazon.get(j).getY()][color];
                    long newArrow = table[(int)apuntada.getX()][(int)apuntada.getY()][2];
                    
                    key ^=oldPoss;
                    key ^=newPoss;
                    key ^=newArrow;
                    
                    int ab = AlphaBeta(aux, alpha, beta, this.depth);  
                    
                    key ^=oldPoss;
                    key ^=newPoss;
                    key ^=newArrow;
                    //###########################
                    
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
            return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), s.getAmazon(s.getCurrentPlayer(), bestAmazon), shootArrow(s), (int)numNodosExp, this.depth, SearchType.MINIMAX);  
        }
        GameStatus aux = new GameStatus(s);
        aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove);
        return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove, shootArrow(aux), (int)numNodosExp, this.depth, SearchType.MINIMAX);
    }
    
    
    /**
     * Funcion para calcular la Heuristica general de un GameStatus. 
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @return Devuelve un entero correspondiente a la heuristica del tablero para nuestro jugador - el enemigo.
     */
    private int heuristica(GameStatus s){
        int hValue;
        if(htable.get(key) == null){
            hValue = getHeuristica(s,player)-getHeuristica(s,enemy);
            htable.put(key, hValue);
        } else {
            hValue = htable.get(key);
        }
        return hValue;
    }
    
    /**
     * Funcion para calcular la Heuristica especifica de un jugador en un tablero. 
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @param p CellType.PLAYER1 o CellType.PLAYER2 que estamos analizando.
     * @return Devuelve un entero correspondiente al valor heuristico de el jugador p en el gamestatus s.
     */
    private int getHeuristica(GameStatus s,CellType p){
        
        //Inicializamos variables que usaremos.
        //###########################
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        ArrayList llistapos = new ArrayList(); //Llista de possibles posicions
        int valor=0;
        int valortotal=0;
        CellType jugadoractual=p;
        //###########################
        
        
        if(s.isGameOver()){     //comprobamos si alguien gano, devolvemos valores maximos o minimos dependiendo.
            
            if(s.GetWinner()==jugadoractual){
                valortotal=maxint;
            }else{
                valortotal=minint;
            }
        }else{                  //si no, calculamos cuantos movimientos puede hacer cada amazona y lo sumamos.
            for (int i = 0; i<numeroamazones; i++){ 
                amazonActual=s.getAmazon(jugadoractual,i);
                llistapos=s.getAmazonMoves(amazonActual,false);
                valor=llistapos.size();
                valortotal+=valor;      
            } 
        }
        
        //Calcula cuantas casillas vacias y cuantas amazonas estan bloqueadas para el calculo final de nuestra heuristica.
        //###########################
        int buida = 0;
        int bloqueadas = 0;
        for (int i = 0; i<s.getNumberOfAmazonsForEachColor(); i++){
            amazonActual=s.getAmazon(p,i);
            double posx=amazonActual.getX();
            double posy=amazonActual.getY();  
            for (double x = posx-1; x<=posx+1; x++){
                 for (double y = posy-1; y<=posy+1; y++){ 
                    if(isInBounds((int)x,(int)y)){
                        if(s.getPos((int)x,(int)y)==CellType.EMPTY){
                           ++buida;
                       }
                    }
                }
            } 
            if(buida==0) bloqueadas+=1;
           
        }
        //###########################
        
        return valortotal * (buida/s.getNumberOfAmazonsForEachColor()) - (50*bloqueadas); //calculo final, numº movimientos disponibles * (cantidad de casillas vacias alrededor de las amazonas / numero de amazonas) - (valor arbitrario * numero de amazonas bloqueadas)
    }
    
    
    /**
     * Devuelve la posicion en la que disparar una flecha dado un GameStatus s. Prioriza bloquear al rival.
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @return un Point correspondiente a la casilla mas optima para lanzar una flecha.
     */
    private java.awt.Point shootArrow(GameStatus s){
        
        //Valores y variables que usaremos en cada desicion.
        //###########################
        java.awt.Point apuntada = null;
        java.awt.Point amazonActual = null;
        int numeroamazones=s.getNumberOfAmazonsForEachColor();
        int tauler=s.getSize();
        int amazonIndex = 0;
        double posx,posy;
        int buida=0;
        int bestAmazon=0;
        int minBuida=maxint;
        CellType jugadoractual;
        if(s.getCurrentPlayer()==CellType.PLAYER1)  jugadoractual=CellType.PLAYER2;
        else jugadoractual=CellType.PLAYER1;
        //###########################
        
        //Vemos que amazona tiene menos movimientos disponible, la guardamos como prioridad para disparar.
        //###########################
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
        //###########################
        
        //Escogemos una casilla alrededor de la amazona escogida para poner la flecha i bloquearla.
        //###########################
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
        //###########################
        
        //Si no hay ninguna disponible, escogemos una casilla aleatoria para poner la felcha
        //###########################
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
        //###########################
        
        return apuntada;
    }
    
    
    
    
    /**
     * Funcion min/max con poda alpha/beta y un limite de profundidad depth.
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @param alpha valor alpha de la poda.
     * @param beta valor beta de la poda.
     * @param depth profundidad actual, es mayor o igual a 0. 0 significa que estamos en lo mas profundo (corresponde a limite de profundidad - profundidad actual).
     * @return valor heristic de la ultima rama de profundidad/condiciones de salida.
     */
    private int AlphaBeta(GameStatus s, int alpha, int beta, int depthActual){
    if(timeOut){ //si estamos en timeOut, devolvemos el peor resultado posible para que no lo tenga en cuenta la mayor parte de veces.
        return minint;
    }
    numNodosExp++;          
    int best;    
    
    //Condicion de salida, devolvemos la heuristica del tablero.
    //###########################
    if (depthActual <= 0 || s.isGameOver() || s.getEmptyCellsCount() == 0){ 
        return heuristica(s);                               
    }
    //###########################
    
    //Escogemos la amazona con menos movimientos posibles, hemos desidido que para ganar hay que priorizar que no nos bloqueen a nuestras amazonas por lo que unicamente hacemos el subarbol de movimientos de esta.
    //###########################
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
    //###########################
    
    
    if (player == s.getCurrentPlayer()){  
        best = minint;  
        for (int i = 0; i<priorityAmazon.size(); i++){  
            
            GameStatus aux = new GameStatus(s);
            aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i));
            java.awt.Point apuntada = shootArrow(aux);
            aux.placeArrow(apuntada);

            
            //Calculamos la key aplicando el movimiento realizado antes de llamar a la recursividad y despues deshacemos el movimiento en la key para volver al tablero inicial.
            //###########################
            int color;
            if(s.getCurrentPlayer() == CellType.PLAYER1) color = 0;
            else color = 1;
            long oldPoss = table[(int)s.getAmazon(s.getCurrentPlayer(), amazonIndex).getX()][(int)s.getAmazon(s.getCurrentPlayer(), amazonIndex).getY()][color];
            long newPoss = table[(int)priorityAmazon.get(i).getX()][(int)priorityAmazon.get(i).getY()][color];
            long newArrow = table[(int)apuntada.getX()][(int)apuntada.getY()][2];
                    
            key ^=oldPoss;
            key ^=newPoss;
            key ^=newArrow;
                    
            best = Math.max(best, AlphaBeta(aux, alpha, beta, depthActual-1));  
            alpha = Math.max(alpha, best); 
                    
            key ^=oldPoss;
            key ^=newPoss;
            key ^=newArrow;
            //###########################
            
            if (alpha >= beta) break;       
        }
        return best;                            
    }
    else{      
        best = maxint;  
        for (int i = 0; i<priorityAmazon.size(); i++){    
            GameStatus aux = new GameStatus(s);
            aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), amazonIndex), priorityAmazon.get(i));
            java.awt.Point apuntada = shootArrow(aux);
            aux.placeArrow(apuntada);

            int color;
            if(s.getCurrentPlayer() == CellType.PLAYER1) color = 0;
            else color = 1;
            long oldPoss = table[(int)s.getAmazon(s.getCurrentPlayer(), amazonIndex).getX()][(int)s.getAmazon(s.getCurrentPlayer(), amazonIndex).getY()][color];
            long newPoss = table[(int)priorityAmazon.get(i).getX()][(int)priorityAmazon.get(i).getY()][color];
            long newArrow = table[(int)apuntada.getX()][(int)apuntada.getY()][2];
                    
            key ^=oldPoss;
            key ^=newPoss;
            key ^=newArrow;
                    
            best = Math.min(best, AlphaBeta(aux, alpha, beta, depthActual-1));  
            beta = Math.min(beta, best); 
            
            key ^=oldPoss;
            key ^=newPoss;
            key ^=newArrow;
     
            if (beta <= alpha) break;     
        }
        return best;                            
    } 
  } 
}


