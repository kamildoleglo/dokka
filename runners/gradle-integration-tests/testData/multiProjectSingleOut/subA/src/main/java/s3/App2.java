package s3;

import s1.TestClass;

/**
 * Sample class inside the package to be included in the javadoc
 * @see TestClass
 * @author <a href="mailto:bilbo.baggins@mordor.com">Bilbo Baggins</a>
 */
public class App2 {
    /**
     * The main method
     *
     * @param args an array of strings that contains the arguments
     */
    public static void main( String[] args )
    {
        System.out.println( "Sample Application." );
    }

    /**
     * Sample method that prints out the parameter string.
     *
     * @param str   The string value to be printed.
     */
    protected void sampleMethod( String str )
    {
        System.out.println( str );
    }
}

