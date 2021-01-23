package com.nbkelly.aux;

import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.io.File;
import java.nio.file.Files;

/*
 * print(x)
 * printf(x, args)
 * println(x)
 * 
 * DEBUG(level, x)
 * DEBUG(x) -> DEBUG(1, x)
 * DEBUGF(level, x, args) -> DEBUG(level, f(x, args))
 * DEBUG() -> DEBUG("")
 */
public abstract class Drafter {
    /** Does this session support/enable color? */
    private boolean _COLOR_ENABLED = false;
    private boolean _COLOR_CHECKED = false;
    private boolean _COLOR_HARD_DISABLED = false;

    /** some handy keywords */
    protected final boolean MANDATORY = true;
    protected final boolean OPTIONAL  = false;
    
    /** used in argument processing */    
    private final int _ARGUMENT_MATCH_FAILED = -1;

    /** list of all commands */
    private Command[] _commands = new Command[0];

    /** page mode */
    protected boolean _PAGE_ENABLED = false;
    protected boolean _PAGE_OPTIONAL = true;
    
    /** debug level */
    protected int _DEBUG_LEVEL = 0;
    protected boolean _DEBUG = false; //true if _DEBUG_LEVEL > 0

    /** read input */
    private int LINE = 0;
    private int TOKEN = 0;
    private Scanner _line = null;
    private Scanner _input = null;
    private String _currentLine = null;
    private ArrayList<String> _paged = new ArrayList<String>();
    
    /*
     * for reference:
     *   LINE   -> line number
     *   TOKEN  -> token number
     *   _line  -> line scanner
     *   _input -> input scanner
     *   _paged -> paged input lines
     *   _currentLine - > current line
     *   _PAGE_ENABLED : page mode enabled
     *   _NEXT_GREEDY  : next churns through lines
     */

    protected int GET_DEBUG_LEVEL() {
	return _DEBUG_LEVEL;
    }
    
    /** performs check-once analysis to enable colors */
    private boolean _COLOR_ENABLED() {
	if(_COLOR_HARD_DISABLED)
	    return false;

	else if (!_COLOR_CHECKED) {
	    //this supposedly only works on linux - no clue what the fuck to do on windows
	    //if(System.console() != null && System.getenv().get("TERM") != null)
		_COLOR_ENABLED = true;
	    _COLOR_CHECKED = true;
	}
	
	return _COLOR_ENABLED;
    }
    
    private Drafter self = null;
    
    //THINGS THAT NEED TO BE OVERRIDDEN
    protected abstract void actOnCommands(); // { };

    protected abstract Command[] setCommands(); /* {
	return new Command[0];
	}*/

    protected abstract int solveProblem() throws Exception; /* {
	Timer t = new Timer(_DEBUG_LEVEL > 0);
	println("Normal line");
	print("A");
	print("B");
	println(a2s(new char[]{'C', 'D'}));
	//'C');

	DEBUG("PLAIN DEBUG");
	DEBUG(0, "DEBUG LEVEL 0");
	DEBUG(1, "DEBUG LEVEL 1");
	DEBUG(2, "DEBUG LEVEL 2");
	DEBUGF(3, "DEBUG LEVEL %d%n", 3);
	DEBUG(4, "DEBUG LEVEL 4");
	DEBUG(5, "DEBUG LEVEL 5");
	DEBUG(6, "DEBUG LEVEL 6");

	println(t.splitf("Solve Time: %fs"));
	
	//println("Has Next: " + hasNext());
	println("First Line: " + nextLine());
	println("Has Next: " + hasNext());
	println(next());
	println(next());
	while(hasNextLine())
	    println(nextLine());

	println("Here's the results of our page:");
	for(String s: _paged)
	    printf("paged : '%s'%n", s);
	    }*/



    public Drafter() {
	self = this;
	_input = new Scanner(System.in);
    }
    
    
    // TODO: get rid of this
    /*public static void main(String[] argv) {
	//first we set up the commands	
	new ConceptHelperV2().run(argv); 
	}*/

    /**
     * Runs the given program.
     * <p>
     * More specifically, we: <br>
     * 1) Set all default commands <br>
     * 2) *Add any additional user-based commands
     * 3) Process all commands
     * 4) Act based on the results of the default commands
     * 5) *Act on the results of all user-input commands
     * 6) Perform any pre-processing on the input
     * 7) *Run the 'solveProblem' function. This is the actual user code.
     *
     * @param argv the argument vector for the program
     * @since 1.0
     */
    protected void run(String[] argv) {
	//add in any commands the user wants to add
	addCommands(setCommands());
	//then we set the default commands
	addCommands(defaultCommands());	
	//process all of the arguments
	argv = processCommands(argv);
	//act on the deafult commands
	actOnDefaultCommands();
	//act on the user commands
	actOnCommands();
	//run the program
	doSolveProblem();
    }

    private void doSolveProblem() {
	try {
	    int res = solveProblem();
	    if(res != 0) {
		ERR(String.format("solveProblem() failed at line %s token %s with code %d",
				  LINE, TOKEN, res));
		if(_currentLine != null)
		    ERR("Current Line: >" + _currentLine);
		FAIL(1);
	    }
	} catch (Exception e) {
	    ERR(String.format("solveProblem() failed at line %s token %s with exception %s:%n%s",
			      LINE, TOKEN, e.toString(), arrayToString(e.getStackTrace(), "\n")));
	    if(_currentLine != null)
		ERR("Current Line: " + _currentLine);
	    FAIL(1);		
	}
    }

    private Command[] defaultCommands() {	
	if(_PAGE_OPTIONAL)
	    return new Command[] {_debugLevel, _page, _help, _disableColors, _ignore};
	else
	    return new Command[] {_debugLevel, _help, _disableColors, _ignore};	
    }    
    
    private void addCommands(Command[] c) {
	Command[] res = new Command[_commands.length + c.length];

	for(int i = 0; i < _commands.length; i++)
	    res[i] = _commands[i];

	for(int j = 0; j < c.length; j++)
	    res[j + _commands.length] = c[j];

	_commands = res;
    }

    /**
     * Process argument vector based on command set
     * @param argv input arguments 
     * @return any valid unprocessed arguments
     */
    private String[] processCommands(String[] argv) {
	int index = 0;
	int index_last = -1;

	outer:
	while(index != index_last && index < argv.length) {
	    index_last = index;
	    for(int i = 0; i < _commands.length; i++) {
		int new_ind = _commands[i].match(argv, index);
		if(new_ind > 0) { //matched rule
		    index = new_ind;

		    //if that was terminal, we must break
		    if(_commands[i].isTerminal())
			break outer;
		    
		    continue outer;
		}
		else if (new_ind == _ARGUMENT_MATCH_FAILED) {
		    FAIL(_commands, 1, true);
		}
	    }
	}

	int unprocessed_args = argv.length - index;

	if(_help.matched())
	    FAIL(_commands, 0, false);
	if(unprocessed_args != 0 && !_ignore.matched()) {
	    //we have a number of unprocessed arguments
	    PRINT_ERROR_TEXT("ERROR: a number of arguments were not matched by any rule (index = " + index + ")");
	    System.err.println("Unmatched arguments: " + arrayToString(_REMAINING_ARGUMENTS(argv, index)));
	    FAIL(1);
	}	
	if(!arguments_satisfied(_commands))
	    FAIL(_commands, 1, true);

	return _REMAINING_ARGUMENTS(argv, index);
    }

    /**
     * Checks that all given arguments with mandatory principles are satisfied
     *
     * @param commands the set of all commands
     * @return True is all commands are valid, false otherwise
     * @since 1.0
     */
    private boolean arguments_satisfied(Command[] commands) {
	for(int i =0; i < commands.length; i++)
	    if((commands[i].mandatory && !commands[i].matched()) || !commands[i].valid())
		return false;

	return true;
    }

    /**
     * Prints text to the stderror using red and bold     
     * @param str message to print
     */
    private void PRINT_ERROR_TEXT(String str) {
	System.err.println(Color.colorize(_COLOR_ENABLED(), str, Color.RED_BOLD));
    }

    /**
     * Fails with the given exit code
     */
    protected void FAIL(int exit) {
	System.exit(exit);
    }

    /**
     * Prints out the usage for all of the commands, and then gracefully exists with given status code
     *
     * @param commands the set of all commands
     * @return exits the current program
     * @since 1.0
     */
    private void FAIL(Command[] commands, int exit, boolean error_only) {
	if(!error_only)
	    //display the usage of each command
	    for(int i = 0; i < commands.length; i++)		
		System.err.println(commands[i].usage(_COLOR_ENABLED()));
	else
	    //display the usage of each command with an error
	    for(int i = 0; i < commands.length; i++)
		if(commands[i].invalid())
		    System.err.println(commands[i].usage(_COLOR_ENABLED()));
	
	System.exit(exit);
    }

    /**
     * Flattens an array into a string
     * @param arr Array to flatten
     * @return String representation of array
     */
    private <T> String arrayToString(T[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i].toString() + " ");

	return res.toString() + "]";
    }

    private <T> String arrayToString(T[] arr, String delim) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    if(i != arr.length - 1)
		res.append(arr[i].toString() + delim);
	    else
		res.append(arr[i].toString());

	return res.toString() + "]";
    }

    
    private String arrayToString(int[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(long[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(float[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(double[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(char[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(short[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(byte[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    private String arrayToString(boolean[] arr) {
	var res = new StringBuilder("[ ");
	for(int i = 0; i < arr.length; i++)
	    res.append(arr[i] + " ");

	return res.toString() + "]";
    }

    /**
     * Cuts the input vector to a smaller size
     * @param arr the input vector
     * @param cutAt index to cut the vector at
     * @return cut argvector
     */
    private String[] _REMAINING_ARGUMENTS(String[] arr, int cutAt) {
	if(cutAt == arr.length)
	    return new String[0];
	if(cutAt == 0)
	    return arr;
	
	return Arrays.copyOfRange(arr, cutAt, arr.length);
    }

    public ArrayList<String> readFileLines(File fileToCopy) {
	//check the file exists
	if(!fileToCopy.exists()) {
	    ERR(String.format("The file %s, which should exist, does not!", fileToCopy));
	    return null;
	}
	
	//check the file is readable
	//check the file exists
	if(!Files.isReadable(fileToCopy.toPath())) {
	    ERR(String.format("The file %s, which does exist, is not readable!", fileToCopy));
	    return null;
	}
	
	ArrayList<String> outputFileLines = new ArrayList<String>();
	
	try {
	    DEBUG(1, "Reading File: " + fileToCopy);
	    outputFileLines = new ArrayList<String>(Files.readAllLines(fileToCopy.toPath()));
	} catch (Exception e) {
	    ERR(String.format("Failure when reading from file %s", fileToCopy));
	    ERR(e.toString());
	    return null;
	}

	return outputFileLines;
    }    
    
    /*********************************************************
     *
     *                     INPUT COMMANDS
     *
     *********************************************************/
    /*
     * for reference:
     *   LINE   -> line number
     *   TOKEN  -> token number
     *   _line  -> line scanner
     *   _input -> input scanner
     *   _paged -> paged input lines
     *   _currentLine - > current line
     *   _PAGE_ENABLED
     *
     *   Note that, on initiation, _input will be set to the system scanner
     *
     *   implemented:
     *     currentLine() - > gets the whole current line
     *     isEmptyLine() - > checks the current line exists and has size 0
     *     hasNextLine() - > checks that another line exists
     *     nextLine()    - > returns the remainder of the current line, and cycles to the next one
     *     hasNext()     - > is there another token on this line
     *     next()        - > return the next token on this line
     *
     *     hasNextInt()         - > Is the next token on this line (if it exists) an integer 
     *     nextInt()            - > Returns the next integer on this line, or null
     *     
     *     hasNextDouble()      - > Is the next token on this line (if it exists) a double 
     *     nextDouble()         - > Returns the next double on this line, or null
     *     
     *     hasNextLong()        - > Is the next token on this line (if it exists) a long 
     *     nextLong()           - > Returns the next long on this line, or null
     *     
     *     hasNextBigInteger()  - > Is the next token on this line (if it exists) a BigInteger 
     *     nextBigInteger()     - > Returns the next BigInteger on this line, or null
     *     
     *     hasNextBigDecimal()  - > Is the next token on this line (if it exists) a BigDecimal 
     *     nextBigDecimal()     - > Returns the next BigDecimal on this line, or null
     *     
     *     lineNumber()  - > return the current line number
     *     tokenNumber() - > return the current token number
     *     getPage(line) - > returns the paged value from line if possible, otherwise null
     *
     *     makeTimer ()  - > timer
     */

    protected Timer makeTimer() {
	return new Timer(_DEBUG_LEVEL > 0);
    }
    
    public int lineNumber() {
	return LINE;
    }

    public int tokenNumber() {
	return TOKEN;
    }

    public String getPage(int number) throws IllegalArgumentException {
	if(!_PAGE_ENABLED)
	    return null;

	//get the thing at this number
	if(number < 0)
	    throw new IllegalArgumentException("Attempted to page a negative index");

	if(_paged.size() >= number)
	    return null;

	return _paged.get(number);
    }
    
    public boolean isEmptyLine() {
	return currentLine() != null && currentLine().length() == 0;
    }
    
    public String currentLine() {
	return _currentLine;
    }

    ///////////// NEXTBIGINTEGER

    public BigInteger nextBigInteger() {
        if(hasNextBigInteger()) {
            TOKEN++;
            return _line.nextBigInteger();
        }
	
        return null;
    }
    
    public boolean hasNextBigInteger() {
        if(_line != null) {
            return (_line.hasNextBigInteger());
        }
        if (!checkNextLine())
            return false;
        if(_line != null)
            return (_line.hasNextBigInteger());
        return false;
    }
    
    ///////////// NEXTBIGDECIMAL

    public BigDecimal nextBigDecimal() {
        if(hasNextBigDecimal()) {
            TOKEN++;
            return _line.nextBigDecimal();
        }
	
        return null;
    }
    
    public boolean hasNextBigDecimal() {
        if(_line != null) {
            return (_line.hasNextBigDecimal());
        }
        if (!checkNextLine())
            return false;
        if(_line != null)
            return (_line.hasNextBigDecimal());
        return false;
    }
    
    ///////////// NEXTDOUBLE

    public Double nextDouble() {
        if(hasNextDouble()) {
            TOKEN++;
            return _line.nextDouble();
        }

        return null;
    }

    public boolean hasNextDouble() {
        if(_line != null) {
            return (_line.hasNextDouble());
        }
        if (!checkNextLine())
            return false;
        if(_line != null)
            return (_line.hasNextDouble());
        return false;
    }
    
    ///////////// NEXTLONG
    
    public Long nextLong() {
        if(hasNextLong()) {
            TOKEN++;
            return _line.nextLong();
        }

        return null;
    }

    public boolean hasNextLong() {
        if(_line != null) {
            return (_line.hasNextLong());
        }
        if (!checkNextLine())
            return false;
        if(_line != null)
            return (_line.hasNextLong());
        return false;
    }
    
    ///////////// NEXTINT
    
    public Integer nextInt() {
	if(hasNextInt()) {
	    TOKEN++;
	    return _line.nextInt();
	}

	return null;
    }
    
    public boolean hasNextInt() {
	if(_line != null) {
	    return (_line.hasNextInt());
	}
	if (!checkNextLine())
	    return false;
	if(_line != null)
	    return (_line.hasNextInt());
	return false;
    }

    //////////// NEXT
    
    public String next() {
	if(hasNext()) {
	    TOKEN++;
	    return _line.next();
	}

	return null;
    }
    
    private boolean hasNext() {
	if(_line != null) {
	    return (_line.hasNext());
	}
	if (!checkNextLine())
	    return false;
	if(_line != null)
	    return (_line.hasNext());
	return false;
    }

    //////////// NEXTLINE
    
    public String nextLine() {
	if(hasNextLine()) {
	    //if there's something left on the input
	    if(_line.hasNext()) {
		String res = _line.nextLine();
		if(hasNextLine()) {
		    _currentLine = _input.nextLine();
		    if(_PAGE_ENABLED)
			_paged.add(_currentLine);
		    _line = new Scanner(_currentLine);
		}
		TOKEN = 0;
		LINE += 1;
		return res;
	    }
	    else if(TOKEN == 0) {
		//this line is blank
		String res = _currentLine;
		_currentLine = _input.nextLine();
		if(_PAGE_ENABLED)
		    _paged.add(_currentLine);
		LINE += 1;
		TOKEN = 0;
		_line = new Scanner(_currentLine);
		return res;
	    }

	    //there's nothing left on the current line
	    _currentLine = _input.nextLine();
	    if(_PAGE_ENABLED)
		    _paged.add(_currentLine);
	    _line = new Scanner(_currentLine);
	    TOKEN = 0;
	    LINE += 1;

	    //this is our hack for empty lines
	    if(_line.hasNextLine())
		return _line.nextLine();
	    else
		return _currentLine;
	}

	return null;
    }
    
    public boolean hasNextLine() {
	return checkNextLine();	
    }

    private boolean checkNextLine() {
	if(_line == null) {
	    if(_input.hasNextLine()) {
		_currentLine = _input.nextLine();
		if(_PAGE_ENABLED)
		    _paged.add(_currentLine);
		println("From Check: '" + _currentLine + "'");		       
		_line = new Scanner(_currentLine);
	    }
	    else
		return false;
	}
			
	return (_line.hasNext() || _input.hasNextLine());
    }

    

    /*********************************************************
     *
     *                    OUTPUT COMMANDS
     *
     *********************************************************/
    
    public DebugLogger logger = new DebugLogger() {
	    public void DEBUG(String s) { self.DEBUG(s); }
	    public void DEBUG(int level, Object s) { self.DEBUG(1, s); }
	    public void DEBUGF(int level, String s, Object... args) { self.DEBUGF(level, s, args); }

	    public void print(Object value) { self.print(value); }
	    public void printf(String value, Object... args) { self.printf(value, args); }
	    public void println(Object value) { self.println(value); }
	};	    
    
    //DEBUG (int level, String s)
    //DEBUG (String s) -> DEBUG(1, s)
    //DEBUGF(int level, String s, args) -> DEBUG(level, f(s, args))
    //DEBUGF(String s, args) -> DEBUGF(1, s, args)
    //
    //print
    //printf
    //println
    //
    //a2s - > arrayToString
    
    //colorize based on debug level
    //1 = black
    //2 = bold black
    //3 = yellow
    //4 = red
    //5 = bold red
    private Color _DEBUG_TO_COLOR(int level) {
	switch(level) {
	case 0:
	case 1:
	    return Color.RESET;
	case 2:
	    return Color.GREEN;
	case 3:
	    return Color.YELLOW;
	case 4:
	    return Color.RED;
	case 5:
	    return Color.RED_BOLD;
	default:
	    return Color.BLACK;
	}
    }

    public void DEBUG() {
	DEBUG("");
    }

    public int ERR(String message) {
	System.err.println(_DEBUG_COLORIZE(message.toString(), _DEBUG_TO_COLOR(5)));
	return 1;
    }
    
    public void DEBUG(int level, Object message) {
	if(_DEBUG_LEVEL == 0)
	    return;

	//we print anything with a level equal to or below our own
	if(_DEBUG_LEVEL >= level)
	    System.err.println(_DEBUG_COLORIZE(message.toString(), _DEBUG_TO_COLOR(level)));
    }

    public void DEBUG(Object message) {
	DEBUG(1, message);
    }

    public void DEBUGF(int level, String message, Object... args) {
	if(_DEBUG_LEVEL == 0)
	    return;

	if(_DEBUG_LEVEL == 0)
	    return;

	String tmp = String.format(message, args);
	
	//we print anything with a level equal to or below our own
	if(_DEBUG_LEVEL >= level)
	    System.err.print(_DEBUG_COLORIZE(tmp, _DEBUG_TO_COLOR(level)));
    }

    public void DEBUGF(String message, Object... args) {
	DEBUGF(1, message, args);
    }

    private String _DEBUG_COLORIZE(String s, Color c) {
	return Color.colorize(_COLOR_ENABLED(), s, c);
    }
    
    public void print(Object a) {
	System.out.print(a);
    }

    public void printf(String a, Object... args) {
	System.out.printf(a, args);
    }

    public void println(Object a) {
	System.out.println(a);
    }

    public void println() {
	System.out.println();
    }


    //array to str

    
    public String a2s(Object[] a) {
	return arrayToString(a);
    }

    public String a2s(int[] a) {
	return arrayToString(a);
    }

    public String a2s(long[] a) {
	return arrayToString(a);
    }

    public String a2s(float[] a) {
	return arrayToString(a);
    }

    public String a2s(double[] a) {
	return arrayToString(a);
    }

    public String a2s(boolean[] a) {
	return arrayToString(a);
    }

    public String a2s(byte[] a) {
	return arrayToString(a);
    }

    public String a2s(short[] a) {
	return arrayToString(a);
    }

    public String a2s(char[] a) {
	return arrayToString(a);
    }
        

    /*********************************************************
     *
     *                   DEFAULT COMMANDS
     *
     *********************************************************/

    private final OptionalIntCommand _debugLevel =
	new OptionalIntCommand(0, 5, false, 1, "-d","--debug","--debug-level")
	.setName("(Default) Debug Level")
	.setDescription("Sets the debug level. " + 
			"Level 0 means no debug input is displayed, " +
			"the allowable range for debug is (0, 5), "+
			"and it is up to each program to decide what to display at each level."
			+ " All debug output between levels 0 and the selected level " +
			"will be displayed during operation of the program.");

    private final BooleanCommand _page = new BooleanCommand(false, "-p", "--page-enabled")
	.setName("(Default) Page Mode")
	.setDescription("Sets wether page mode is or isn't enabled. If it is enabled, " +
			"Then all input that is read will be saved. All of the saved input will be readily accessible on a line-by-line basis with the page(line) function. "
			+ "This may end up using too much memory if the input happens to be particularly large. This is disabled by default.");

    private final BooleanCommand _help = new BooleanCommand(false, "-h", "-h", "--help", "--show-help")
	.setName("(Default) Display Help")
	.setDescription("Displays this help dialogue. " +
			"This dialogue will also display if one " +
			"of the inputs happens to be invalid.")
	.setTerminal();
    
    private final BooleanCommand _disableColors = new BooleanCommand(false, "-dc", "--disable-colors")
	.setName("(Default) Disable Colors")
	.setDescription("Disables the output of any colorized strings");
    
    private final BooleanCommand _ignore = new BooleanCommand(false, "-i", "--ignore-remaining")
	.setName("(Default) Ignore Remaining")
	.setDescription("Ignores all remaining input")
	.setTerminal();

    private void actOnDefaultCommands() {
	_COLOR_HARD_DISABLED = (_disableColors.matched());
	_PAGE_ENABLED = (_page.matched());
	_DEBUG_LEVEL = (_debugLevel.matched() ? _debugLevel.getValue() : 0);

	//if the debug level is greater than 0, then debug mode as a whole is enabled
	_DEBUG = _DEBUG_LEVEL > 0;
    }
}
