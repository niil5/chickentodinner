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
    private String name;    //donde guardamos el nombre
    private GameStatus s;   //donde guardamos el game status inicial
    private int maxDepth = 4;   //maximo de profundidad por defecto
    private long numNodosExp = 0;   //numero de nodosExplorados, lo incrementamos y reseteamos en cada movimiento
    private int maxint = Integer.MAX_VALUE; //valor maximo que puede asignarse a un entero
    private int minint = Integer.MIN_VALUE; //valor minimo que puede asignarse a un entero
    private CellType player;    //donde guardamos nuestro jugador
    private CellType enemy;     //donde guardmaos el jugador enemigo
    
    /**
     * Crea un nuevo jugador con nombre = name y un limite de profundidad por defecto de 4.
     * @param name 
     */
    public chickentodinner(String name) {
        this.name = name;
        this.maxDepth = 4;
    }
    
    /**
     * Crea un nuevo jugador con nombre = name y un limite de profundidad = depth.
     * @param name
     * @param depth 
     */
    public chickentodinner(String name, int depth) {
        this.name = name;
        this.maxDepth = depth;
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
     * no se usa en esta version, necesaria al ser una clase que implementa IAuto e IPlayer.
     */
    @Override
    public void timeout() {
        
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
     * Funcion para escoger el mejor movimiento a partir de un GameStatus pasado por parametro.  
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @return Devuelve el siguiente movimiento que nuestro jugador quiere hacer.
     */
    public Move move(GameStatus s) {
        
        //Guardamos valores por defecto y reiniciamos parametros que usamos en cada movimiento.
        //###########################
        player = s.getCurrentPlayer(); 
        if(player == CellType.PLAYER1) enemy = CellType.PLAYER2;
        else enemy = CellType.PLAYER1;
        this.s = s;
        numNodosExp = 0;
        int alpha = minint;     
        int beta = maxint; 
        int best = minint;
        int bestAmazon = 0;
        Point bestMove = null;
        //###########################
        
        //Calculamos un valor heuristico para cada movimiento posible de cada amazona, lo comparamos y nos quedamos con el mejor movimiento posible. 
        //Tambien hacemos un "trim" de la busqueda aumentando la ventana alpha.
        //###########################
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
                    bestAmazon = i;
                    best = ab;                 
                }                       
                alpha = Math.max(alpha, best);
            }         
        }
        //###########################
        
        //Comprobason que exista mejor movimiento, si no existe ponemos flecha para que acabe el juego(perdemos en teoria)
        //Si existe mejor movimiento, devolvemos el mejor movimiento posible, junto a la profundidad maxima y el numero de nodos explorados.
        //###########################
        if(bestMove == null){
          return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), s.getAmazon(s.getCurrentPlayer(), bestAmazon), shootArrow(s), (int)numNodosExp, maxDepth, SearchType.MINIMAX);  
        }
        GameStatus aux = new GameStatus(s);
        aux.moveAmazon(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove);
        return new Move(s.getAmazon(s.getCurrentPlayer(), bestAmazon), bestMove, shootArrow(aux), (int)numNodosExp, maxDepth, SearchType.MINIMAX);
        //###########################
    }
    
    /**
     * Funcion para calcular la Heuristica general de un GameStatus. 
     * @param s es el GameStatus del cual obtenemos datos para los calculos.
     * @return Devuelve un entero correspondiente a la heuristica del tablero para nuestro jugador - el enemigo.
     */
    private int heuristica(GameStatus s){
        return getHeuristica(s,player)-getHeuristica(s,enemy); //restamos el valor del enemigo a nuestro jugador par aun tablero espesifico y asi saber el valor general de la partida
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
    private int AlphaBeta(GameStatus s, int alpha, int beta, int depth){
    numNodosExp++;         //aumentamos el numero de nodos recorridos. 
    int best;               
    
    //Condicion de salida, devolvemos la heuristica del tablero.
    //###########################
    if (depth <= 0 || s.isGameOver() || s.getEmptyCellsCount() == 0){ 
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
    
    
    
    //Min-Max con poda alpha-beta
    //###########################
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
    //###########################
  } 
}

