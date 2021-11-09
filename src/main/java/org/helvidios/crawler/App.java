package org.helvidios.crawler;

/**
 * Hello world!
 *
 */
public class App 
{

    record Person(String name){}

    public static void main( String[] args )
    {
        var person = new Person("Jane Eyre");
        System.out.println(person);
        System.out.println( "Hello World!" );
    }
}
