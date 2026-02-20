import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);

        
        for (int i = 0; i < windowLength && !in.isEmpty(); i++) {
            window += in.readChar();
        }
        
        if (window.length() < windowLength) return;

        
        while (!in.isEmpty()) {
            
            c = in.readChar();

            
            List probs = CharDataMap.get(window);

            
            if (probs == null) {
                
                probs = new List();
                CharDataMap.put(window, probs);
            }

           
            probs.update(c);

            
            window = window.substring(1) + c;
        }

        
        for (List probs : CharDataMap.values()) {
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		if (probs == null || probs.getSize() == 0) return;

    
    int total = 0;
    ListIterator it1 = probs.listIterator(0);
    while (it1.hasNext()) {
        total += it1.next().count;
    }
    if (total == 0) return;

    
    double runningCp = 0.0;
    ListIterator it2 = probs.listIterator(0);
    CharData last = null;

    while (it2.hasNext()) {
        CharData cd = it2.next();
        cd.p = (double) cd.count / total;
        runningCp += cd.p;
        cd.cp = runningCp;
        last = cd;
    }

    
    if (last != null) last.cp = 1.0;
	}

    
	char getRandomChar(List probs) {
		if (probs == null || probs.getSize() == 0) return ' ';

        double r = randomGenerator.nextDouble();
        ListIterator it = probs.listIterator(0);

        while (it.hasNext()) {
        CharData cd = it.next();
        if (cd.cp > r) return cd.chr;
         }   

    
        return probs.get(probs.getSize() - 1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
    if (initialText == null) return "";
    if (textLength <= initialText.length()) {
        return initialText.substring(0, textLength);
    }
    if (initialText.length() < windowLength) return initialText;

    StringBuilder generated = new StringBuilder(initialText);

    while (generated.length() < textLength) {
        String window = generated.substring(generated.length() - windowLength);

        List probs = CharDataMap.get(window);
        if (probs == null) break;

        char next = getRandomChar(probs);
        generated.append(next);
    }

    return generated.toString();
    }   

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);

        
        lm.train(fileName);

        
        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
