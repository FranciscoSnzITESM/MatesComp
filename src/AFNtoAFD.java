import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class AFNtoAFD {
	private static Character[] nfaAlphabet, dfaAlphabet;
	private static LinkedList<LinkedList<Integer>[]> nfaStates;
	private static LinkedList<Integer[]> dfaStates;
	private static LinkedList<LinkedList<LinkedList<Integer>>> eClosures;
	private static LinkedList<Integer> nfaInits, nfaFinals, dfaFinals;
	public static void passAFN( LinkedList<LinkedList<Integer>[]> s, Character[] a,
								LinkedList<Integer> i, LinkedList<Integer> f) {
		nfaStates = s;
		nfaAlphabet = a;
		dfaAlphabet = Arrays.copyOf(a, a.length-1);
		nfaInits = i;
		nfaFinals = f;
		dfaFinals = new LinkedList<>();
		eClosures = new LinkedList<>();
		dfaStates = new LinkedList<>();
		afntoafd();
		emptyState();
		printAFD();
	}
	private static void afntoafd() {
		eClosure(nfaInits);
		for(int i=0;i<eClosures.size();i++) {
			dfaStates.add(new Integer[dfaAlphabet.length]);
			dfaState(i);
		}
	}
	private static void dfaState(int q) {
		Integer[] trans = dfaStates.getLast();
		for(int i=0;i<dfaAlphabet.length;i++) {
			int transQ = letterPath(q,i);
			if(transQ>=0) trans[i]= transQ;
		}
	}
	// ln = letter number
	private static int letterPath(int q, int ln) {
		LinkedList<Integer> newQ = new LinkedList<>();
		LinkedList<Integer> eC = eClosures.get(q).getFirst();
		for(int i=0;i<nfaStates.size();i++) {
			LinkedList<Integer>[] state = nfaStates.get(i);
			if(state != null && eC.contains(i)) {
				LinkedList<Integer> trans = state[ln];
				if(trans != null) {
					Iterator<Integer> tIter = trans.iterator();
					while(tIter.hasNext()) {
						int t = tIter.next();
						if(!newQ.contains(t)) {
							newQ.add(t);
						}
					}
				}
			}
		}
		int i = eClosure(newQ);
		return i;
	}
	
	private static int eClosure(LinkedList<Integer> q) {
		// If there arent any paths to check
		if(q.size()==0) {
			//System.out.println("no pahts");
			return -1;
		}
		// Check if e-closure of q already calculated
		Iterator<LinkedList<LinkedList<Integer>>> esIter = eClosures.iterator();
		int i=0;
		while(esIter.hasNext()) {
			Iterator<LinkedList<Integer>> eIter = esIter.next().iterator();
			// Skip the first one as that is the result
			eIter.next();
			while(eIter.hasNext()) {
				if(eIter.next().equals(q)) {
					//System.out.println("eclosure proccesed before");
					return i;
				}
			}
			i++;
		}
		// Calculate e-closure of q
		@SuppressWarnings("unchecked")
		LinkedList<Integer> ec = checkEs((LinkedList<Integer>) q.clone(), new LinkedList<>());
		
		// Check if result repeated, if not create new state
		esIter = eClosures.iterator();
		i=0;
		while(esIter.hasNext()) {
			LinkedList<LinkedList<Integer>> es = esIter.next();
			LinkedList<Integer> er = es.getFirst();
			if(er.equals(ec)) {
				//System.out.println("eclosure result repeated");
				es.add(ec);
				return i;
			}
			i++;
		}
		//System.out.println("New Closure");
		// Create new E-closure State
		eClosures.add(new LinkedList<>());
		eClosures.getLast().add(q);
		eClosures.getLast().addFirst(ec);
		// Check if final state inside
		Iterator<Integer> finals = nfaFinals.iterator();
		while(finals.hasNext()) {
			if(ec.contains(finals.next())) {
				dfaFinals.add(eClosures.size()-1);
			}
		}
		return eClosures.size()-1;
	}
	private static LinkedList<Integer> checkEs(LinkedList<Integer> ec, LinkedList<Integer> checkedECs) {
		while(ec.size()>0) {
			int checkE = ec.getFirst();
			if(!checkedECs.contains(checkE)) {
				checkedECs.add(checkE);
				LinkedList<Integer> es = nfaStates.get(checkE)[nfaAlphabet.length-1];
				if(es!=null) {
					Iterator<Integer> eIter = es.iterator();
					while(eIter.hasNext()) {
						int e = eIter.next();
						if(!ec.contains(e) && !checkedECs.contains(e)) {
							ec.add(e);	
						}
					}
				}
			}
			ec.removeFirst();
		}
		return checkedECs;
	}
	
	private static void emptyState() {
		// First check if emptyState needed
		boolean needed = false;
		Iterator<Integer[]> sIter = dfaStates.iterator();
		int emptyN = dfaStates.size();
		while(sIter.hasNext()) {
			Integer[] n = sIter.next();
			for(int i=0;i<n.length;i++) {
				if(n[i]==null) {
					needed = true;
					n[i] = emptyN;
				}
			}
		}
		if(needed) {
			Integer[] values = new Integer[dfaAlphabet.length];
			for(int i=0;i<values.length;i++) {
				values[i] = emptyN;
			}
			dfaStates.add(values);
		}
	}
	
	private static void printAFD() {
		// Print e Closures
		Iterator<LinkedList<LinkedList<Integer>>> i1 = eClosures.iterator();
		int q=0;
		while(i1.hasNext()) {
			System.out.print("Q"+q+": ");
			q++;
			Iterator<LinkedList<Integer>> i2 = i1.next().iterator();
			while(i2.hasNext()) {
				Iterator<Integer> i3 = i2.next().iterator();
				System.out.print("<");
				while(i3.hasNext()) {
					System.out.print(i3.next()+", ");
				}
				System.out.print(">");
			}
			System.out.println();
		}
		System.out.println();
		
		// Print DFA
		Iterator<Integer[]> sIter = dfaStates.iterator();
		q = 0;
		System.out.print(">");
		while(sIter.hasNext()) {
			System.out.print("Q"+q);
			if(dfaFinals.contains(q)) System.out.print("*");
			System.out.print(": ");
			Integer[] n = sIter.next();
			for(int i=0;i<n.length;i++) {
				System.out.print(dfaAlphabet[i]+"["+n[i]+"]");
			}
			System.out.println();
			q++;
		}
		System.out.println();

		// GraphViz Print
		try{
			PrintWriter pw = new PrintWriter(
				new FileWriter("C:\\Users\\hss_f\\OneDrive\\Documents\\eclipseWorkspace\\MatesComputacionales\\graph.dot"));
			pw.println("digraph G {");
			pw.println("start -> Q0");
			pw.println("start [shape=Mdiamond]");
			sIter = dfaStates.iterator();
			q = 0;
			while(sIter.hasNext()) {
				if(dfaFinals.contains(q)) pw.println("Q"+q+" [shape=Msquare];");
				Integer[] n = sIter.next();
				for(int i=0;i<n.length;i++) {
					pw.println("Q"+q+" -> Q"+n[i]+"[label=\""+dfaAlphabet[i]+"\"];");
				}
				q++;
			}
			pw.println("}");
			pw.close();
		}
		catch (Exception e){
			System.out.println(e);
		}
	}
}
