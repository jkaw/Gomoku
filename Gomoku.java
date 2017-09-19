import java.awt.Point;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
public class Gomoku {

	private static final int GOMOKUPORT = 17033; 
	private Socket gridSocket;				// socket for communicating w/ server
	private PrintWriter gridOut;                        // takes care of output stream for sockets
	private BufferedReader gridIn;
	private String myID;
	private String Status;
	private String[][] Board;
	private int Size;
	private int turn = 0;
	private String Me = "";
	private ArrayList<String> myBoard = new ArrayList();
	private String[] rowsEvaluation;
	private ArrayList<Point> Moves = new ArrayList();
	
	
	public Gomoku(String h, int p) throws IOException{
		
		gridSocket = new Socket(h, p);
		gridOut = new PrintWriter(gridSocket.getOutputStream(), true); 
		gridIn = new BufferedReader(new InputStreamReader(gridSocket.getInputStream()));
		
	
		
		
	}
	

	 public int evaluatePlayer(String playing, String[][] board){

	        int score = 0;

	        // actual ROWS
	        rowsEvaluation = new String[Size];
	        for(int i = 0; i < Size; i++) {
	            for(int j =0; j < Size; j++)
	                translateCellToStr(playing, i, board[i][j]);
	            // System.out.println(rowsEvaluation[i] + " ");
	            score += evaluateRow(rowsEvaluation[i]);
	        }


	        // COLOUMNS
	        rowsEvaluation = new String[Size];
	        for(int i = 0; i < Size; i++) {
	            for(int j =0; j < Size; j++)
	                translateCellToStr(playing, i, board[j][i]);
	            // System.out.println(rowsEvaluation[i] + " ");
	            score += evaluateRow(rowsEvaluation[i]);
	        }

	        // DIAGONAL LEFT TOP TO RIGHT DOWN

	        rowsEvaluation = new String[Size];    // left edge
	        for(int i = 0; i < Size / 2; i++) {
	            int h = i;
	            for(int j = 0; j < Size - i; j++, h++)
	                translateCellToStr(playing, i, board[h][j]);
	            score += evaluateRow(rowsEvaluation[i]);
	        }

	        rowsEvaluation = new String[Size];    // top edge (1st part)
	        for(int i = 1; i < Size / 2; i++) {
	            int h = i;
	            for(int j = 0; j < Size - i ; j++, h++)
	                translateCellToStr(playing, i, board[j][h]);
	            // System.out.println(rowsEvaluation[i] + " ");
	            score += evaluateRow(rowsEvaluation[i]);
	        }


	        // DIAGONAL RIGHT TOP TO LEFT DOWN

	        rowsEvaluation = new String[Size];    // top edge (2nd part)
	        for(int i = 0; i < Size / 2; i++) {
	            int h = 0;
	            for(int j = 7 - i; j >= 0; j--, h++)
	                translateCellToStr(playing, i, board[h][j]);
	            // System.out.println(rowsEvaluation[i] + " ");
	            score += evaluateRow(rowsEvaluation[i]);
	        }

	        rowsEvaluation = new String[Size];    // right edge
	        for(int i = 1; i < Size / 2; i++) {
	            int h = i;
	            for(int j = Size - 1; j > i - 1; j--, h++)
	                translateCellToStr(playing, i, board[h][j]);
	            //System.out.println(rowsEvaluation[i] + " ");
	            score += evaluateRow(rowsEvaluation[i]);
	        }

	        return score;
	    }

	    
	 private void translateCellToStr(String colorToEvaluate, int i, String cell){
	        if(cell == colorToEvaluate)
	            rowsEvaluation[i] += Me; // no reason for being G, just a char to identify the current user
	        else if(cell == null)
	            rowsEvaluation[i] += '_';
	        else
	            rowsEvaluation[i] += "S"; // literally can be anything (doesnt matter)
	    }
	 
	 private int evaluateRow(String row){
	        int score = 0;

	        // X5

	        // max scores for most dangerous situations
			if (row.indexOf(Me + Me + Me + Me + Me) >= 0)
				return 1000;
	        // less dangeours


	        // x4
	        if (row.indexOf("_"+ Me + Me + Me +"_") >= 0)
	            score += 500;

	        if (row.indexOf(Me + Me + Me +"_" + Me) >= 0)
				score += 100;
	        if (row.indexOf(Me + Me + "_" + Me + Me) >= 0)
				score += 100;
	        if (row.indexOf(Me + "_" + Me + Me + Me) >= 0)
				score += 100;
	        if (row.indexOf("_" + Me + Me + Me + Me) >= 0)
				score += 100;
			if (row.indexOf(Me + Me + Me + Me + "_") >= 0)
				score += 100;

	        // x3
	        if (row.indexOf("_" + Me + Me +"_" + Me +"_") >= 0)
				score += 10;
			if (row.indexOf("_" + Me + "_" + Me + Me +"_") >= 0)
				score += 10;
			if (row.indexOf("_" + Me + Me + Me + "_") >= 0)
				score += 10;
	        if (row.indexOf(Me + Me + Me + "_") >= 0)
				score += 10;
	        if (row.indexOf("_" + Me + Me + Me) >= 0)
	            score += 10;

	        // x2
			if (row.indexOf("_"+ Me +"_" + Me + "_") >= 0)
				score += 2;
	        if (row.indexOf("_" + Me + Me +"_") >= 0)
				score += 2;
			if (row.indexOf("_" + Me + Me) >= 0)
				score += 1;
	        if (row.indexOf(Me + Me +"_") >= 0)
				score += 1;

			return score;
	    }
	 
	 
	 public String changeplayer(String s){
		 if (s.equals("x")){
			 return "o";
			 
		 }
		 
		 else return "x";
	 }
	
	 private int evaluate(String[][] board){
	        return evaluatePlayer(Me, board) - evaluatePlayer(changeplayer(Me), board);
	    }
	 
	 
	 public int minimax(String[][] board,int depth, String player, int alpha, int beta){
	        int score;
	        String[][] tempBoard = new String[Size][Size];
	        ArrayList<Point> moves = Moves;

	        if(moves.size() == 0|| depth == 0){
	            int evaluation = evaluate(board);
	            score = evaluation;
	            return score;
	        }

	        for(Point move : Moves) {
	            tempBoard = Board;
	            tempBoard[move.x][move.y] = player;
	            score = minimax(tempBoard,depth -1 , changeplayer(player), alpha, beta);
	            if(player == Me) { // MAX
	                if (score > alpha)
	                    alpha = score;
	                if (alpha >= beta)
	                    return alpha;
	                return alpha;
	            } else {    // MIN
	                if (score < beta)
	                    beta = score;
	                if (beta <= alpha)
	                    return beta;
	                return beta;
	            }
	        }
	        if (player == Me)
	           return alpha;
	        return beta;
	    }
	 
	 
	 public boolean haveAdjacentNodes(String[][] board, int i, int j){
	        for(int x=-1; x <= 1; x++) {
	            for(int y=-1; y <= 1; y++){
	                try{
	                    if(board[i + x][j + y] != null) {
	                        return true;
	                    }
	                } catch(ArrayIndexOutOfBoundsException e){
	                }
	            }
	        }
	        return false;
	    }
	 
	 public void chooseMove(String[][] board, String myColor) throws IOException{
	       
	        Point bestMove = null;
	        //MAX_Color = myColour;
	        // System.out.println("myColour MAX : " String.toString(myColour;
	        int bestValue = -10000;

	        for (Point move : Moves) {
	            String[][] tempBoard = Board;
	            tempBoard[move.x][move.y] = myColor;
	            int score =  minimax(tempBoard,30 ,  myColor, Integer.MIN_VALUE, Integer.MAX_VALUE);

	            if (score > bestValue) {
	                bestValue = score;
	                //System.out.println("so far best is : " + bestValue);
	                bestMove = new Point(move.x, move.y);
	            }
	        }

	        if(bestMove == null )
	            Randmove();
	        gridOut.println(String.valueOf(bestMove.x) + " " + String.valueOf(bestMove.y)); 
	    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void moves(String x, ArrayList<Point> m){
		m.clear();
		for (int i = 0; i < Size; i ++){
			for (int j = 0; j < Size; j ++){
				if (Board[i][j].equals(x)){
					m.add(new Point(i,j));
				}
			}
		}
	}
	
	
	
	
	public boolean isLine(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	    
	    	if(Character.isLetter('x') || Character.isLetter('o')){
	    		return false;
	    	}
	    	
	    	else if(!Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}
	
	public boolean isAlpha(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	    
	    	
	    if(!Character.isLetter(c)) {
	            
	    	System.out.println(name);
	    	return false;
	            
	        }
	    }

	    return true;
	}
	
	
	
	
	public void checkStatus() throws IOException {
		if (Status.equals("continuing")) {
			//System.out.println("does it get here");
			
				
			
			chooseMove(Board, Me);
		}
		else if (Status.equals("win")) {
			System.out.println("Yay! I win!");
			System.exit(1);
		}
		else if (Status.equals("lose")){
			System.out.println("Aww, I lost");
			System.exit(1);
		}
		else if (Status.equals("draw")) {
			System.out.println("Good game. We're evenly matched!");
			System.exit(1);
		}
		else if (Status.equals("forfeit-move")){
			System.out.println("I made an illegal move");
			System.exit(1);
		}
		else if (Status.equals("forfeit-time")) {
			System.out.println("I took too long to make a move");
			System.exit(1);
		}
		else {
			System.out.println(Status + "n");
			System.out.println("I don't recognize that game status");
			System.exit(1);
		}
		
		
	}
	
	
	
	public void Randmove() throws IOException {
		Random r = new Random();
		int a = r.nextInt(Size);
		int b = r.nextInt(Size);
		
			if (checkMove(a, b) == false){
			Randmove();
			
			}
			else 
		
		gridOut.println(String.valueOf(a) + " " + String.valueOf(b));
		
	}
	
	public void openmoves(){
		Moves.clear();
		for (int i = 0; i < Size; i ++){
			for (int j = 0; j < Size; j ++){
				if (Board[i][j].equals("x") || Board[i][j].equals("o"));
				else if (haveAdjacentNodes(Board, i, j)) Moves.add(new Point(i, j));
			}
		}
	}
	

	
	
	public void converttwod() {
		Board = new String[Size][Size];
		for (int i = 0; i < Size; i ++){
			for (int j = 0; j < Size; j ++){
				Board[i][j] = myBoard.get(i).substring(j, j +1);
			}
		}
		
		
	}
	
	
	
	
	
	public void readin() throws IOException{
		myBoard.clear();
		for (int i = 0; i < Size; i ++){
			myBoard.add(gridIn.readLine());
		}
		gridIn.readLine();
	}
	
	
	public boolean checkMove(int a, int b) {
		if (Board[a][b].equals("x") || Board[a][b].equals("o")){
			return false;
		}
		
		else return true;
		
	}
	

	
	
	
	
	public void readboard() throws IOException{
		Random r = new Random();
		Status = gridIn.readLine();
		String rowone;
		System.out.println(Status);
		if (turn == 0) {
			//determineSize();
			rowone = gridIn.readLine();
			Size = rowone.length();
			System.out.println(Size);
			myBoard.add(rowone);
			for (int i = 0; i < Size -1; i ++){
				myBoard.add(gridIn.readLine());
			}
			System.out.println(Size);
			Me = gridIn.readLine();
			
			//Board = new String[Size][Size];
			//Me = gridIn.readLine();
			//System.out.println(Me);
			gridOut.println(String.valueOf(r.nextInt(Size)) + " " + String.valueOf(r.nextInt(Size)));;
			converttwod();
			turn += 1;
		}
		else {
		readin();
		converttwod();
		//mypop();
		//twoconnected(MyPlay);
		//connectiondoubles();
		//System.out.println(Connections);
		openmoves();
		//System.out.println("does it get here");
		checkStatus();
		}
		
	}
	
	
	
	public void run() throws IOException{
		while (true){	
		readboard();
		
		}
	}
	
    
    
    
    
    public static void main(String[] args) throws IOException{
    	Gomoku me = new Gomoku("localhost", GOMOKUPORT);
    	me.run();
    }
	
	
}



	
	
	
	
	
	
	
	
	
	
	
	
	

