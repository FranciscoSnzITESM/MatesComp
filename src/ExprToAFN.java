import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class ExprToAFN {
	private static int n;
	private static Character[] alphabet;
	private static LinkedList<LinkedList<Integer>[]> states;
	private static LinkedList<Integer> inits,finals,oneExitE,oneEntryE;
	public static void main(String[] args) {
		String expresion = JOptionPane.showInputDialog("Ingresa la expresion regular").replaceAll(" ", "");
		//String[] options = {"AFNE","AFN","AFD"};
		int option = 2;//JOptionPane.showOptionDialog(null, "De expresion a:","ExprTo:",
				//JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		//System.out.println(option);
		alphabet = getAlphabet(expresion);
		n = alphabet.length;
		states = new LinkedList<>();
		oneExitE = new LinkedList<>();
		oneEntryE = new LinkedList<>();
		addState();
		addState();
		System.out.println(expresion);
		exprToAFN(0, 1, expresion);
		inits = new LinkedList<>();
		finals = new LinkedList<>();
		inits.add(0);
		finals.add(1);
		if(option>0) {
			eliminaEsAFN();
			printAFN();
		}
		if(option>1) {
			AFNtoAFD.passAFN(states,alphabet,inits,finals);
		}
	}
	
	private static void exprToAFN(int start, int end, String str) {
		//System.out.println(str);
		// Elimina () si rodean por completo al str (str)
		str = eliminaBrackets(str);
		// Caso base
		if(str.length()==1) {
			LinkedList<Integer>[] current = states.get(start);
			for(int i=0;i<alphabet.length;i++) {
				if(alphabet[i]==str.charAt(0)) {
					if(current[i]==null) {
						current[i] = new LinkedList<Integer>();
					}
					current[i].add(end);
					break;
				}
			}
			if(!str.equals("$")) {
				oneExitE.set(start, -1);
				oneEntryE.set(end, -1);
			}
			else {
				int x = oneExitE.get(start);
				if(x == 0) oneExitE.set(start, end);
				else if(x>0) oneExitE.set(start, -1);
				
				x = oneEntryE.get(end);
				if(x == 0) oneEntryE.set(end, start);
				else if(x>0) oneEntryE.set(end, -1);
			}
			return;
		}
		// String donde adentro de parentesis este con espacios
		String cleanStr = getCleanStr(str);
		// Caso | (Union)
		if(cleanStr.contains("|")) {
			//System.out.println("union");
			exprToAFN(start,end,str.substring(0,cleanStr.indexOf('|')));
			exprToAFN(start,end,str.substring(cleanStr.indexOf('|')+1,str.length()));
			return;
		}
		// Caso concatenacion
		int x = 0;
		String reset = "| "; // Operaciones
		String ignorar = "+*";
		for(int i=0;i<cleanStr.length();i++) {
			// Si es operacion o cadena
			if(reset.contains(""+cleanStr.charAt(i))) x = 0;
			else if(!ignorar.contains(""+cleanStr.charAt(i))) x++;
			// Si hay concatenacion
			if(x==2) {
				//System.out.println("concatenacion");
				addState();
				x=states.size()-1;
				exprToAFN(start,x,str.substring(0,i));
				exprToAFN(x,end,str.substring(i,str.length()));
				return;
			}
		}
		// Caso cerradura positiva
		if(cleanStr.contains("+")) {
			//System.out.println("cerradura positiva");
			addState();
			x=states.size()-1;
			exprToAFN(start,x,str.substring(0,cleanStr.indexOf('+')));
			exprToAFN(x,x,str.substring(0,cleanStr.indexOf('+')));
			exprToAFN(x,end,"$");
			return;
		}
		// Caso cerradura
		if(cleanStr.contains("*")) {
			//System.out.println("cerradura");
			addState();
			x=states.size()-1;
			exprToAFN(start,x,"$");
			exprToAFN(x,x,str.substring(0,cleanStr.indexOf('*')));
			exprToAFN(x,end,"$");
			return;
		}
	}
	
	private static Character[] getAlphabet(String str) {
		char[] removes = {'(',')','*','+','|','$'};
		for(int i=0;i<removes.length;i++) {
			str = str.replace(removes[i], ' ');
		}
		str = str.replaceAll(" ", "");
		LinkedList<Character> c = new LinkedList<>();
		for(int i=0;i<str.length();i++) {
			boolean insert = true;
			Iterator<Character> cs = c.iterator();
			while(cs.hasNext()) {
				if(cs.next()==str.charAt(i)) {
					insert=false;
					break;
				}
			}
			if(insert) c.add(str.charAt(i));
		}
		c.add('$');
		return c.toArray(new Character[c.size()]);
	}
	
	private static String eliminaBrackets(String str) {
		// Se asegura que el bracket ( al inicio esta conectado con ) al final
		if(str.startsWith("(") && str.endsWith(")")) {
			// Profundidad de entre Brackets
			int pb=1;
			for(int i=1;i<str.length()-1;i++) {
				if(str.charAt(i)==('(')) pb++;
				else if(str.charAt(i)==(')')) pb--;
				if(pb==0) return str;
			}
			return str.substring(1,str.length()-1);
		}
		return str;
	}
	
	private static String getCleanStr(String str) {
		String cleanStr = "";
		int pb=0;
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i)==('(')) {
				pb++;
				cleanStr+=pb==1?'(':' ';
			}
			else if(str.charAt(i)==(')')) {
				pb--;
				cleanStr+=pb==0?')':' ';
			}
			else cleanStr+=pb==0?str.charAt(i):' ';
		}
		return cleanStr;
	}
	
	@SuppressWarnings("unchecked")
	private static void addState() {
		states.add(new LinkedList[n]);
		oneEntryE.add(0);
		oneExitE.add(0);
	}
	
	private static void eliminaEsAFN() {
		Iterator<Integer> iter = oneExitE.iterator();
		int q = 0;
		while(iter.hasNext()) {
			int x = iter.next();
			if(x>0 && states.get(q)!=null) {
				elimOneEExit(q,x);
			}
			q++;
		}
		iter = oneEntryE.iterator();
		q = 0;
		while(iter.hasNext()) {
			int x = iter.next();
			if(x>0 && states.get(q) != null) {
				elimOneEEntry(q,x);
			}
			q++;
		}
	}
	private static void elimOneEEntry(int q, int entry) {
		// Pon las salidas del estado a borrar que salgan de la entrada de $
		LinkedList<Integer>[] qSt = states.get(q);
		LinkedList<Integer>[] nSt = states.get(entry);
		nSt[nSt.length-1].remove((Integer)q);
		for(int i=0;i<qSt.length;i++) {
			if(qSt[i] != null) {
				if(nSt[i] == null) nSt[i] = new LinkedList<>();
				nSt[i].addAll(qSt[i]);
			}
		}
		// Elimina el nodo q y si era final hacer el nodo entry final
		states.set(q, null);
		if(finals.contains(q)) finals.set(finals.indexOf(q),entry);

	}
	
	private static void elimOneEExit(int q, int exit) {
		
		// Pon las entradas del estado a borrar apuntando a donde apuntaba la salida $
		Iterator<LinkedList<Integer>[]> iterator = states.iterator();
		while(iterator.hasNext()) {
			LinkedList<Integer>[] current = iterator.next();
			if(current != null) {
				for(int i=0;i<current.length;i++) {
					if(current[i] != null) {
						Iterator<Integer> n = current[i].iterator();
						int j=0;
						while(n.hasNext()) {
							if(n.next() == q) {
								current[i].set(j, exit);
							}
							j++;
						}
					}
				}
			}
		}
		// Elimina estado y checa si era inicial, si es registrar la salida de $ como inicial.
		states.set(q, null);
		if(inits.contains(q)) inits.set(inits.indexOf(q),exit);
	}
	
	private static void printAFN() {
		Iterator<LinkedList<Integer>[]> iterator = states.iterator();
		int q=0;
		while(iterator.hasNext()) {
			LinkedList<Integer>[] current = iterator.next();
			if(current != null) {
				if(inits.contains(q)) System.out.print(">");
				System.out.print("Q"+q);
				if(finals.contains(q)) System.out.print("*");
				System.out.print(": ");
				for(int i=0;i<current.length;i++) {
					if(current[i] == null) {
						System.out.print(alphabet[i]+"{}, ");
					}
					else {
						System.out.print(alphabet[i]+"{");
						Iterator<Integer> transitions = current[i].iterator();
						if(transitions.hasNext()) System.out.print(transitions.next());
						while(transitions.hasNext()) {
							System.out.print(", " + transitions.next());
						}
						System.out.print("}, ");
					}
				}
				System.out.println();
			}
			q++;
		}
		System.out.println();
	}
}

